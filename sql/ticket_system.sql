-- =============================================
-- 传票系统数据库脚本
-- 数据库: ry_user
-- =============================================

-- 1. 工序code映射表
DROP TABLE IF EXISTS `ticket_process_code_mapping`;
CREATE TABLE `ticket_process_code_mapping` (
  `process_code` varchar(20) NOT NULL COMMENT '工序code',
  `process_name` varchar(50) NOT NULL COMMENT '工序中文名',
  PRIMARY KEY (`process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序code映射表';

-- 种子数据: 工序code映射
INSERT INTO `ticket_process_code_mapping` VALUES ('2350', 'dh处理');
INSERT INTO `ticket_process_code_mapping` VALUES ('2400', '电镀');
INSERT INTO `ticket_process_code_mapping` VALUES ('2450', '电镀后外选');
INSERT INTO `ticket_process_code_mapping` VALUES ('2500', '电镀后热处理');
INSERT INTO `ticket_process_code_mapping` VALUES ('3300', '一回测定');
INSERT INTO `ticket_process_code_mapping` VALUES ('3410', '后始末');
INSERT INTO `ticket_process_code_mapping` VALUES ('3500', '真空热处理');
INSERT INTO `ticket_process_code_mapping` VALUES ('3550', 'mips');
INSERT INTO `ticket_process_code_mapping` VALUES ('3600', 'g2外选');
INSERT INTO `ticket_process_code_mapping` VALUES ('4100', 'gp');
INSERT INTO `ticket_process_code_mapping` VALUES ('4400', '磁石吸取');
INSERT INTO `ticket_process_code_mapping` VALUES ('4500', '清空');

-- 2. lot最终工序序列表
DROP TABLE IF EXISTS `ticket_lot_process_sequence`;
CREATE TABLE `ticket_lot_process_sequence` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `lot_no` varchar(50) NOT NULL COMMENT 'lot编号',
  `process_code` varchar(20) NOT NULL COMMENT '工序code',
  `process_name` varchar(50) NOT NULL COMMENT '工序中文名',
  `seq` int NOT NULL COMMENT '排序号(从1开始)',
  `process_source` varchar(10) NOT NULL COMMENT '来源: fixed/extra',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lot_process` (`lot_no`, `process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='lot最终工序序列表';

-- 3. 感应器工序映射表
DROP TABLE IF EXISTS `ticket_sensor_process_mapping`;
CREATE TABLE `ticket_sensor_process_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `sensor_ip` varchar(50) NOT NULL COMMENT '感应器IP',
  `sensor_port` varchar(10) NOT NULL COMMENT '端口',
  `process_code` varchar(20) NOT NULL COMMENT '对应工序code',
  `sensor_type` varchar(10) NOT NULL COMMENT 'bind=绑定用 flow=流转用',
  `location_desc` varchar(200) DEFAULT NULL COMMENT '位置描述',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0=启用 1=停用',
  `remark` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sensor` (`sensor_ip`, `sensor_port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='感应器工序映射表';

-- 种子数据: 模拟感应器映射
INSERT INTO `ticket_sensor_process_mapping` (`sensor_ip`, `sensor_port`, `process_code`, `sensor_type`, `location_desc`, `status`) VALUES
('192.168.1.100', '9001', '2350', 'bind', 'dh处理现场-绑定感应器', '0'),
('192.168.1.101', '9001', '2350', 'flow', 'dh处理现场-流转感应器', '0'),
('192.168.1.102', '9001', '2400', 'flow', '电镀现场-流转感应器', '0'),
('192.168.1.103', '9001', '2450', 'flow', '电镀后外选现场-流转感应器', '0'),
('192.168.1.104', '9001', '2500', 'flow', '电镀后热处理现场-流转感应器', '0'),
('192.168.1.105', '9001', '3300', 'flow', '测定棚架-感应器1', '0'),
('192.168.1.106', '9001', '3410', 'flow', '后始末现场-流转感应器', '0'),
('192.168.1.107', '9001', '3500', 'flow', '真空热处理现场-流转感应器', '0'),
('192.168.1.108', '9001', '3550', 'flow', 'mips现场-流转感应器', '0'),
('192.168.1.109', '9001', '3600', 'flow', 'g2外选现场-流转感应器', '0'),
('192.168.1.110', '9001', '4100', 'flow', 'gp现场-流转感应器', '0'),
('192.168.1.111', '9001', '4400', 'flow', '磁石吸取现场-流转感应器', '0'),
('192.168.1.112', '9001', '4500', 'flow', '清空现场-流转感应器', '0');

-- 4. RFID与lot绑定关系表
DROP TABLE IF EXISTS `ticket_rfid_lot_binding`;
CREATE TABLE `ticket_rfid_lot_binding` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `rfid` varchar(100) NOT NULL COMMENT '水墨屏RFID编号',
  `lot_no` varchar(50) NOT NULL COMMENT 'lot编号',
  `bind_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0=有效 1=已解绑',
  PRIMARY KEY (`id`),
  KEY `idx_rfid` (`rfid`),
  KEY `idx_lot_no` (`lot_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RFID与lot绑定关系表';

-- 5. 水墨屏显示内容表
DROP TABLE IF EXISTS `ticket_eink_display_content`;
CREATE TABLE `ticket_eink_display_content` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `rfid` varchar(100) NOT NULL COMMENT '水墨屏RFID',
  `lot_no` varchar(50) NOT NULL COMMENT 'lot编号',
  `product_name` varchar(100) DEFAULT NULL COMMENT '品名',
  `product_model` varchar(100) DEFAULT NULL COMMENT '品番',
  `customer` varchar(100) DEFAULT NULL COMMENT '客户',
  `current_process_code` varchar(20) NOT NULL COMMENT '当前工序code',
  `current_process_name` varchar(50) NOT NULL COMMENT '当前工序中文名',
  `gp_defect_info` varchar(500) DEFAULT NULL COMMENT 'GP不良指示',
  `display_data` text COMMENT '预留扩展字段(JSON)',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0=正常 1=异常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rfid` (`rfid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水墨屏显示内容表';

-- 6. 完了输机明细表
DROP TABLE IF EXISTS `ticket_completion_detail`;
CREATE TABLE `ticket_completion_detail` (
  `lot_no` varchar(50) NOT NULL COMMENT 'lot编号',
  `process_code` varchar(20) NOT NULL COMMENT '工序code',
  `process_name` varchar(50) NOT NULL COMMENT '工序中文名',
  `work_date` date NOT NULL COMMENT '作业日',
  `worker` varchar(50) NOT NULL COMMENT '作业者',
  `completion_count` int NOT NULL COMMENT '完了数',
  `quantity` int DEFAULT NULL COMMENT '枚数(清空工序)',
  `device_no` varchar(50) DEFAULT NULL COMMENT '设备号(磁石吸取工序)',
  `film_roll_no` varchar(50) DEFAULT NULL COMMENT '薄膜卷号(gp工序)',
  `defect_info` varchar(500) DEFAULT NULL COMMENT '不良信息(gp工序)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`lot_no`, `process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='完了输机明细表';

-- 7. 不良入力明细表
DROP TABLE IF EXISTS `ticket_defect_detail`;
CREATE TABLE `ticket_defect_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `lot_no` varchar(50) NOT NULL COMMENT 'lot编号',
  `process_code` varchar(20) NOT NULL COMMENT '工序code',
  `process_name` varchar(50) NOT NULL COMMENT '工序中文名',
  `defect_item` varchar(100) NOT NULL COMMENT '不良项目',
  `sample_count` int NOT NULL COMMENT '取样数量(不良数)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  PRIMARY KEY (`id`),
  KEY `idx_lot_no` (`lot_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不良入力明细表';

-- 8. lot流转状态表
DROP TABLE IF EXISTS `ticket_lot_flow_status`;
CREATE TABLE `ticket_lot_flow_status` (
  `lot_no` varchar(50) NOT NULL COMMENT 'lot编号',
  `current_process_code` varchar(20) NOT NULL COMMENT '当前工序code',
  `current_seq` int NOT NULL COMMENT '当前工序在序列中的位置',
  `has_exited` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已离开当前工序(0=否 1=是)',
  `testing_round` int NOT NULL DEFAULT 0 COMMENT '测定回数(非测定工序时为0)',
  `testing_process_code` varchar(20) DEFAULT NULL COMMENT '测定工序code(首次进入测定时记录)',
  `last_event_type` varchar(10) DEFAULT NULL COMMENT '最后事件类型(IN/OUT)',
  `last_event_time` datetime DEFAULT NULL COMMENT '最后事件时间',
  `status` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/ABNORMAL',
  `abnormal_msg` varchar(500) DEFAULT NULL COMMENT '异常信息',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`lot_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='lot流转状态表';

-- 9. 感应器事件日志表
DROP TABLE IF EXISTS `ticket_sensor_event_log`;
CREATE TABLE `ticket_sensor_event_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `rfid` varchar(100) NOT NULL COMMENT 'RFID',
  `ant` varchar(50) DEFAULT NULL COMMENT '天线',
  `sensor_ip` varchar(50) NOT NULL COMMENT '感应器IP',
  `sensor_port` varchar(10) DEFAULT NULL COMMENT '端口',
  `event_type` int NOT NULL COMMENT '1=进 0=出',
  `remark` varchar(200) DEFAULT NULL,
  `event_time` datetime NOT NULL COMMENT '事件时间',
  `process_code` varchar(20) DEFAULT NULL COMMENT '通过映射查出的工序',
  `processed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0=未处理 1=已处理',
  `process_result` varchar(200) DEFAULT NULL COMMENT '处理结果描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='感应器事件日志表';
