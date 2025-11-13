# 监控配置示例文件说明

## 📁 文件清单

### **配置文件（JSON）**
1. **lcp_monitor_config_correct.json** - ✅ LCP监控正确配置（修正版）
2. **lcp_monitor_config.json** - ❌ 旧版（API expression格式错误，已废弃）

### **测试数据（XML）**
1. **test_gp_001_with_multikey.xml** - ✅ GP测试数据（含multiKey区分说明）
2. **test_lcp_clean.xml** - ✅ LCP测试数据（干净注释版）
3. **test_lcp_001.xml** - 原始LCP测试数据
4. **test_gp_001.xml** - 原始GP测试数据

### **说明文档（Markdown）**
1. **README.md** - 本文件（总览）
2. **README_LCP.md** - LCP监控详细说明
3. **README_API_EXPRESSION.md** - ⭐ API数据源Expression格式说明（重要！）

---

## ⭐ **重要发现：API数据源格式**

### ❌ **之前的错误理解**
```json
{
  "dataSource": "api",
  "expression": "temperature"    // ❌ 错误：以为是字段名
}
```

### ✅ **正确的格式**
```json
{
  "dataSource": "api",
  "expression": "001;3;5",       // ✅ 正确：typeCd;multiKey;seq
  "valueKey": "temperature"
}
```

**说明：** API数据源的expression使用与XML相同的格式！

---

## 🎯 **multiKey含义区分**

| multiKey | 用途 | 使用场景 | 示例 |
|----------|------|---------|------|
| **2** | 下拉框选项 | formItems中的select | `"001;2;2"` |
| **3** | 显示字段 | descItems、remarkItems、tableConfigs | `"001;3;5"` |

**为什么要区分？**
- 数据隔离：下拉框数据和显示数据分开存储
- 灵活配置：同一个cd可以有多组不同用途的数据
- 后端处理：根据multiKey区分处理逻辑

---

## 📊 **GP vs LCP 配置对比**

### **GP监控（用户提供的正确示例）**

```json
{
  "procConditionGroup": "02",
  "formItems": [
    {
      "label": "设备类型",
      "type": "select",
      "dataSource": "xml",
      "expression": "001;2;2"    // multiKey=2 下拉框
    }
  ],
  "descItems": [
    {
      "label": "温度(API-Direct)",
      "dataSource": "api",
      "expression": "001;3;5",   // multiKey=3 显示字段
      "valueKey": "temperature"
    }
  ]
}
```

### **LCP监控（修正后）**

```json
{
  "procConditionGroup": "02",
  "formItems": [
    {
      "label": "工程系列",
      "type": "select",
      "dataSource": "xml",
      "expression": "001;2;1"    // multiKey=2 下拉框
    }
  ],
  "descItems": [
    {
      "label": "温度",
      "dataSource": "api",
      "expression": "001;3;6",   // multiKey=3 显示字段
      "valueKey": "temperature"
    }
  ]
}
```

---

## 🔍 **Expression格式完整说明**

### **格式：`typeCd;multiKey;seq`**

```
"001;3;5" 的含义：
├─ 001: 加工条件种cd（typeCd）
├─ 3: multiKey（用途区分：2=下拉框，3=显示）
└─ 5: seq（序号，数据位置 = cdIndex + seq）
```

### **数据提取位置计算**

**GP项目（cdIndex=5）：**
```
expression = "001;3;5"
数据位置 = item_5 + 5 = item_10
```

**非GP项目（cdIndex=3）：**
```
expression = "001;3;5"
数据位置 = item_3 + 5 = item_8
```

---

## 📋 **三种数据源对比**

| 数据源 | Expression格式 | 示例 | 说明 |
|--------|---------------|------|------|
| **xml** | `typeCd;multiKey;seq` | `"001;2;2"` | 从XML提取 |
| **api** | `typeCd;multiKey;seq` | `"001;3;5"` | 从API返回的XML提取 |
| **database** | SQL语句 | `"SELECT ... WHERE name = :gpName"` | 从数据库查询 |
| **api-computed** | 计算表达式 | `"(temperature + humidity) / 2"` | 引用其他valueKey计算 |

---

## 🚀 **快速开始**

### **1. 查看正确的配置示例**
```bash
# LCP监控配置（正确版本）
cat lcp_monitor_config_correct.json

# 用户提供的GP监控配置（参考标准）
# （见用户提供的JSON）
```

### **2. 查看测试XML数据**
```bash
# GP测试数据（含multiKey详细说明）
cat test_gp_001_with_multikey.xml

# LCP测试数据
cat test_lcp_clean.xml
```

### **3. 阅读详细文档**
```bash
# API数据源格式说明（必读！）
cat README_API_EXPRESSION.md

# LCP监控详细说明
cat README_LCP.md
```

