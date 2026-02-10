package com.ruoyi.summonsSystem.vo;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 水墨屏显示内容对象 ticket_eink_display_content
 */
public class EinkDisplayContent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String rfid;
    private String lotNo;
    private String productName;
    private String productModel;
    private String customer;
    private String currentProcessCode;
    private String currentProcessName;
    private String gpDefectInfo;
    private String displayData;
    private String status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getRfid()
    {
        return rfid;
    }

    public void setRfid(String rfid)
    {
        this.rfid = rfid;
    }

    public String getLotNo()
    {
        return lotNo;
    }

    public void setLotNo(String lotNo)
    {
        this.lotNo = lotNo;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getProductModel()
    {
        return productModel;
    }

    public void setProductModel(String productModel)
    {
        this.productModel = productModel;
    }

    public String getCustomer()
    {
        return customer;
    }

    public void setCustomer(String customer)
    {
        this.customer = customer;
    }

    public String getCurrentProcessCode()
    {
        return currentProcessCode;
    }

    public void setCurrentProcessCode(String currentProcessCode)
    {
        this.currentProcessCode = currentProcessCode;
    }

    public String getCurrentProcessName()
    {
        return currentProcessName;
    }

    public void setCurrentProcessName(String currentProcessName)
    {
        this.currentProcessName = currentProcessName;
    }

    public String getGpDefectInfo()
    {
        return gpDefectInfo;
    }

    public void setGpDefectInfo(String gpDefectInfo)
    {
        this.gpDefectInfo = gpDefectInfo;
    }

    public String getDisplayData()
    {
        return displayData;
    }

    public void setDisplayData(String displayData)
    {
        this.displayData = displayData;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
