package com.ruoyi.summonsSystem.flow.adapter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.summonsSystem.flow.UnifiedFlowEvent;
import com.ruoyi.summonsSystem.mapper.RfidLotBindingMapper;
import com.ruoyi.summonsSystem.mapper.SensorProcessMappingMapper;
import com.ruoyi.summonsSystem.vo.RfidLotBinding;
import com.ruoyi.summonsSystem.vo.SensorProcessMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * WS感应器事件适配器
 * 将WS消息 { rfid, ant, ip, status, remark, time } 转换为 UnifiedFlowEvent
 */
@Component
public class WsSensorEventAdapter implements FlowEventAdapter
{
    private static final Logger log = LoggerFactory.getLogger(WsSensorEventAdapter.class);

    @Autowired
    private SensorProcessMappingMapper sensorProcessMappingMapper;

    @Autowired
    private RfidLotBindingMapper rfidLotBindingMapper;

    @Override
    public String getName()
    {
        return "WS_SENSOR";
    }

    @Override
    public UnifiedFlowEvent adapt(String rawData)
    {
        try
        {
            JSONObject json = JSON.parseObject(rawData);
            String rfid = json.getString("rfid");
            String ip = json.getString("ip");
            Integer status = json.getInteger("status");

            if (rfid == null || ip == null || status == null)
            {
                log.warn("[WsSensorAdapter] 消息字段不完整: {}", rawData);
                return null;
            }

            // 查rfid对应的lot
            RfidLotBinding binding = rfidLotBindingMapper.selectActiveByRfid(rfid);
            if (binding == null)
            {
                log.info("[WsSensorAdapter] rfid未绑定lot，忽略流转事件: rfid={}", rfid);
                return null;
            }

            // 查感应器对应的工序（默认端口9001）
            String port = json.getString("port");
            if (port == null)
            {
                port = "9001";
            }
            SensorProcessMapping mapping = sensorProcessMappingMapper.selectByIpAndPort(ip, port);
            if (mapping == null)
            {
                log.warn("[WsSensorAdapter] 未找到感应器映射: ip={}, port={}", ip, port);
                return null;
            }

            // 只处理flow类型感应器
            if (!"flow".equals(mapping.getSensorType()))
            {
                log.info("[WsSensorAdapter] 非flow类型感应器，跳过: ip={}, type={}", ip, mapping.getSensorType());
                return null;
            }

            String eventType = (status == 1) ? UnifiedFlowEvent.EVENT_IN : UnifiedFlowEvent.EVENT_OUT;

            UnifiedFlowEvent event = new UnifiedFlowEvent(
                binding.getLotNo(),
                mapping.getProcessCode(),
                eventType,
                new Date(),
                getName()
            );
            event.setRawData(rawData);

            log.info("[WsSensorAdapter] 转换完成: {}", event);
            return event;
        }
        catch (Exception e)
        {
            log.error("[WsSensorAdapter] 解析WS消息失败: {}", rawData, e);
            return null;
        }
    }
}
