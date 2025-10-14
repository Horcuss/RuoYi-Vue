package com.ruoyi.monitor.utils;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON路径提取工具类
 * 支持嵌套路径提取，如：geo.lat, address.city
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
public class JsonPathExtractor 
{
    private static final Logger log = LoggerFactory.getLogger(JsonPathExtractor.class);

    /**
     * 从JSON对象中提取指定路径的值
     * 
     * @param jsonObject JSON对象
     * @param path 路径，支持点号分隔的嵌套路径，如：geo.lat, address.city
     * @return 提取的值，如果路径不存在返回null
     */
    public static Object extractValue(JSONObject jsonObject, String path) 
    {
        if (jsonObject == null || StringUtils.isEmpty(path)) 
        {
            return null;
        }

        try 
        {
            // 去除首尾空格
            path = path.trim();
            
            // 如果路径不包含点号，直接获取
            if (!path.contains(".")) 
            {
                return jsonObject.get(path);
            }

            // 分割路径
            String[] pathParts = path.split("\\.");
            Object current = jsonObject;

            // 逐级获取
            for (String part : pathParts) 
            {
                if (current == null) 
                {
                    return null;
                }

                if (current instanceof JSONObject) 
                {
                    current = ((JSONObject) current).get(part);
                } 
                else 
                {
                    log.warn("路径 {} 中的 {} 不是JSON对象，无法继续提取", path, part);
                    return null;
                }
            }

            return current;
        } 
        catch (Exception e) 
        {
            log.error("从JSON中提取路径 {} 失败", path, e);
            return null;
        }
    }

    /**
     * 从JSON对象中提取指定路径的字符串值
     * 
     * @param jsonObject JSON对象
     * @param path 路径
     * @return 字符串值
     */
    public static String extractString(JSONObject jsonObject, String path) 
    {
        Object value = extractValue(jsonObject, path);
        return value != null ? value.toString() : null;
    }

    /**
     * 从JSON对象中提取指定路径的数字值
     * 
     * @param jsonObject JSON对象
     * @param path 路径
     * @return 数字值
     */
    public static Double extractNumber(JSONObject jsonObject, String path) 
    {
        Object value = extractValue(jsonObject, path);
        if (value == null) 
        {
            return null;
        }

        try 
        {
            if (value instanceof Number) 
            {
                return ((Number) value).doubleValue();
            } 
            else 
            {
                return Double.parseDouble(value.toString());
            }
        } 
        catch (NumberFormatException e) 
        {
            log.error("路径 {} 的值 {} 无法转换为数字", path, value);
            return null;
        }
    }
}

