# LCP监控配置示例说明

## 📁 文件清单

1. **lcp_monitor_config.json** - LCP监控页面配置JSON
2. **test_lcp_clean.xml** - LCP监控测试用XML数据
3. **README_LCP.md** - 本说明文档

---

## 📊 数据结构说明

### GP格式特征（procConditionGroup="02"）

```
item_0: procConditionGroup（固定值"02"）
item_1: 品名（固定值）
item_2: 工程系列（固定值）
item_3: 大分类cd（固定值）
item_4: 中分类cd（固定值）
item_5: ✅ 加工条件种cd（用于区分数据类型）
item_6: ✅ 数据开始位置（连续，无分隔符）
```

---

## 🔑 Expression表达式格式

格式：`typeCd;multiKey;seq`

**示例：**
- `"001;2;4"` 表示：
  - `001` - 加工条件种cd
  - `2` - multiKey（多key区分）
  - `4` - seq序号（数据位置 = item_5 + seq = item_9）

---

## 📋 配置字段说明

### formItems（表单输入项）

```json
{
  "label": "LCP品名",        // 显示标签
  "prop": "lcpName",         // 字段名
  "type": "input",           // 类型：input/select
  "dataSource": "xml",       // 数据源（仅select）
  "expression": "001;2;1",   // 表达式（仅xml数据源）
  "options": []              // 选项列表（动态填充）
}
```

### descItems（基础信息项）

```json
{
  "label": "温度",           // 显示标签
  "dataSource": "xml",       // 数据源：xml/database/api
  "displayType": "direct",   // 显示类型：direct/computed
  "valueKey": "temperature", // 变量名（用于前端引用）
  "expression": "001;2;5"    // xml: typeCd;multiKey;seq
                             // database: SQL语句
                             // api: json字段路径
}
```

### tableConfigs（表格配置）

**简单行（simple）：**
```json
{
  "rowType": "simple",
  "projectName": "LCP处置项次",
  "unit": "",
  "dataSource": "xml",
  "displayType": "direct",
  "valueKey": "disposalTimes",
  "expression": "002;1;1"
}
```

**复杂行（complex，带子项）：**
```json
{
  "rowType": "complex",
  "projectName": "基准精度/尺度",
  "subRows": [
    {
      "subName": "L",
      "unit": "um",
      "dataSource": "database",
      "displayType": "direct",
      "valueKey": "precisionL",
      "expression": "SELECT precision_l FROM lcp_dimension WHERE ..."
    }
  ]
}
```

---

## 🔄 数据流程示例

### 1. 用户输入LCP品名

```
用户输入: "LCP-2024-A"
按回车 → 调用 getSelectOptions API
```

### 2. 后端获取下拉框选项

```
expression: "001;2;1" → 工程系列下拉框

1. 遍历所有 cd=001 的 row
2. 提取 item_6 位置的值（cdIndex + seq = 5 + 1）
3. 结果：["工程系列A", "工程系列B"]
4. 返回给前端填充下拉框
```

### 3. 用户选择并查询

```
用户选择:
- 工程系列: "工程系列A"
- 版本: "V1.0"

点击查询 → 调用 getMonitorDataWithParams API
```

### 4. 后端提取数据

```
expression: "001;2;5" → 温度字段

1. 找到匹配的 row（cd=001, 工程系列=工程系列A, 版本=V1.0）
2. 计算位置: item_5 + 5 = item_10
3. 提取值: "25.5"
4. 返回给前端展示
```

---

## 🎯 XML数据说明

### 加工条件种cd分类

- **001** - 基本信息（品名、工程系列、型号、版本、温度、速度等）
- **002** - 印刷检查（处置项次、印刷方式、LCP量等）
- **003** - 工艺说明（备注信息）
- **004** - 其他检查项

### 数据行示例

```xml
<row>
  <item>02</item>           <!-- procConditionGroup -->
  <item>LCP-2024-A</item>   <!-- 品名 -->
  <item>工程系列X</item>     <!-- 工程系列 -->
  <item>001</item>          <!-- 大分类cd -->
  <item>N01</item>          <!-- 中分类cd -->
  <item>001</item>          <!-- 加工条件种cd -->
  <item>LCP-2024-A</item>   <!-- seq=1: 品名 -->
  <item>工程系列A</item>     <!-- seq=2, multiKey=1: 工程系列 -->
  <item>A型</item>          <!-- seq=2, multiKey=2: 型号 -->
  <item>A1型</item>         <!-- seq=2, multiKey=3: 类型 -->
  <item>V1.0</item>         <!-- seq=2, multiKey=4: 版本 -->
  <item>25.5</item>         <!-- seq=2, multiKey=5: 温度 -->
  <item>60</item>           <!-- seq=2, multiKey=6: 速度 -->
</row>
```

---

## 💻 使用方法

### 1. 导入配置到数据库

```sql
INSERT INTO monitor_config (config_key, config_name, config_json, status, del_flag, create_by)
VALUES (
  'lcpMonitor',
  'LCP监控',
  '此处粘贴 lcp_monitor_config.json 的内容',
  '0',
  '0',
  'admin'
);
```

### 2. 配置XML API地址

在 `lcp_monitor_config.json` 中修改：
```json
{
  "apiUrl": "http://your-server:port/api/getLcpXmlData"
}
```

### 3. 前端访问

```
http://localhost/lcpMonitor
```

---

## 📝 注意事项

1. **GP格式**：数据连续，无分隔符，cdIndex=5，dataStartIndex=6
2. **非GP格式**：有分隔符，cdIndex=3，dataStartIndex=4
3. **multiKey**：当一个cd有多个相关字段时使用（如：工程系列、型号、类型、版本都属于cd=001）
4. **seq序号**：从1开始，表示相对于cdIndex的偏移量
5. **valueKey**：必须唯一，用于前端变量引用和数据缓存

---

## 🔍 调试技巧

### 查看XML解析结果

在后端日志中搜索：
```
解析XML，共 X 个row
清理非GP项目XML分隔符
```

### 查看数据提取

```
找到匹配的rows，共 X 个cd
从XML提取数据完成，共 X 个字段
```

### 验证expression

```java
// expression = "001;2;4"
// GP项目: item_5 + 4 = item_9
// 非GP项目: item_3 + 4 = item_7
```

---

## 📞 技术支持

如有问题，请检查：
1. procConditionGroup 是否正确（"02"=GP，"01"=非GP）
2. majorClassCd 和 minorClassCd 是否匹配
3. expression 中的 typeCd 是否存在于XML中
4. seq 序号是否超出XML数据范围

---

**创建日期：** 2025-01-14
**版本：** v1.0
