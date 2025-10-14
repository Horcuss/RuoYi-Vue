-- ========================================
-- LCP 监控模块数据库初始化脚本（根据图片重新设计）
-- ========================================

-- 使用 ry_user 数据库
USE `ry_user`;

-- ========================================
-- 1. LCP 生产数据主表
-- ========================================
DROP TABLE IF EXISTS `lcp_production_data`;
CREATE TABLE `lcp_production_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lcp_name` varchar(100) DEFAULT NULL COMMENT 'LCP品名',
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
  UNIQUE KEY `uk_lcp_device` (`lcp_name`, `device`),
  KEY `idx_lcp_name` (`lcp_name`),
  KEY `idx_device` (`device`),
  KEY `idx_product_date1` (`product_date1`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='LCP生产数据主表';

-- ========================================
-- 2. LCP 印刷检查数据表（左侧表格）
-- ========================================
DROP TABLE IF EXISTS `lcp_print_check`;
CREATE TABLE `lcp_print_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lcp_name` varchar(100) DEFAULT NULL COMMENT 'LCP品名',
  `device` varchar(50) DEFAULT NULL COMMENT '设备',
  `lcp_item` varchar(100) DEFAULT NULL COMMENT 'LCP处置项次',
  `furnace_time` varchar(50) DEFAULT NULL COMMENT '三温炉时间',
  `speed` decimal(10,2) DEFAULT NULL COMMENT '速度(m/min)',
  `furnace_temp` varchar(50) DEFAULT NULL COMMENT '炉温（开/关/开）',
  `lcp_amount` decimal(10,2) DEFAULT NULL COMMENT 'lcp量(um)',
  `precision_l` decimal(10,2) DEFAULT NULL COMMENT '基准精度/尺度 L(um)',
  `precision_w` decimal(10,2) DEFAULT NULL COMMENT '基准精度/尺度 w(um)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lcp_device` (`lcp_name`, `device`),
  KEY `idx_lcp_name` (`lcp_name`),
  KEY `idx_device` (`device`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='LCP印刷检查数据表';

-- ========================================
-- 3. LCP 温度检查数据表（右侧表格）
-- ========================================
DROP TABLE IF EXISTS `lcp_temp_check`;
CREATE TABLE `lcp_temp_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lcp_name` varchar(100) DEFAULT NULL COMMENT 'LCP品名',
  `device` varchar(50) DEFAULT NULL COMMENT '设备',
  `preheat_temp` varchar(20) DEFAULT NULL COMMENT '预热温度',
  `heat_temp` varchar(20) DEFAULT NULL COMMENT '加热温度',
  `cool_temp` varchar(20) DEFAULT NULL COMMENT '冷却温度',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lcp_device` (`lcp_name`, `device`),
  KEY `idx_lcp_name` (`lcp_name`),
  KEY `idx_device` (`device`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='LCP温度检查数据表';

-- ========================================
-- 4. 插入示例数据
-- ========================================

-- 插入 LCP 生产数据主表
INSERT INTO `lcp_production_data` 
(`lcp_name`, `device`, `operator1`, `shift1`, `batch1`, `product_date1`, `product_model1`, 
 `operator2`, `shift2`, `batch2`, `product_date2`, `product_model2`, `remark1`, `remark2`) 
VALUES
('LCP-001', 'DEV-01', 'ZhangSan', 'DayShift', 'Batch-A1', '2025-01-15', 'LCP-2024-A', 
 'LiSi', 'NightShift', 'Batch-A2', '2025-01-16', 'LCP-2024-B', 
 'ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890', 
 'Normal production, no abnormalities detected.'),
('LCP-002', 'DEV-02', 'WangWu', 'DayShift', 'Batch-B1', '2025-01-17', 'LCP-2024-C', 
 'ZhaoLiu', 'NightShift', 'Batch-B2', '2025-01-18', 'LCP-2024-D', 
 'Quality check passed, all parameters within specification.', 
 'Temperature monitoring normal.'),
('LCP-003', 'DEV-03', 'SunQi', 'DayShift', 'Batch-C1', '2025-01-19', 'LCP-2024-E', 
 'ZhouBa', 'NightShift', 'Batch-C2', '2025-01-20', 'LCP-2024-F', 
 'Production efficiency improved by 5%.', 
 'Equipment maintenance completed.');

-- 插入 LCP 印刷检查数据
INSERT INTO `lcp_print_check` 
(`lcp_name`, `device`, `lcp_item`, `furnace_time`, `speed`, `furnace_temp`, `lcp_amount`, `precision_l`, `precision_w`) 
VALUES
('LCP-001', 'DEV-01', 'ITEM-LCP-001', '120s', 15.5, '200/OFF/180', 25.5, 100.2, 50.1),
('LCP-002', 'DEV-02', 'ITEM-LCP-002', '125s', 16.0, '210/OFF/190', 26.0, 102.5, 51.3),
('LCP-003', 'DEV-03', 'ITEM-LCP-003', '118s', 15.2, '195/OFF/175', 24.8, 99.8, 49.9);

-- 插入 LCP 温度检查数据
INSERT INTO `lcp_temp_check` 
(`lcp_name`, `device`, `preheat_temp`, `heat_temp`, `cool_temp`) 
VALUES
('LCP-001', 'DEV-01', '150C', '200C', '80C'),
('LCP-002', 'DEV-02', '155C', '205C', '82C'),
('LCP-003', 'DEV-03', '148C', '198C', '78C');

-- ========================================
-- 5. 更多示例数据（用于测试）
-- ========================================

-- 更多 LCP 生产数据
INSERT INTO `lcp_production_data` 
(`lcp_name`, `device`, `operator1`, `shift1`, `batch1`, `product_date1`, `product_model1`, 
 `operator2`, `shift2`, `batch2`, `product_date2`, `product_model2`, `remark1`, `remark2`) 
VALUES
('LCP-004', 'DEV-01', 'LiuJiu', 'DayShift', 'Batch-D1', '2025-01-21', 'LCP-2024-G', 
 'ChenShi', 'NightShift', 'Batch-D2', '2025-01-22', 'LCP-2024-H', 
 'New batch started with updated parameters.', 
 'Quality control inspection scheduled.'),
('LCP-005', 'DEV-02', 'YangShiyi', 'DayShift', 'Batch-E1', '2025-01-23', 'LCP-2024-I', 
 'HuangShier', 'NightShift', 'Batch-E2', '2025-01-24', 'LCP-2024-J', 
 'Production line optimization in progress.', 
 'Energy consumption reduced by 3%.'),
('LCP-006', 'DEV-03', 'WuShisan', 'DayShift', 'Batch-F1', '2025-01-25', 'LCP-2024-K', 
 'ZhengShisi', 'NightShift', 'Batch-F2', '2025-01-26', 'LCP-2024-L', 
 'Material quality excellent.', 
 'No defects detected in this batch.'),
('LCP-007', 'DEV-01', 'WangShiwu', 'DayShift', 'Batch-G1', '2025-01-27', 'LCP-2024-M', 
 'ZhaoShiliu', 'NightShift', 'Batch-G2', '2025-01-28', 'LCP-2024-N', 
 'Standard operating procedure followed.', 
 'All safety checks completed.'),
('LCP-008', 'DEV-02', 'LiShiqi', 'DayShift', 'Batch-H1', '2025-01-29', 'LCP-2024-O', 
 'ZhouShiba', 'NightShift', 'Batch-H2', '2025-01-30', 'LCP-2024-P', 
 'Production target achieved.', 
 'Equipment performance stable.');

-- 更多 LCP 印刷检查数据
INSERT INTO `lcp_print_check` 
(`lcp_name`, `device`, `lcp_item`, `furnace_time`, `speed`, `furnace_temp`, `lcp_amount`, `precision_l`, `precision_w`) 
VALUES
('LCP-004', 'DEV-01', 'ITEM-LCP-004', '122s', 15.8, '202/OFF/182', 25.8, 101.0, 50.5),
('LCP-005', 'DEV-02', 'ITEM-LCP-005', '119s', 15.3, '198/OFF/178', 24.9, 99.5, 49.7),
('LCP-006', 'DEV-03', 'ITEM-LCP-006', '121s', 15.6, '201/OFF/181', 25.6, 100.8, 50.4),
('LCP-007', 'DEV-01', 'ITEM-LCP-007', '123s', 15.9, '203/OFF/183', 25.9, 101.2, 50.6),
('LCP-008', 'DEV-02', 'ITEM-LCP-008', '120s', 15.5, '200/OFF/180', 25.5, 100.0, 50.0);

-- 更多 LCP 温度检查数据
INSERT INTO `lcp_temp_check` 
(`lcp_name`, `device`, `preheat_temp`, `heat_temp`, `cool_temp`) 
VALUES
('LCP-004', 'DEV-01', '151C', '201C', '81C'),
('LCP-005', 'DEV-02', '149C', '199C', '79C'),
('LCP-006', 'DEV-03', '152C', '202C', '82C'),
('LCP-007', 'DEV-01', '150C', '200C', '80C'),
('LCP-008', 'DEV-02', '153C', '203C', '83C');

-- ========================================
-- 完成！
-- ========================================
-- 说明：
-- 1. lcp_production_data：LCP 生产数据主表（包含作业员、班次、日期、型号等基础信息）
-- 2. lcp_print_check：LCP 印刷检查数据表（包含处置项次、炉温、速度、精度等检查数据）
-- 3. lcp_temp_check：LCP 温度检查数据表（包含预热、加热、冷却温度）
-- 4. 通过 lcp_name + device 关联三张表的数据
-- 5. 已插入 8 条完整的示例数据供测试使用
-- ========================================

