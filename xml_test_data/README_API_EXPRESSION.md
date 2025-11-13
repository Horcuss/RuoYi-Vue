# API数据源Expression格式说明

## 🔑 **核心发现**

API数据源的expression **不是简单的字段名**，而是使用 **XML路径格式**：`typeCd;multiKey;seq`

---

## 📊 **Expression格式对比**

### **XML数据源**
```json
{
  "dataSource": "xml",
  "expression": "001;2;2"
}
```
→ 从XML的cd=001行中，提取multiKey=2、seq=2位置的数据

### **API数据源（正确格式）**
```json
{
  "dataSource": "api",
  "expression": "001;3;5"
}
```
→ 从API返回的XML中，cd=001行，multiKey=3、seq=5位置的数据

### **Database数据源**
```json
{
  "dataSource": "database",
  "expression": "SELECT COUNT(*) FROM test_table WHERE name = :gpName"
}
```
→ SQL语句

---

## 🎯 **multiKey的用途区分**

根据GP配置示例，multiKey有明确分工：

### **multiKey = 2：下拉框数据源**
```json
{
  "label": "设备类型",
  "prop": "deviceType",
  "type": "select",
  "dataSource": "xml",
  "expression": "001;2;2"   // multiKey=2 用于下拉框
}
```

### **multiKey = 3：显示字段数据源**
```json
{
  "label": "温度(API-Direct)",
  "dataSource": "api",
  "displayType": "direct",
  "expression": "001;3;5",   // multiKey=3 用于显示
  "valueKey": "temperature"
}
```

---

## 📋 **完整示例对比**

### **GP监控配置（用户提供）**

```json
{
  "formItems": [
    {
      "label": "设备类型",
      "prop": "deviceType",
      "type": "select",
      "dataSource": "xml",
      "expression": "001;2;2"    // cd=001, multiKey=2, seq=2
    },
    {
      "label": "工序",
      "prop": "process",
      "type": "select",
      "dataSource": "xml",
      "expression": "002;2;2"    // cd=002, multiKey=2, seq=2
    }
  ],
  "descItems": [
    {
      "label": "温度(API-Direct)",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "001;3;5",   // cd=001, multiKey=3, seq=5
      "valueKey": "temperature"
    },
    {
      "label": "速度(cd=002)",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "002;3;5",   // cd=002, multiKey=3, seq=5
      "valueKey": "speed"
    },
    {
      "label": "质检标准(cd=003无下拉框)",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "003;3;2",   // cd=003, multiKey=3, seq=2
      "valueKey": "qcStandard"
    }
  ]
}
```

---

## 🔍 **数据提取逻辑**

### **1. 下拉框数据提取（multiKey=2）**

**配置：**
```json
{
  "expression": "001;2;2"
}
```

**后端逻辑：**
```java
// 1. 查找所有 cd=001 的row
// 2. 从每个row中提取 item_5 + 2 = item_7 的值
// 3. 去重后返回作为下拉框选项
```

**XML示例：**
```xml
<row>
  <item>02</item>         <!-- item_0 -->
  <item>品名</item>       <!-- item_1 -->
  <item>系列</item>       <!-- item_2 -->
  <item>001</item>        <!-- item_3 -->
  <item>N01</item>        <!-- item_4 -->
  <item>001</item>        <!-- item_5: cd -->
  <item>数据1</item>      <!-- item_6: seq=1 -->
  <item>设备A</item>      <!-- item_7: seq=2 ✅ 提取这个 -->
  <item>数据3</item>      <!-- item_8: seq=3 -->
</row>
```

### **2. 显示字段数据提取（multiKey=3）**

**配置：**
```json
{
  "expression": "001;3;5",
  "valueKey": "temperature"
}
```

**后端逻辑：**
```java
// 1. 根据用户选择的下拉框值，找到匹配的row（cd=001）
// 2. 从该row中提取 item_5 + 5 = item_10 的值
// 3. 返回给前端，存储在变量 temperature 中
```