---

## 📊 **XML数据结构示例**

### **multiKey=2和multiKey=3的数据分布**

```xml
<row>
  <item>02</item>           <!-- item_0: procConditionGroup -->
  <item>品名</item>         <!-- item_1-4: 固定字段 -->
  <item>001</item>          <!-- item_5: cd -->

  <!-- ===== multiKey=2: 下拉框数据 ===== -->
  <item>数据A</item>        <!-- item_6: seq=1 -->
  <item>设备A</item>        <!-- item_7: seq=2 设备类型（下拉框） -->
  <item>型号A1</item>       <!-- item_8: seq=3 设备型号（下拉框） -->
  <item>V1.0</item>         <!-- item_9: seq=4 设备版本（下拉框） -->

  <!-- ===== multiKey=3: 显示字段数据 ===== -->
  <item>数据A</item>        <!-- item_10: seq=1 -->
  <item>设备A</item>        <!-- item_11: seq=2 设备类型（显示） -->
  <item>型号A1</item>       <!-- item_12: seq=3 设备型号（显示） -->
  <item>V1.0</item>         <!-- item_13: seq=4 设备版本（显示） -->
  <item>25.5</item>         <!-- item_14: seq=5 温度 -->
  <item>60</item>           <!-- item_15: seq=6 湿度 -->
</row>
```

---

## 🎯 **完整数据流程**

### **1. 用户输入并获取下拉框选项**

```
用户输入: "品名1111"
按回车 → getSelectOptions

后端处理:
1. expression = "001;2;2" （multiKey=2）
2. 查找所有 cd=001 的row
3. 提取 item_7（cdIndex=5 + seq=2）
4. 去重: ["设备A", "设备B", "设备C"]
5. 返回给前端填充下拉框
```

### **2. 用户选择并查询数据**

```
用户选择:
- 设备类型: "设备A"
- 设备版本: "V1.0"

点击查询 → getMonitorDataWithParams

后端处理:
1. expression = "001;3;5" （multiKey=3）
2. 根据选择值找到匹配的row
3. 提取 item_14（cdIndex=5 + seq=5）
4. 值 = "25.5"
5. 返回 { "temperature": "25.5" }
```

### **3. 计算字段**

```
配置: expression = "(temperature + humidity) / 2"

后端处理:
1. 获取 temperature = 25.5
2. 获取 humidity = 60
3. 计算 (25.5 + 60) / 2 = 42.75
4. 返回 { "avgTempHumidity": "42.75" }
```

---

## ⚠️ **常见错误**

### **1. API数据源使用字段名**
```json
// ❌ 错误
{
  "dataSource": "api",
  "expression": "temperature"
}

// ✅ 正确
{
  "dataSource": "api",
  "expression": "001;3;5",
  "valueKey": "temperature"
}
```

### **2. multiKey使用错误**
```json
// ❌ 错误：显示字段使用multiKey=2
{
  "dataSource": "api",
  "expression": "001;2;5"    // 应该用3不是2
}

// ✅ 正确：显示字段使用multiKey=3
{
  "dataSource": "api",
  "expression": "001;3;5"
}
```

### **3. seq序号计算错误**
```
// 想取item_14的数据
// cdIndex=5, seq应该=9 (5+9=14)

❌ 错误: "001;3;5"  → item_10
✅ 正确: "001;3;9"  → item_14
```

---

## 🔧 **调试技巧**

### **1. 检查expression计算**
```
expression = "001;3;5"
GP项目: item_5 + 5 = item_10
非GP项目: item_3 + 5 = item_8
```

### **2. 查看后端日志**
```
解析XML，共 X 个row
清理非GP项目XML分隔符
找到匹配的rows，共 X 个cd
从XML提取数据完成，共 X 个字段
```

### **3. 验证multiKey**
```
下拉框数据 → multiKey=2
显示字段 → multiKey=3
```

---

## 📞 **技术支持**

遇到问题时检查清单：

- [ ] procConditionGroup 是否正确（"02"=GP，"01"=非GP）
- [ ] API数据源的expression是否使用 `typeCd;multiKey;seq` 格式
- [ ] multiKey是否正确（2=下拉框，3=显示）
- [ ] seq序号计算是否正确
- [ ] valueKey是否唯一
- [ ] computed表达式中引用的变量是否已定义

---

## 📝 **版本历史**

- **v1.0** (2025-01-14 初版) - 创建初始配置
- **v1.1** (2025-01-14 修正) - 发现并修正API数据源expression格式错误
  - ❌ 之前：`"expression": "temperature"`
  - ✅ 现在：`"expression": "001;3;5"`

---

**最后更新：** 2025-01-14
**维护者：** Claude Code Assistant
