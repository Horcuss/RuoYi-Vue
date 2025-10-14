-- ========================================
-- GP 监控相关数据库表和示例数据
-- ========================================

USE `ry_user`;

-- 1. 创建 GP 生产数据主表
DROP TABLE IF EXISTS `gp_production_data`;
CREATE TABLE `gp_production_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `gp_name` varchar(100) DEFAULT NULL COMMENT 'GP品名',
  `device` varchar(50) DEFAULT NULL COMMENT '设备',
  `operator1` varchar(64) DEFAULT NULL COMMENT '作业员1',
  `shift1` varchar(20) DEFAULT NULL COMMENT '班次1',
  `batch1` varchar(50) DEFAULT NULL COMMENT '第次1',
  `product_date1` date DEFAULT NULL COMMENT '生产日期1',
  `product_model1` varchar(100) DEFAULT NULL COMMENT '产品型号1',
  `operator2` varchar(64) DEFAULT NULL COMMENT '作业员2',
  `shift2` varchar(20) DEFAULT NULL COMMENT '班次2',
  `batch2` varchar(50) DEFAULT NULL COMMENT '第次2',
  `product_date2` date DEFAULT NULL COMMENT '生产日期2',
  `product_model2` varchar(100) DEFAULT NULL COMMENT '产品型号2',
  `remark1` text COMMENT '备注1',
  `remark2` text COMMENT '备注2',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_gp_name` (`gp_name`),
  KEY `idx_device` (`device`),
  KEY `idx_product_date1` (`product_date1`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='GP生产数据主表';

-- 2. 创建 GP 印刷检查数据表
DROP TABLE IF EXISTS `gp_print_check`;
CREATE TABLE `gp_print_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `gp_name` varchar(100) DEFAULT NULL COMMENT 'GP品名',
  `device` varchar(50) DEFAULT NULL COMMENT '设备',
  `gp_item` varchar(100) DEFAULT NULL COMMENT 'GP处置项次',
  `furnace_time` varchar(50) DEFAULT NULL COMMENT '三温炉时间',
  `speed` decimal(10,2) DEFAULT NULL COMMENT '速度(m/min)',
  `furnace_temp` varchar(50) DEFAULT NULL COMMENT '炉温（开/关/开）',
  `gp_amount` decimal(10,2) DEFAULT NULL COMMENT 'gp量(um)',
  `precision_l` decimal(10,2) DEFAULT NULL COMMENT '基准精度/尺度 L(um)',
  `precision_w` decimal(10,2) DEFAULT NULL COMMENT '基准精度/尺度 w(um)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_gp_name` (`gp_name`),
  KEY `idx_device` (`device`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='GP印刷检查数据表';

-- 3. 创建 GP 温度检查数据表
DROP TABLE IF EXISTS `gp_temp_check`;
CREATE TABLE `gp_temp_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `gp_name` varchar(100) DEFAULT NULL COMMENT 'GP品名',
  `device` varchar(50) DEFAULT NULL COMMENT '设备',
  `preheat_temp` varchar(20) DEFAULT NULL COMMENT '预热温度',
  `heat_temp` varchar(20) DEFAULT NULL COMMENT '加热温度',
  `cool_temp` varchar(20) DEFAULT NULL COMMENT '冷却温度',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_gp_name` (`gp_name`),
  KEY `idx_device` (`device`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='GP温度检查数据表';

-- ========================================
-- 插入示例数据
-- ========================================

-- 插入 GP 生产数据主表示例数据
INSERT INTO `gp_production_data`
(`gp_name`, `device`, `operator1`, `shift1`, `batch1`, `product_date1`, `product_model1`,
 `operator2`, `shift2`, `batch2`, `product_date2`, `product_model2`, `remark1`, `remark2`)
VALUES
('GP-001', 'DEV-01', 'ZhangSan', 'DayShift', 'Batch-A1', '2025-01-15', 'GP-2024-A',
 'LiSi', 'NightShift', 'Batch-A2', '2025-01-16', 'GP-2024-B',
 'GP生产正常，质量稳定，符合标准要求。所有检查项目均在合格范围内。',
 'GP加工过程顺利，设备运行正常，温度控制精确，产品质量优良。'),
 
('GP-002', 'DEV-02', 'WangWu', 'DayShift', 'Batch-B1', '2025-01-17', 'GP-2024-C',
 'ZhaoLiu', 'NightShift', 'Batch-B2', '2025-01-18', 'GP-2024-D',
 'GP生产过程中温度略有波动，已调整参数，后续生产正常。',
 'GP质量检查合格，所有指标符合要求，可以继续生产。'),
 
('GP-003', 'DEV-03', 'SunQi', 'DayShift', 'Batch-C1', '2025-01-19', 'GP-2024-E',
 'ZhouBa', 'NightShift', 'Batch-C2', '2025-01-20', 'GP-2024-F',
 'GP生产效率高，产品质量稳定，设备运行良好。',
 'GP加工标准执行到位，各项参数控制精确，产品合格率100%。'),
 
('GP-004', 'DEV-04', 'WuJiu', 'DayShift', 'Batch-D1', '2025-01-21', 'GP-2024-G',
 'ZhengShi', 'NightShift', 'Batch-D2', '2025-01-22', 'GP-2024-H',
 'GP生产正常，质量优良，符合客户要求。',
 'GP加工过程稳定，温度控制良好，产品质量达标。');

-- 插入 GP 印刷检查数据
INSERT INTO `gp_print_check` 
(`gp_name`, `device`, `gp_item`, `furnace_time`, `speed`, `furnace_temp`, `gp_amount`, `precision_l`, `precision_w`) 
VALUES
('GP-001', 'DEV-01', 'ITEM-GP-001', '115s', 14.5, '190/OFF/170', 22.5, 98.5, 48.2),
('GP-002', 'DEV-02', 'ITEM-GP-002', '118s', 15.0, '195/OFF/175', 23.0, 99.8, 49.1),
('GP-003', 'DEV-03', 'ITEM-GP-003', '120s', 15.5, '200/OFF/180', 23.5, 100.2, 50.0),
('GP-004', 'DEV-04', 'ITEM-GP-004', '122s', 16.0, '205/OFF/185', 24.0, 101.5, 50.8),
('GP-005', 'DEV-01', 'ITEM-GP-005', '116s', 14.8, '192/OFF/172', 22.8, 99.0, 48.5),
('GP-006', 'DEV-02', 'ITEM-GP-006', '119s', 15.2, '197/OFF/177', 23.2, 100.0, 49.5),
('GP-007', 'DEV-03', 'ITEM-GP-007', '121s', 15.8, '202/OFF/182', 23.8, 100.8, 50.3),
('GP-008', 'DEV-04', 'ITEM-GP-008', '123s', 16.2, '207/OFF/187', 24.2, 102.0, 51.0);

-- 插入 GP 温度检查数据
INSERT INTO `gp_temp_check` 
(`gp_name`, `device`, `preheat_temp`, `heat_temp`, `cool_temp`) 
VALUES
('GP-001', 'DEV-01', '145C', '195C', '75C'),
('GP-002', 'DEV-02', '148C', '198C', '77C'),
('GP-003', 'DEV-03', '150C', '200C', '80C'),
('GP-004', 'DEV-04', '152C', '202C', '82C'),
('GP-005', 'DEV-01', '146C', '196C', '76C'),
('GP-006', 'DEV-02', '149C', '199C', '78C'),
('GP-007', 'DEV-03', '151C', '201C', '81C'),
('GP-008', 'DEV-04', '153C', '203C', '83C');

-- ========================================
-- 验证数据
-- ========================================

-- 查看 GP 生产数据
SELECT COUNT(*) as gp_production_count FROM gp_production_data;

-- 查看 GP 印刷检查数据
SELECT COUNT(*) as gp_print_check_count FROM gp_print_check;

-- 查看 GP 温度检查数据
SELECT COUNT(*) as gp_temp_check_count FROM gp_temp_check;

-- 查看示例数据
SELECT * FROM gp_production_data WHERE gp_name = 'GP-001' AND device = 'DEV-01';
SELECT * FROM gp_print_check WHERE gp_name = 'GP-001' AND device = 'DEV-01';
SELECT * FROM gp_temp_check WHERE gp_name = 'GP-001' AND device = 'DEV-01';

