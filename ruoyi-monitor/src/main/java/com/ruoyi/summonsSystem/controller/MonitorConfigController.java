package com.ruoyi.monitor.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.monitor.domain.MonitorConfig;
import com.ruoyi.monitor.service.IMonitorConfigService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 监控配置Controller
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
@RestController
@RequestMapping("/monitor/config")
public class MonitorConfigController extends BaseController
{
    @Autowired
    private IMonitorConfigService monitorConfigService;

    /**
     * 查询监控配置列表
     */
    @PreAuthorize("@ss.hasPermi('monitor:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(MonitorConfig monitorConfig)
    {
        startPage();
        List<MonitorConfig> list = monitorConfigService.selectMonitorConfigList(monitorConfig);
        return getDataTable(list);
    }

    /**
     * 获取监控配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('monitor:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(monitorConfigService.selectMonitorConfigByConfigId(configId));
    }

    /**
     * 根据配置KEY获取监控配置
     */
    @GetMapping(value = "/key/{configKey}")
    public AjaxResult getInfoByKey(@PathVariable("configKey") String configKey)
    {
        return success(monitorConfigService.selectMonitorConfigByConfigKey(configKey));
    }

    /**
     * 新增监控配置
     */
    @PreAuthorize("@ss.hasPermi('monitor:config:add')")
    @Log(title = "监控配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MonitorConfig monitorConfig)
    {
        if (!monitorConfigService.checkConfigKeyUnique(monitorConfig))
        {
            return error("新增监控配置'" + monitorConfig.getConfigName() + "'失败，配置KEY已存在");
        }
        monitorConfig.setCreateBy(getUsername());
        return toAjax(monitorConfigService.insertMonitorConfig(monitorConfig));
    }

    /**
     * 修改监控配置
     */
    @PreAuthorize("@ss.hasPermi('monitor:config:edit')")
    @Log(title = "监控配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MonitorConfig monitorConfig)
    {
        if (!monitorConfigService.checkConfigKeyUnique(monitorConfig))
        {
            return error("修改监控配置'" + monitorConfig.getConfigName() + "'失败，配置KEY已存在");
        }
        monitorConfig.setUpdateBy(getUsername());
        return toAjax(monitorConfigService.updateMonitorConfig(monitorConfig));
    }

    /**
     * 删除监控配置
     */
    @PreAuthorize("@ss.hasPermi('monitor:config:remove')")
    @Log(title = "监控配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(monitorConfigService.deleteMonitorConfigByConfigIds(configIds));
    }
}

