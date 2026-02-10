package com.ruoyi.summonsSystem.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.summonsSystem.flow.FlowResult;
import com.ruoyi.summonsSystem.flow.UnifiedFlowEvent;
import com.ruoyi.summonsSystem.flow.adapter.WsSensorEventAdapter;
import com.ruoyi.summonsSystem.mapper.ProcessCodeMappingMapper;
import com.ruoyi.summonsSystem.mapper.SensorEventLogMapper;
import com.ruoyi.summonsSystem.mapper.SensorProcessMappingMapper;
import com.ruoyi.summonsSystem.service.IFlowEngineService;
import com.ruoyi.summonsSystem.vo.ProcessCodeMapping;
import com.ruoyi.summonsSystem.vo.SensorEventLog;
import com.ruoyi.summonsSystem.vo.SensorProcessMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 传感器WebSocket处理器
 * 接收感应器数据，根据感应器类型分发处理
 */
@ServerEndpoint("/ws/sensor")
@Component
public class SensorWebSocketHandler
{
    private static final Logger log = LoggerFactory.getLogger(SensorWebSocketHandler.class);

    /** 所有连接的session */
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    /** 绑定页面前端session（用于推送bind类型感应器数据） */
    private static final CopyOnWriteArraySet<Session> BIND_PAGE_SESSIONS = new CopyOnWriteArraySet<>();

    private static SensorEventLogMapper sensorEventLogMapper;
    private static SensorProcessMappingMapper sensorProcessMappingMapper;
    private static ProcessCodeMappingMapper processCodeMappingMapper;
    private static WsSensorEventAdapter wsSensorEventAdapter;
    private static IFlowEngineService flowEngineService;

    @Autowired
    public void setSensorEventLogMapper(SensorEventLogMapper mapper)
    {
        SensorWebSocketHandler.sensorEventLogMapper = mapper;
    }

    @Autowired
    public void setSensorProcessMappingMapper(SensorProcessMappingMapper mapper)
    {
        SensorWebSocketHandler.sensorProcessMappingMapper = mapper;
    }

    @Autowired
    public void setProcessCodeMappingMapper(ProcessCodeMappingMapper mapper)
    {
        SensorWebSocketHandler.processCodeMappingMapper = mapper;
    }

    @Autowired
    public void setWsSensorEventAdapter(WsSensorEventAdapter adapter)
    {
        SensorWebSocketHandler.wsSensorEventAdapter = adapter;
    }

    @Autowired
    public void setFlowEngineService(IFlowEngineService service)
    {
        SensorWebSocketHandler.flowEngineService = service;
    }

    @OnOpen
    public void onOpen(Session session)
    {
        SESSIONS.add(session);
        // 默认也加到bind页面sessions
        BIND_PAGE_SESSIONS.add(session);
        log.info("WS连接建立, sessionId={}, 当前连接数={}", session.getId(), SESSIONS.size());
    }

    @OnClose
    public void onClose(Session session)
    {
        SESSIONS.remove(session);
        BIND_PAGE_SESSIONS.remove(session);
        log.info("WS连接关闭, sessionId={}, 当前连接数={}", session.getId(), SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable error)
    {
        log.error("WS错误, sessionId={}", session.getId(), error);
        SESSIONS.remove(session);
        BIND_PAGE_SESSIONS.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        log.info("WS收到消息: {}", message);

        try
        {
            JSONObject json = JSON.parseObject(message);
            String rfid = json.getString("rfid");
            String ip = json.getString("ip");
            Integer status = json.getInteger("status");
            String ant = json.getString("ant");
            String remark = json.getString("remark");

            // 1. 记录事件日志
            SensorEventLog eventLog = new SensorEventLog();
            eventLog.setRfid(rfid);
            eventLog.setAnt(ant);
            eventLog.setSensorIp(ip);
            eventLog.setSensorPort(json.getString("port"));
            eventLog.setEventType(status);
            eventLog.setRemark(remark);
            eventLog.setEventTime(new Date());
            eventLog.setProcessed(0);
            sensorEventLogMapper.insert(eventLog);

            // 2. 查询感应器映射
            String port = json.getString("port");
            if (port == null)
            {
                port = "9001";
            }
            SensorProcessMapping mapping = sensorProcessMappingMapper.selectByIpAndPort(ip, port);
            if (mapping == null)
            {
                log.warn("未找到感应器映射: ip={}, port={}", ip, port);
                updateEventLog(eventLog, null, "未找到感应器映射");
                return;
            }

            String processCode = mapping.getProcessCode();

            if ("bind".equals(mapping.getSensorType()))
            {
                // bind类型：推送到前端绑定页面
                handleBindSensor(rfid, processCode, eventLog);
            }
            else if ("flow".equals(mapping.getSensorType()))
            {
                // flow类型：交给流转引擎
                handleFlowSensor(message, eventLog, processCode);
            }
        }
        catch (Exception e)
        {
            log.error("处理WS消息异常: {}", message, e);
        }
    }

    /**
     * 处理bind类型感应器：推送rfid + processCode + processName到前端
     */
    private void handleBindSensor(String rfid, String processCode, SensorEventLog eventLog)
    {
        ProcessCodeMapping codeMapping = processCodeMappingMapper.selectByCode(processCode);
        String processName = (codeMapping != null) ? codeMapping.getProcessName() : processCode;

        JSONObject pushData = new JSONObject();
        pushData.put("type", "bind");
        pushData.put("rfid", rfid);
        pushData.put("processCode", processCode);
        pushData.put("processName", processName);

        String pushMsg = pushData.toJSONString();
        for (Session s : BIND_PAGE_SESSIONS)
        {
            try
            {
                if (s.isOpen())
                {
                    s.getBasicRemote().sendText(pushMsg);
                }
            }
            catch (IOException e)
            {
                log.error("推送bind数据失败: sessionId={}", s.getId(), e);
            }
        }

        updateEventLog(eventLog, processCode, "bind推送完成");
        log.info("bind类型感应器：推送到前端 rfid={}, processCode={}, processName={}", rfid, processCode, processName);
    }

    /**
     * 处理flow类型感应器：调用流转引擎
     */
    private void handleFlowSensor(String rawMessage, SensorEventLog eventLog, String processCode)
    {
        UnifiedFlowEvent event = wsSensorEventAdapter.adapt(rawMessage);
        if (event == null)
        {
            updateEventLog(eventLog, processCode, "适配器返回null，跳过");
            return;
        }

        FlowResult result = flowEngineService.processEvent(event);
        updateEventLog(eventLog, processCode, result.getMessage());
        log.info("flow类型感应器处理结果: {}", result.getMessage());
    }

    /**
     * 更新事件日志处理结果
     */
    private void updateEventLog(SensorEventLog eventLog, String processCode, String result)
    {
        eventLog.setProcessed(1);
        eventLog.setProcessCode(processCode);
        eventLog.setProcessResult(result);
        try
        {
            sensorEventLogMapper.updateProcessed(eventLog);
        }
        catch (Exception e)
        {
            log.error("更新事件日志失败", e);
        }
    }
}
