package com.ruoyi.summonsSystem.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.summonsSystem.flow.FlowResult;
import com.ruoyi.summonsSystem.flow.UnifiedFlowEvent;
import com.ruoyi.summonsSystem.mapper.LotFlowStatusMapper;
import com.ruoyi.summonsSystem.mapper.LotProcessSequenceMapper;
import com.ruoyi.summonsSystem.service.IFlowEngineService;
import com.ruoyi.summonsSystem.vo.LotFlowStatus;
import com.ruoyi.summonsSystem.vo.LotProcessSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 工序流转测试Controller
 * 提供模拟感应器事件的API，方便测试流转引擎
 */
@RestController
@RequestMapping("/api/ticket/flow")
public class FlowTestController extends BaseController
{
    @Autowired
    private IFlowEngineService flowEngineService;

    @Autowired
    private LotFlowStatusMapper lotFlowStatusMapper;

    @Autowired
    private LotProcessSequenceMapper lotProcessSequenceMapper;

    /**
     * 模拟感应器事件（IN或OUT）
     * 参数: lotNo, processCode, eventType, isTestingEvent(可选,默认false)
     */
    @PostMapping("/simulate")
    public AjaxResult simulate(@RequestBody Map<String, Object> params)
    {
        String lotNo = (String) params.get("lotNo");
        String processCode = (String) params.get("processCode");
        String eventType = (String) params.get("eventType");
        Boolean isTestingEvent = Boolean.TRUE.equals(params.get("isTestingEvent"));

        if (lotNo == null || lotNo.isEmpty())
        {
            return error("lotNo不能为空");
        }
        if (processCode == null || processCode.isEmpty())
        {
            return error("processCode不能为空");
        }
        if (eventType == null || eventType.isEmpty())
        {
            return error("eventType不能为空（IN或OUT）");
        }

        UnifiedFlowEvent event = new UnifiedFlowEvent(
            lotNo, processCode, eventType.toUpperCase(), new Date(), "MANUAL_TEST"
        );
        event.setTestingEvent(isTestingEvent);

        FlowResult result = flowEngineService.processEvent(event);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("transitioned", result.isTransitioned());
        data.put("abnormal", result.isAbnormal());
        data.put("message", result.getMessage());
        data.put("newProcessCode", result.getNewProcessCode());
        data.put("newProcessName", result.getNewProcessName());

        // 附带最新流转状态
        LotFlowStatus status = lotFlowStatusMapper.selectByLotNo(lotNo);
        if (status != null)
        {
            Map<String, Object> statusMap = buildStatusMap(status);
            data.put("currentStatus", statusMap);
        }

        return success(data);
    }

    /**
     * 查询lot的流转状态
     */
    @GetMapping("/status/{lotNo}")
    public AjaxResult getStatus(@PathVariable("lotNo") String lotNo)
    {
        LotFlowStatus status = lotFlowStatusMapper.selectByLotNo(lotNo);
        if (status == null)
        {
            return error("未找到该lot的流转状态，请先绑定水墨屏");
        }
        return success(buildStatusMap(status));
    }

    /**
     * 查询lot的工序序列
     */
    @GetMapping("/sequence/{lotNo}")
    public AjaxResult getSequence(@PathVariable("lotNo") String lotNo)
    {
        List<LotProcessSequence> sequences = lotProcessSequenceMapper.selectByLotNo(lotNo);
        if (sequences == null || sequences.isEmpty())
        {
            return error("未找到该lot的工序序列，请先绑定水墨屏");
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (LotProcessSequence seq : sequences)
        {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("seq", seq.getSeq());
            map.put("processCode", seq.getProcessCode());
            map.put("processName", seq.getProcessName());
            map.put("processSource", seq.getProcessSource());
            list.add(map);
        }
        return success(list);
    }

    /**
     * 重置lot流转状态（回到指定工序重新测试）
     */
    @PostMapping("/reset/{lotNo}")
    public AjaxResult reset(@PathVariable("lotNo") String lotNo, @RequestBody Map<String, String> params)
    {
        LotFlowStatus status = lotFlowStatusMapper.selectByLotNo(lotNo);
        if (status == null)
        {
            return error("未找到该lot的流转状态");
        }

        String processCode = params.get("processCode");
        if (processCode == null || processCode.isEmpty())
        {
            // 默认回到第一道工序
            List<LotProcessSequence> sequences = lotProcessSequenceMapper.selectByLotNo(lotNo);
            if (!sequences.isEmpty())
            {
                processCode = sequences.get(0).getProcessCode();
            }
            else
            {
                return error("无工序序列");
            }
        }

        LotProcessSequence seqRecord = lotProcessSequenceMapper.selectByLotNoAndProcessCode(lotNo, processCode);
        int seq = (seqRecord != null) ? seqRecord.getSeq() : 1;

        status.setCurrentProcessCode(processCode);
        status.setCurrentSeq(seq);
        status.setHasExited(0);
        status.setTestingRound(0);
        status.setTestingProcessCode(null);
        status.setStatus("NORMAL");
        status.setAbnormalMsg(null);
        status.setLastEventType(null);
        status.setLastEventTime(null);
        lotFlowStatusMapper.update(status);

        return success(buildStatusMap(status));
    }

    private Map<String, Object> buildStatusMap(LotFlowStatus status)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("lotNo", status.getLotNo());
        map.put("currentProcessCode", status.getCurrentProcessCode());
        map.put("currentSeq", status.getCurrentSeq());
        map.put("hasExited", status.getHasExited());
        map.put("testingRound", status.getTestingRound());
        map.put("testingProcessCode", status.getTestingProcessCode());
        map.put("status", status.getStatus());
        map.put("abnormalMsg", status.getAbnormalMsg());
        map.put("lastEventType", status.getLastEventType());
        map.put("lastEventTime", status.getLastEventTime());
        return map;
    }
}
