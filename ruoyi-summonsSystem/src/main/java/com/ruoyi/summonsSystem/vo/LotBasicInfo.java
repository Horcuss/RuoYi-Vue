package com.ruoyi.summonsSystem.vo;

/**
 * lot基础数据（非数据库实体，由 LotDataProvider 接口返回）
 */
public class LotBasicInfo
{
    private String lotNo;
    private String productName;
    private String productModel;
    private String customer;

    public LotBasicInfo()
    {
    }

    public LotBasicInfo(String lotNo, String productName, String productModel, String customer)
    {
        this.lotNo = lotNo;
        this.productName = productName;
        this.productModel = productModel;
        this.customer = customer;
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
}
