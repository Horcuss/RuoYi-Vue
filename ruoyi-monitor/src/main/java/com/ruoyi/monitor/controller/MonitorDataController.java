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

    /**
     * 获取下拉框选项
     * @param configKey 配置KEY
     * @param params 包含用户输入的品名/lotno等参数
     */
    @PostMapping("/{configKey}/selectOptions")
    public AjaxResult getSelectOptions(@PathVariable("configKey") String configKey,
                                       @RequestBody Map<String, Object> params)
    {
        Map<String, Object> options = monitorDataService.getSelectOptions(configKey, params);
        return success(options);
    }
}

