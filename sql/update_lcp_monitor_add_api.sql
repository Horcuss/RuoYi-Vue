-- =============================================
-- 更新LCP监控配置，添加API数据源测试
-- 在现有配置基础上添加API字段
-- =============================================

-- 更新 LCP 监控配置，添加 API 地址和 API 数据源字段
UPDATE monitor_config 
SET config_json = '{
  "apiUrl": "https://jsonplaceholder.typicode.com/users/1",
  "formItems": [
    {"label": "LCP品名", "prop": "lcpName", "value": null},
    {"label": "设备", "prop": "device", "value": null}
  ],
  "widgetItems": [
    {"text": "LCP加工标准", "href": "#", "icon": "el-icon-link", "target": "_blank", "type": "primary"},
    {"text": "LCP本作业标准", "href": "#", "icon": "el-icon-notebook-2", "target": "_blank", "type": "primary"},
    {"text": "LCP异常处置方法", "href": "#", "icon": "el-icon-collection-tag", "target": "_blank", "type": "primary"},
    {"text": "指示书/评价依赖书", "href": "#", "icon": "el-icon-data-line", "target": "_blank", "type": "primary"}
  ],
  "descItems": [
    {
      "label": "作业员",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT operator_name FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device LIMIT 1"
    },
    {
      "label": "班次",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT shift FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device LIMIT 1"
    },
    {
      "label": "API测试-用户名",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "name"
    },
    {
      "label": "API测试-邮箱",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "email"
    },
    {
      "label": "API测试-城市",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "address.city"
    },
    {
      "label": "API测试-纬度",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "address.geo.lat"
    }
  ],
  "remarkItems": [
    {
      "title": "备注1",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT remark1 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device LIMIT 1"
    },
    {
      "title": "API测试-公司信息",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "company.name"
    }
  ],
  "tableConfigs": [
    {
      "rowHeader": "印刷检查",
      "rows": [
        {
          "rowType": "simple",
          "projectName": "LCP处置项次",
          "unit": "m/min",
          "dataSource": "database",
          "displayType": "direct",
          "expression": "SELECT lcp_disposal_times FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device LIMIT 1"
        },
        {
          "rowType": "simple",
          "projectName": "API测试-电话",
          "unit": "",
          "dataSource": "api",
          "displayType": "direct",
          "expression": "phone"
        }
      ]
    },
    {
      "rowHeader": "面积检查",
      "rows": [
        {
          "rowType": "complex",
          "projectName": "基准精度/尺度",
          "subRows": [
            {
              "subName": "L",
              "unit": "um",
              "dataSource": "database",
              "displayType": "direct",
              "expression": "SELECT accuracy_l FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device LIMIT 1"
            },
            {
              "subName": "W",
              "unit": "um",
              "dataSource": "database",
              "displayType": "direct",
              "expression": "SELECT accuracy_w FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device LIMIT 1"
            }
          ]
        },
        {
          "rowType": "complex",
          "projectName": "API测试-坐标",
          "subRows": [
            {
              "subName": "纬度",
              "unit": "度",
              "dataSource": "api",
              "displayType": "direct",
              "expression": "address.geo.lat"
            },
            {
              "subName": "经度",
              "unit": "度",
              "dataSource": "api",
              "displayType": "direct",
              "expression": "address.geo.lng"
            }
          ]
        }
      ]
    }
  ]
}',
update_by = 'admin',
update_time = NOW()
WHERE config_key = 'lcpMonitor';

-- 提交
COMMIT;

-- 查询验证
SELECT config_key, config_name, 
       CASE WHEN config_json LIKE '%apiUrl%' THEN '已添加API地址' ELSE '未添加API地址' END as api_status,
       update_time
FROM monitor_config 
WHERE config_key = 'lcpMonitor';

