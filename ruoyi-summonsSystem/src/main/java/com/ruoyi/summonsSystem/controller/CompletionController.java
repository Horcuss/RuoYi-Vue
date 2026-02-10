package com.ruoyi.summonsSystem.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.summonsSystem.service.ICompletionService;
import com.ruoyi.summonsSystem.vo.CompletionDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 完了输机Controller
 */
@RestController
@RequestMapping("/api/ticket/completion")
public class CompletionController extends BaseController
{
    @Autowired
    private ICompletionService completionService;

    /**
     * 提交完了输机数据
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody CompletionDetail detail)
    {
        if (detail.getLotNo() == null || detail.getLotNo().isEmpty())
        {
            return error("lot编号不能为空");
        }
        if (detail.getProcessCode() == null || detail.getProcessCode().isEmpty())
        {
            return error("工序code不能为空");
        }
        if (detail.getWorkDate() == null)
        {
            return error("作业日不能为空");
        }
        if (detail.getWorker() == null || detail.getWorker().isEmpty())
        {
            return error("作业者不能为空");
        }
        if (detail.getCompletionCount() == null)
        {
            return error("完了数不能为空");
        }

        try
        {
            completionService.submit(detail);
            return success("提交成功");
        }
        catch (Exception e)
        {
            logger.error("完了输机提交失败", e);
            return error("提交失败: " + e.getMessage());
        }
    }
}
