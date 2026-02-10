package com.ruoyi.summonsSystem.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.summonsSystem.service.IDefectService;
import com.ruoyi.summonsSystem.vo.DefectDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 不良入力Controller
 */
@RestController
@RequestMapping("/api/ticket/defect")
public class DefectController extends BaseController
{
    @Autowired
    private IDefectService defectService;

    /**
     * 获取lot当前工序
     */
    @GetMapping("/current-process/{lotNo}")
    public AjaxResult getCurrentProcess(@PathVariable("lotNo") String lotNo)
    {
        Map<String, String> process = defectService.getCurrentProcess(lotNo);
        if (process.isEmpty())
        {
            return error("未找到该lot的工序信息，请先绑定水墨屏");
        }
        return success(process);
    }

    /**
     * 提交不良数据
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody DefectDetail detail)
    {
        if (detail.getLotNo() == null || detail.getLotNo().isEmpty())
        {
            return error("lot编号不能为空");
        }
        if (detail.getProcessCode() == null || detail.getProcessCode().isEmpty())
        {
            return error("工序code不能为空");
        }
        if (detail.getDefectItem() == null || detail.getDefectItem().isEmpty())
        {
            return error("不良项目不能为空");
        }
        if (detail.getSampleCount() == null || detail.getSampleCount() <= 0)
        {
            return error("取样数量必须大于0");
        }

        try
        {
            defectService.submit(detail);
            return success("提交成功");
        }
        catch (Exception e)
        {
            logger.error("不良入力提交失败", e);
            return error("提交失败: " + e.getMessage());
        }
    }

    /**
     * 查询lot的不良明细列表
     */
    @GetMapping("/list/{lotNo}")
    public AjaxResult getDefectList(@PathVariable("lotNo") String lotNo)
    {
        Map<String, Object> result = defectService.getDefectList(lotNo);
        return success(result);
    }
}
