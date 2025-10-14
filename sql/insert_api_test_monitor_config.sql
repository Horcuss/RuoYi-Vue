-- =============================================
-- API测试监控配置
-- 用于测试外部API数据源功能
-- API地址: https://jsonplaceholder.typicode.com/users/1
-- =============================================

-- 删除已存在的配置
DELETE FROM monitor_config WHERE config_key = 'apiTestMonitor';

-- 插入 API测试监控配置
INSERT INTO `monitor_config` (`config_key`, `config_name`, `config_json`, `status`, `del_flag`, `create_by`, `remark`) 
VALUES ('apiTestMonitor', 'API测试监控', '{
  "apiUrl": "https://jsonplaceholder.typicode.com/users/1",
  "formItems": [],
  "widgetItems": [],
  "descItems": [
    {
      "label": "用户ID",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "id"
    },
    {
      "label": "用户名",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "name"
    },
    {
      "label": "用户账号",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "username"
    },
    {
      "label": "邮箱",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "email"
    },
    {
      "label": "城市",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "address.city"
    },
    {
      "label": "街道",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "address.street"
    },
    {
      "label": "纬度",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "address.geo.lat"
    },
    {
      "label": "经度",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "address.geo.lng"
    }
  ],
  "remarkItems": [
    {
      "title": "公司信息",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "company.name"
    },
    {
      "title": "公司口号",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "company.catchPhrase"
    }
  ],
  "tableConfigs": [
    {
      "rowHeader": "联系方式",
      "rows": [
        {
          "rowType": "simple",
          "projectName": "电话",
          "unit": "",
          "dataSource": "api",
          "displayType": "direct",
          "expression": "phone"
        },
        {
          "rowType": "simple",
          "projectName": "网站",
          "unit": "",
          "dataSource": "api",
          "displayType": "direct",
          "expression": "website"
        }
      ]
    },
    {
      "rowHeader": "地理位置",
      "rows": [
        {
          "rowType": "complex",
          "projectName": "坐标",
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
        },
        {
          "rowType": "simple",
          "projectName": "邮编",
          "unit": "",
          "dataSource": "api",
          "displayType": "direct",
          "expression": "address.zipcode"
        }
      ]
    }
  ]
}', '0', '0', 'admin', 'API测试监控配置，用于测试外部API数据源功能');

-- 提交
COMMIT;

