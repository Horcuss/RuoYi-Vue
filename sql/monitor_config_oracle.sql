-- ----------------------------
-- 监控配置表（Oracle版本）
-- ----------------------------

-- 删除表（如果存在）
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE monitor_config';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

-- 删除序列（如果存在）
BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE seq_monitor_config';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -2289 THEN
         RAISE;
      END IF;
END;
/

-- 创建监控配置表
CREATE TABLE monitor_config (
  config_id         NUMBER(20)          NOT NULL,
  config_key        VARCHAR2(100)       NOT NULL,
  config_name       VARCHAR2(200)       NOT NULL,
  config_json       CLOB                NOT NULL,
  status            CHAR(1)             DEFAULT '0',
  remark            VARCHAR2(500),
  create_by         VARCHAR2(64)        DEFAULT '',
  create_time       DATE                DEFAULT SYSDATE,
  update_by         VARCHAR2(64)        DEFAULT '',
  update_time       DATE,
  del_flag          CHAR(1)             DEFAULT '0',
  CONSTRAINT pk_monitor_config PRIMARY KEY (config_id),
  CONSTRAINT uk_monitor_config_key UNIQUE (config_key)
);

-- 添加表注释
COMMENT ON TABLE monitor_config IS '监控配置表';

-- 添加字段注释
COMMENT ON COLUMN monitor_config.config_id IS '配置ID';
COMMENT ON COLUMN monitor_config.config_key IS '监控页面KEY（如：gpMonitor）';
COMMENT ON COLUMN monitor_config.config_name IS '监控页面名称（如：GP监控）';
COMMENT ON COLUMN monitor_config.config_json IS '配置JSON数据';
COMMENT ON COLUMN monitor_config.status IS '状态（0=启用 1=停用）';
COMMENT ON COLUMN monitor_config.remark IS '备注';
COMMENT ON COLUMN monitor_config.create_by IS '创建者';
COMMENT ON COLUMN monitor_config.create_time IS '创建时间';
COMMENT ON COLUMN monitor_config.update_by IS '更新者';
COMMENT ON COLUMN monitor_config.update_time IS '更新时间';
COMMENT ON COLUMN monitor_config.del_flag IS '删除标志（0=正常 2=删除）';

-- 创建序列
CREATE SEQUENCE seq_monitor_config
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;

-- 创建索引
CREATE INDEX idx_monitor_config_key ON monitor_config(config_key);
CREATE INDEX idx_monitor_config_status ON monitor_config(status);
CREATE INDEX idx_monitor_config_del_flag ON monitor_config(del_flag);

-- 插入测试数据
INSERT INTO monitor_config (config_id, config_key, config_name, config_json, status, create_by, create_time, del_flag)
VALUES (
  seq_monitor_config.NEXTVAL,
  'gpMonitor',
  'GP监控',
  '{
    "formItems": [
      {"label": "GP品名", "prop": "gpName"},
      {"label": "设备", "prop": "device"}
    ],
    "widgetItems": [
      {"text": "GP加工标准", "href": "#"},
      {"text": "GP本作业标准", "href": "#"}
    ],
    "descItems": [
      {"label": "作业员", "dataSource": "api", "displayType": "direct", "expression": "userName"},
      {"label": "班次", "dataSource": "api", "displayType": "direct", "expression": "shift"}
    ],
    "remarkItems": [
      {"title": "备注1", "dataSource": "api", "displayType": "direct", "expression": "remark1"}
    ],
    "tableConfigs": [
      {
        "rowHeader": "印刷检查、面积检查",
        "rows": [
          {
            "labelItems": [{"content": "GP处置项次", "colSpan": 3}],
            "dataSource": "api",
            "displayType": "direct",
            "expression": "gpValue",
            "valueStyle": {"color": "", "backgroundColor": "", "fontSize": ""}
          }
        ]
      }
    ]
  }',
  '0',
  'admin',
  SYSDATE,
  '0'
);

INSERT INTO monitor_config (config_id, config_key, config_name, config_json, status, create_by, create_time, del_flag)
VALUES (
  seq_monitor_config.NEXTVAL,
  'lcpMonitor',
  'LCP监控',
  '{
    "formItems": [
      {"label": "LCP品名", "prop": "lcpName"},
      {"label": "设备", "prop": "device"}
    ],
    "widgetItems": [
      {"text": "LCP加工标准", "href": "#"},
      {"text": "LCP本作业标准", "href": "#"}
    ],
    "descItems": [
      {"label": "作业员", "dataSource": "api", "displayType": "direct", "expression": "userName"},
      {"label": "班次", "dataSource": "api", "displayType": "direct", "expression": "shift"}
    ],
    "remarkItems": [
      {"title": "备注1", "dataSource": "api", "displayType": "direct", "expression": "remark1"}
    ],
    "tableConfigs": [
      {
        "rowHeader": "温度检查",
        "rows": [
          {
            "labelItems": [{"content": "预热温度"}],
            "dataSource": "database",
            "displayType": "direct",
            "expression": "SELECT temperature FROM production_data WHERE device_id = ''001'' AND type = ''preheat''",
            "valueStyle": {"color": "", "backgroundColor": "", "fontSize": ""}
          }
        ]
      }
    ]
  }',
  '0',
  'admin',
  SYSDATE,
  '0'
);

COMMIT;

