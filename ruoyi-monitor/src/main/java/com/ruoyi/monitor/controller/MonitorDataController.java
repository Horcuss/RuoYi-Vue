package com.ruoyi.monitor.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.monitor.service.IMonitorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 监控数据Controller
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
@RestController
@RequestMapping("/monitor/data")
public class MonitorDataController extends BaseController
{
    @Autowired
    private IMonitorDataService monitorDataService;

    /**
     * 根据配置KEY和参数获取监控数据
     */
    @PostMapping("/{configKey}")
    public AjaxResult getDataWithParams(@PathVariable("configKey") String configKey,
                                        @RequestBody Map<String, Object> params)
    {
        Map<String, Object> data = monitorDataService.getMonitorData(configKey, params);
        return success(data);
    }
}

