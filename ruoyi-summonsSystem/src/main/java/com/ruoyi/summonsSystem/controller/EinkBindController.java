package com.ruoyi.summonsSystem.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.summonsSystem.service.IEinkBindService;

import java.util.Map;

/**
 * 水墨屏绑定Controller
 */
@RestController
@RequestMapping("/api/ticket/eink")
public class EinkBindController extends BaseController
{
    @Autowired
    private IEinkBindService einkBindService;

    /**
     * 绑定水墨屏与lot
     */
    @PostMapping("/bind")
    public AjaxResult bind(@RequestBody Map<String, String> params)
    {
        String rfid = params.get("rfid");
        String lotNo = params.get("lotNo");
        String processCode = params.get("processCode");

        if (rfid == null || rfid.isEmpty())
        {
            return error("水墨屏编号不能为空");
        }
        if (lotNo == null || lotNo.isEmpty())
        {
            return error("lot编号不能为空");
        }
        if (processCode == null || processCode.isEmpty())
        {
            return error("工序code不能为空");
        }

        try
        {
            Map<String, Object> result = einkBindService.bind(rfid, lotNo, processCode);
            return success(result);
        }
        catch (Exception e)
        {
            logger.error("绑定失败", e);
            return error("绑定失败: " + e.getMessage());
        }
    }
}