**XML示例：**
```xml
<row>
  <item>02</item>
  <item>品名</item>
  <item>系列</item>
  <item>001</item>
  <item>N01</item>
  <item>001</item>        <!-- item_5: cd -->
  <item>数据1</item>      <!-- item_6: seq=1 -->
  <item>设备A</item>      <!-- item_7: seq=2 -->
  <item>数据3</item>      <!-- item_8: seq=3 -->
  <item>数据4</item>      <!-- item_9: seq=4 -->
  <item>25.5</item>       <!-- item_10: seq=5 ✅ 提取这个作为温度 -->
</row>
```

---

## 💡 **为什么要区分multiKey？**

**推测原因：**
1. **数据隔离** - multiKey=2的数据用于下拉框，multiKey=3的数据用于显示，避免混淆
2. **灵活配置** - 同一个cd可以有多组不同用途的数据
3. **后端处理** - 后端可以根据multiKey区分处理逻辑

---

## 🎯 **配置规范**

### ✅ **正确的配置**

```json
{
  "label": "温度",
  "dataSource": "api",
  "displayType": "direct",
  "expression": "001;3;5",      // ✅ 正确：typeCd;multiKey;seq
  "valueKey": "temperature"
}
```

### ❌ **错误的配置**

```json
{
  "label": "温度",
  "dataSource": "api",
  "displayType": "direct",
  "expression": "temperature",  // ❌ 错误：不是字段名
  "valueKey": "temperature"
}
```

---

## 📊 **三种数据源Expression对比表**

| 数据源 | Expression格式 | 示例 | 说明 |
|--------|---------------|------|------|
| **xml** | `typeCd;multiKey;seq` | `"001;2;2"` | 从XML提取 |
| **api** | `typeCd;multiKey;seq` | `"001;3;5"` | 从API返回的XML提取 |
| **database** | SQL语句 | `"SELECT ... WHERE name = :gpName"` | 从数据库查询 |
| **api-computed** | 表达式 | `"(temperature + humidity) / 2"` | 计算式，引用其他valueKey |

---

## 🔄 **完整数据流程**

### **步骤1：用户输入品名**
```
用户输入: "品名1111"
按回车 → getSelectOptions
```

### **步骤2：获取下拉框选项**
```
后端处理 expression="001;2;2"（multiKey=2）:
1. 找到所有 cd=001 的row
2. 提取 item_7（5+2）的值
3. 去重：["设备A", "设备B", "设备C"]
4. 返回给前端填充下拉框
```

### **步骤3：用户选择并查询**
```
用户选择:
- 设备类型: "设备A"
- 工序: "印刷"

点击查询 → getMonitorDataWithParams
```

### **步骤4：提取显示数据**
```
后端处理 expression="001;3;5"（multiKey=3）:
1. 根据选择值，找到匹配的row（cd=001, 设备类型=设备A）
2. 提取 item_10（5+5）的值
3. 值="25.5"
4. 返回 { "temperature": "25.5" }
```

### **步骤5：计算字段**
```
配置: expression="(temperature + humidity) / 2"
后端处理:
1. 获取 temperature=25.5
2. 获取 humidity=60
3. 计算 (25.5 + 60) / 2 = 42.75
4. 返回 { "avgTempHumidity": "42.75" }
```

---

## 📝 **注意事项**

1. ⚠️ **API数据源必须用 `typeCd;multiKey;seq` 格式**，不能用字段名
2. ⚠️ **multiKey=2 通常用于下拉框**，multiKey=3 用于显示字段
3. ⚠️ **valueKey 必须唯一**，用于变量引用
4. ⚠️ **computed类型的expression** 是计算式，引用其他valueKey的变量名
5. ⚠️ **seq从1开始**，表示相对cdIndex的偏移

---

## 🔧 **调试技巧**

### **检查expression是否正确**

```
expression = "001;3;5"
GP项目: item_5 + 5 = item_10
非GP项目: item_3 + 5 = item_8
```

### **检查是否取到数据**

后端日志搜索：
```
从XML提取数据完成，共 X 个字段
valueKey=temperature, value=25.5
```

### **检查multiKey是否匹配**

```
下拉框: multiKey=2
显示字段: multiKey=3
确保配置中使用正确的multiKey
```

---

**更新日期：** 2025-01-14
**版本：** v1.1（修正API数据源expression格式）
