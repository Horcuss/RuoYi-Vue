-- ========================================
-- 插入 LCP 监控配置到 monitor_config 表
-- ========================================

USE `ry_monitor`;

-- 插入 LCP 监控配置
INSERT INTO `monitor_config` (`config_key`, `config_name`, `config_json`, `status`, `del_flag`, `create_by`, `remark`) 
VALUES ('lcpMonitor', 'LCP监控', '{
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
      "label": "作业员1",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT operator1 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "班次1",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT shift1 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "第次1",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT batch1 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "生产日期1",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT product_date1 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "产品型号1",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT product_model1 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "作业员2",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT operator2 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "班次2",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT shift2 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "第次2",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT batch2 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "生产日期2",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT product_date2 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "label": "产品型号2",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT product_model2 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    }
  ],
  "remarkItems": [
    {
      "title": "备注1",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT remark1 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    },
    {
      "title": "备注2",
      "dataSource": "database",
      "displayType": "direct",
      "expression": "SELECT remark2 FROM lcp_production_data WHERE lcp_name = :lcpName AND device = :device"
    }
  ],
  "tableConfigs": [
    {
      "title": "印刷检查、面积检查",
      "header": [
        {"label": "项目名", "style": {"width": "45%"}},
        {"label": "条件", "style": {"width": "40%"}}
      ],
      "body": {
        "rowHeader": "印刷检查、面积检查",
        "rows": [
          {
            "labelItems": [{"content": "LCP处置项次", "colSpan": 3}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT lcp_item FROM lcp_print_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          },
          {
            "labelItems": [{"content": "三温炉时间"}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT furnace_time FROM lcp_print_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          },
          {
            "labelItems": [{"content": "速度", "colSpan": 2}, {"content": "m / min"}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT speed FROM lcp_print_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          },
          {
            "labelItems": [{"content": "炉温（开/关/开）"}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT furnace_temp FROM lcp_print_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          },
          {
            "labelItems": [{"content": "lcp量", "colSpan": 2}, {"content": "um"}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT lcp_amount FROM lcp_print_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          },
          {
            "labelItems": [
              {"content": "基准精度/尺度", "rowSpan": 2, "backgroundColor": "unset"},
              {"content": "L", "backgroundColor": "unset"},
              {"content": "um", "backgroundColor": "sunset"}
            ],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT precision_l FROM lcp_print_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          },
          {
            "labelItems": [
              {"content": "w", "backgroundColor": "unset"},
              {"content": "um", "backgroundColor": "sunset"}
            ],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT precision_w FROM lcp_print_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          }
        ]
      }
    },
    {
      "title": "温度检查",
      "header": [
        {"label": "检查项", "style": {"width": "45%"}},
        {"label": "检查值", "style": {"width": "40%"}}
      ],
      "body": {
        "rowHeader": "温度检查",
        "rows": [
          {
            "labelItems": [{"content": "预热温度"}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT preheat_temp FROM lcp_temp_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          },
          {
            "labelItems": [{"content": "加热温度"}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT heat_temp FROM lcp_temp_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          },
          {
            "labelItems": [{"content": "冷却温度"}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT cool_temp FROM lcp_temp_check WHERE lcp_name = :lcpName AND device = :device",
            "valueStyle": {}
          }
        ]
      }
    }
  ]
}', '0', '0', 'admin', 'LCP监控配置 - 根据图片设计');

-- 查看插入结果
SELECT config_id, config_key, config_name, status, create_time FROM monitor_config WHERE config_key = 'lcpMonitor';

