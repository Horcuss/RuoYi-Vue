-- =============================================
-- 简单API测试配置
-- 用于快速验证API功能是否正常
-- =============================================

-- 删除已存在的测试配置
DELETE FROM monitor_config WHERE config_key = 'simpleApiTest';

-- 插入简单的API测试配置
INSERT INTO `monitor_config` (`config_key`, `config_name`, `config_json`, `status`, `del_flag`, `create_by`, `remark`) 
VALUES ('simpleApiTest', '简单API测试', '{
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
      "label": "纬度",
      "dataSource": "api",
      "displayType": "direct",
      "expression": "address.geo.lat"
    },
    {
      "label": "ID乘以10",
      "dataSource": "api",
      "displayType": "computed",
      "expression": "id * 10"
    }
  ],
  "remarkItems": [],
  "tableConfigs": []
}', '0', '0', 'admin', '简单API测试配置');

-- 提交
COMMIT;

-- 验证配置
SELECT 
    config_key,
    config_name,
    CASE 
        WHEN config_json LIKE '%apiUrl%' THEN '✓ 包含API地址'
        ELSE '✗ 缺少API地址'
    END as api_url_status,
    CASE 
        WHEN config_json LIKE '%"dataSource":"api"%' THEN '✓ 包含API数据源'
        ELSE '✗ 缺少API数据源'
    END as api_datasource_status,
    create_time
FROM monitor_config 
WHERE config_key = 'simpleApiTest';

-- 显示完整配置（用于检查）
SELECT config_json 
FROM monitor_config 
WHERE config_key = 'simpleApiTest'\G

