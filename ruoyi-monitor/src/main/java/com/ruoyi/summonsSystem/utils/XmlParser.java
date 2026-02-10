package com.ruoyi.monitor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * XML解析工具类
 * 用于解析监控API返回的XML响应
 *
 * @author ruoyi
 * @date 2025-01-15
 */
public class XmlParser
{
    private static final Logger log = LoggerFactory.getLogger(XmlParser.class);

    /**
     * 解析XML响应，返回所有row的数据
     *
     * @param xmlString XML字符串
     * @return List<Map<String, String>> 每个Map代表一个row，key是字段名（如"item_0", "item_1"...）
     */
    public static List<Map<String, String>> parseXmlResponse(String xmlString) throws Exception
    {
        List<Map<String, String>> result = new ArrayList<>();

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));

            // 获取所有row节点
            NodeList rowNodes = doc.getElementsByTagName("row");
            log.info("解析XML，找到 {} 个row节点", rowNodes.getLength());

            for (int i = 0; i < rowNodes.getLength(); i++)
            {
                Element rowElement = (Element) rowNodes.item(i);
                NodeList items = rowElement.getElementsByTagName("item");

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < items.getLength(); j++)
                {
                    Element item = (Element) items.item(j);
                    String value = item.getTextContent().trim();
                    rowData.put("item_" + j, value);
                }

                log.debug("解析row[{}]: {}", i, rowData);
                result.add(rowData);
            }

            return result;
        }
        catch (Exception e)
        {
            log.error("解析XML失败", e);
            throw e;
        }
    }

    /**
     * 根据加工条件種cd过滤row
     *
     * @param rows 所有XML行
     * @param typeCodeIndex 加工条件種cd在row中的索引位置
     * @param targetTypeCode 目标加工条件種cd值
     * @return 匹配的row列表
     */
    public static List<Map<String, String>> filterByTypeCode(
        List<Map<String, String>> rows,
        int typeCodeIndex,
        String targetTypeCode
    )
    {
        List<Map<String, String>> filtered = new ArrayList<>();

        for (Map<String, String> row : rows)
        {
            String typeCode = row.get("item_" + typeCodeIndex);
            if (targetTypeCode.equals(typeCode))
            {
                filtered.add(row);
            }
        }

        log.debug("过滤加工条件種cd={}，找到 {} 个匹配的row", targetTypeCode, filtered.size());
        return filtered;
    }

    /**
     * 从row中提取指定位置的值
     *
     * @param row 行数据
     * @param index 索引位置
     * @return 值
     */
    public static String extractValue(Map<String, String> row, int index)
    {
        return row.getOrDefault("item_" + index, "");
    }

    /**
     * 构建GP项目的XML请求体
     *
     * @param params 请求参数
     * @return XML字符串
     */
    public static String buildGpXmlRequest(Map<String, Object> params)
    {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<trafficParam>");
        xml.append("<trafficId>61112</trafficId>");
        xml.append("<pattern><row>");

        // 固定字段
        xml.append("<item>").append(params.getOrDefault("userName", "")).append("</item>");  // 氏名cd
        xml.append("<item></item>");  // 文书区分
        xml.append("<item></item>");  // 制造规格类no
        xml.append("<item></item>");  // 制作区分
        xml.append("<item></item>");  // lotno
        xml.append("<item>").append(params.getOrDefault("productName", "")).append("</item>");  // 品名
        xml.append("<item></item>");  // 工程系列
        xml.append("<item></item>");  // 品名种别cd
        xml.append("<item>").append(params.getOrDefault("majorClassCd", "")).append("</item>");  // 大分类cd
        xml.append("<item>").append(params.getOrDefault("minorClassCd", "")).append("</item>");  // 中分类cd
        xml.append("<item></item>");  // 加工条件种cd
        xml.append("<item></item>");  // 指示书
        xml.append("<item></item>");  // 变更flag

        xml.append("</row></pattern>");
        xml.append("</trafficParam>");

        return xml.toString();
    }

    /**
     * 构建非GP项目的XML请求体
     *
     * @param params 请求参数
     * @return XML字符串
     */
    public static String buildNonGpXmlRequest(Map<String, Object> params)
    {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<trafficParam>");
        xml.append("<trafficId>61113</trafficId>");
        xml.append("<pattern><row>");

        // 固定字段
        xml.append("<item>").append(params.getOrDefault("userName", "")).append("</item>");  // 氏名cd
        xml.append("<item></item>");  // 制作区分
        xml.append("<item>").append(params.getOrDefault("lotNo", "")).append("</item>");  // lotno
        xml.append("<item></item>");  // 标准书
        xml.append("<item></item>");  // 加工条件种cd
        xml.append("<item>").append(params.getOrDefault("majorClassCd", "")).append("</item>");  // 大分类cd
        xml.append("<item>").append(params.getOrDefault("minorClassCd", "")).append("</item>");  // 中分类cd

        xml.append("</row></pattern>");
        xml.append("</trafficParam>");

        return xml.toString();
    }
}
