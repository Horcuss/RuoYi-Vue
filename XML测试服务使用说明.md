# XML测试服务使用说明

## 简介
这是一个简单的XML数据存储和获取服务，用于测试监控系统的XML数据源功能。

## API接口

### 1. 存储XML数据
**接口：** `POST /api/xmltest/store/{key}`

**说明：** 使用Postman或其他工具将XML数据存储到服务器

**使用步骤：**
1. 打开Postman
2. 创建POST请求：`http://localhost:8080/api/xmltest/store/test_gp_001`
3. 在Headers中设置：`Content-Type: application/xml`
4. 在Body中选择"raw"，粘贴XML内容
5. 发送请求

**示例XML（GP项目）：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
  <status>success</status>
  <data>
    <row>
      <item>001</item>
      <item>设备A</item>
      <item>111</item>
      <item>型号X</item>
      <item>222</item>
      <item>225</item>
    </row>
    <row>
      <item>001</item>
      <item>设备B</item>
      <item>112</item>
      <item>型号Y</item>
      <item>223</item>
      <item>226</item>
    </row>
    <row>
      <item>001</item>
      <item>设备C</item>
      <item>113</item>
      <item>型号Z</item>
      <item>224</item>
      <item>227</item>
    </row>
  </data>
</response>
```

**示例XML（非GP项目）：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
  <status>success</status>
  <data>
    <row>
      <item>002</item>
      <item>参数1</item>
      <item>331</item>
      <item>参数2</item>
      <item>332</item>
    </row>
    <row>
      <item>002</item>
      <item>参数3</item>
      <item>333</item>
      <item>参数4</item>
      <item>334</item>
    </row>
  </data>
</response>
```

---

### 2. 获取XML数据
**接口：** `POST /api/xmltest/{key}`

**说明：** 监控系统会调用此接口获取XML数据

**在监控配置中使用：**
- API URL配置为：`http://localhost:8080/api/xmltest/test_gp_001`
- 监控系统会自动调用并解析返回的XML数据

---

### 3. 查看已存储的KEY列表
**接口：** `GET /api/xmltest/list`

**示例：**
```
GET http://localhost:8080/api/xmltest/list
```

**返回：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
  <status>success</status>
  <keys>
    <key>test_gp_001</key>
    <key>test_nongp_001</key>
  </keys>
</response>
```

---

### 4. 删除XML数据
**接口：** `DELETE /api/xmltest/{key}`

**示例：**
```
DELETE http://localhost:8080/api/xmltest/test_gp_001
```

---

## 使用流程

### 步骤1：存储测试数据
使用Postman发送XML数据到服务器：
```
POST http://localhost:8080/api/xmltest/store/my_test_data
Content-Type: application/xml

<你的XML内容>
```

### 步骤2：在监控配置中使用
在监控管理界面配置API数据源：
- **API URL：** `http://localhost:8080/api/xmltest/my_test_data`
- **请求方式：** POST
- **其他配置：** 按照GP监控需求配置

### 步骤3：测试
在CommonMonitor页面输入查询条件，系统会自动调用XML测试接口并展示数据。

---

## 数据存储位置
XML数据文件存储在项目根目录的 `xml_test_data` 文件夹中：
```
RuoYi-Vue/
  └── xml_test_data/
      ├── test_gp_001.xml
      ├── test_nongp_001.xml
      └── my_test_data.xml
```

---

## 注意事项
1. XML文件存储在服务器本地，重启服务后数据不会丢失
2. KEY只能包含字母、数字、下划线和中划线
3. 存储的XML必须是合法的XML格式
4. 建议使用有意义的KEY名称，如：`test_gp_device`、`test_nongp_param`等
