package com.ruoyi.monitor.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XML数据缓存
 * 用于缓存从API获取的XML数据，避免重复调用
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Component
public class XmlDataCache
{
    private static final Logger log = LoggerFactory.getLogger(XmlDataCache.class);

    /**
     * 缓存过期时间（毫秒）：5分钟
     */
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000;

    /**
     * 缓存数据
     */
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 缓存项
     */
    private static class CacheEntry
    {
        private final List<Map<String, String>> xmlRows;
        private final long timestamp;

        public CacheEntry(List<Map<String, String>> xmlRows)
        {
            this.xmlRows = xmlRows;
            this.timestamp = System.currentTimeMillis();
        }

        public List<Map<String, String>> getXmlRows()
        {
            return xmlRows;
        }

        public boolean isExpired()
        {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME;
        }
    }

    /**
     * 生成缓存key
     *
     * @param configKey 配置KEY
     * @param params 请求参数
     * @return 缓存key
     */
    public String generateCacheKey(String configKey, Map<String, Object> params)
    {
        try
        {
            // 将参数按key排序后拼接，确保相同参数生成相同的hash
            StringBuilder sb = new StringBuilder(configKey);
            params.keySet().stream()
                    .sorted()
                    .forEach(key -> sb.append("|").append(key).append("=").append(params.get(key)));

            // 计算MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));

            // 转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest)
            {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        }
        catch (Exception e)
        {
            log.error("生成缓存key失败", e);
            // 失败时返回一个简单的key
            return configKey + "_" + params.hashCode();
        }
    }

    /**
     * 放入缓存
     *
     * @param cacheKey 缓存key
     * @param xmlRows XML行数据
     */
    public void put(String cacheKey, List<Map<String, String>> xmlRows)
    {
        cache.put(cacheKey, new CacheEntry(xmlRows));
        log.info("缓存XML数据: key={}, rowCount={}", cacheKey, xmlRows.size());

        // 清理过期缓存
        cleanExpiredCache();
    }

    /**
     * 从缓存获取
     *
     * @param cacheKey 缓存key
     * @return XML行数据，如果不存在或已过期则返回null
     */
    public List<Map<String, String>> get(String cacheKey)
    {
        CacheEntry entry = cache.get(cacheKey);

        if (entry == null)
        {
            log.debug("缓存未命中: key={}", cacheKey);
            return null;
        }

        if (entry.isExpired())
        {
            log.info("缓存已过期: key={}", cacheKey);
            cache.remove(cacheKey);
            return null;
        }

        log.info("缓存命中: key={}, rowCount={}", cacheKey, entry.getXmlRows().size());
        return entry.getXmlRows();
    }

    /**
     * 清除指定缓存
     *
     * @param cacheKey 缓存key
     */
    public void remove(String cacheKey)
    {
        cache.remove(cacheKey);
        log.info("清除缓存: key={}", cacheKey);
    }

    /**
     * 清理所有过期的缓存
     */
    private void cleanExpiredCache()
    {
        int beforeSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterSize = cache.size();

        if (beforeSize > afterSize)
        {
            log.info("清理过期缓存: 清理前={}, 清理后={}", beforeSize, afterSize);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear()
    {
        cache.clear();
        log.info("清空所有缓存");
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存项数量
     */
    public int size()
    {
        return cache.size();
    }
}
