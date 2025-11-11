package com.ruoyi.monitor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * XML测试服务Controller
 * 提供简单的XML存储和获取功能
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@RestController
@RequestMapping("/api/xmltest")
public class XmlTestController
{
    private static final Logger log = LoggerFactory.getLogger(XmlTestController.class);

    // XML数据存储目录
    private static final String STORAGE_DIR = "xml_test_data";

    /**
     * 存储XML数据
     * 使用Postman发送：POST http://localhost:8080/api/xmltest/store/{key}
     * Body选择raw/XML，直接粘贴XML内容
     *
     * @param key 存储的标识KEY
     * @param xmlData XML数据内容
     * @return 成功消息
     */
    @PostMapping(value = "/store/{key}", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_XML_VALUE})
    public String storeXml(@PathVariable("key") String key,
                           @RequestBody String xmlData)
    {
        try
        {
            // 确保存储目录存在
            File dir = new File(STORAGE_DIR);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            // 将XML数据写入文件
            Path filePath = Paths.get(STORAGE_DIR, key + ".xml");
            Files.write(filePath, xmlData.getBytes(StandardCharsets.UTF_8));

            log.info("XML数据已存储: key={}, size={} bytes", key, xmlData.length());

            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<response>\n" +
                   "  <status>success</status>\n" +
                   "  <message>XML数据已成功存储，KEY: " + key + "</message>\n" +
                   "</response>";
        }
        catch (IOException e)
        {
            log.error("存储XML数据失败: key={}", key, e);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<response>\n" +
                   "  <status>error</status>\n" +
                   "  <message>存储失败: " + e.getMessage() + "</message>\n" +
                   "</response>";
        }
    }

    /**
     * 获取XML数据
     * 监控系统会调用：POST http://localhost:8080/api/xmltest/{key}
     *
     * @param key 存储的标识KEY
     * @param requestBody 请求体（可选，用于接收监控系统的请求参数）
     * @return XML数据
     */
    @PostMapping(value = "/{key}", produces = MediaType.APPLICATION_XML_VALUE)
    public String getXml(@PathVariable("key") String key,
                         @RequestBody(required = false) String requestBody)
    {
        try
        {
            // 读取XML文件
            Path filePath = Paths.get(STORAGE_DIR, key + ".xml");

            if (!Files.exists(filePath))
            {
                log.warn("XML数据不存在: key={}", key);
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                       "<response>\n" +
                       "  <status>error</status>\n" +
                       "  <message>未找到KEY为 " + key + " 的XML数据，请先使用/store接口存储</message>\n" +
                       "</response>";
            }

            String xmlData = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            log.info("返回XML数据: key={}, size={} bytes", key, xmlData.length());

            return xmlData;
        }
        catch (IOException e)
        {
            log.error("读取XML数据失败: key={}", key, e);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<response>\n" +
                   "  <status>error</status>\n" +
                   "  <message>读取失败: " + e.getMessage() + "</message>\n" +
                   "</response>";
        }
    }

    /**
     * 列出所有已存储的XML KEY
     * GET http://localhost:8080/api/xmltest/list
     *
     * @return 已存储的KEY列表
     */
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_XML_VALUE)
    public String listKeys()
    {
        try
        {
            File dir = new File(STORAGE_DIR);
            if (!dir.exists() || !dir.isDirectory())
            {
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                       "<response>\n" +
                       "  <status>success</status>\n" +
                       "  <keys></keys>\n" +
                       "</response>";
            }

            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));

            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<response>\n");
            xml.append("  <status>success</status>\n");
            xml.append("  <keys>\n");

            if (files != null)
            {
                for (File file : files)
                {
                    String key = file.getName().replace(".xml", "");
                    xml.append("    <key>").append(key).append("</key>\n");
                }
            }

            xml.append("  </keys>\n");
            xml.append("</response>");

            return xml.toString();
        }
        catch (Exception e)
        {
            log.error("列出XML KEY失败", e);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<response>\n" +
                   "  <status>error</status>\n" +
                   "  <message>列表失败: " + e.getMessage() + "</message>\n" +
                   "</response>";
        }
    }

    /**
     * 删除存储的XML数据
     * DELETE http://localhost:8080/api/xmltest/{key}
     *
     * @param key 存储的标识KEY
     * @return 删除结果
     */
    @DeleteMapping(value = "/{key}", produces = MediaType.APPLICATION_XML_VALUE)
    public String deleteXml(@PathVariable("key") String key)
    {
        try
        {
            Path filePath = Paths.get(STORAGE_DIR, key + ".xml");

            if (!Files.exists(filePath))
            {
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                       "<response>\n" +
                       "  <status>error</status>\n" +
                       "  <message>KEY为 " + key + " 的XML数据不存在</message>\n" +
                       "</response>";
            }

            Files.delete(filePath);
            log.info("XML数据已删除: key={}", key);

            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<response>\n" +
                   "  <status>success</status>\n" +
                   "  <message>XML数据已成功删除，KEY: " + key + "</message>\n" +
                   "</response>";
        }
        catch (IOException e)
        {
            log.error("删除XML数据失败: key={}", key, e);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<response>\n" +
                   "  <status>error</status>\n" +
                   "  <message>删除失败: " + e.getMessage() + "</message>\n" +
                   "</response>";
        }
    }
}
