-- ========================================
-- MySQL 监控模块数据库初始化脚本
-- ========================================

-- 1. 创建 monitor 数据库（存储监控配置）
CREATE DATABASE IF NOT EXISTS `ry_monitor` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `ry_monitor`;

-- 2. 创建监控配置表
DROP TABLE IF EXISTS `monitor_config`;
CREATE TABLE `monitor_config` (
  `config_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键名',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_json` text COMMENT '配置JSON',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='监控配置表';

-- 3. 插入示例数据（先不插入，避免编码问题）
-- INSERT INTO `monitor_config` (`config_key`, `config_name`, `config_json`, `status`, `del_flag`, `create_by`, `remark`) VALUES
-- ('gpMonitor', 'GP监控', '{}', '0', '0', 'admin', 'GP监控示例配置');

-- ========================================
-- 4. 创建 user 数据库（业务数据查询）
-- ========================================
CREATE DATABASE IF NOT EXISTS `ry_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `ry_user`;

-- 5. 创建 LCP 生产数据主表
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
  KEY `idx_lcp_name` (`lcp_name`),
  KEY `idx_device` (`device`),
  KEY `idx_product_date1` (`product_date1`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='LCP生产数据主表';

-- 6. 创建 LCP 印刷检查数据表
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
  KEY `idx_lcp_name` (`lcp_name`),
  KEY `idx_device` (`device`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='LCP印刷检查数据表';

-- 7. 创建 LCP 温度检查数据表
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
  KEY `idx_lcp_name` (`lcp_name`),
  KEY `idx_device` (`device`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='LCP温度检查数据表';

-- 8. 插入 LCP 生产数据主表示例数据
INSERT INTO `lcp_production_data`
(`lcp_name`, `device`, `operator1`, `shift1`, `batch1`, `product_date1`, `product_model1`,
 `operator2`, `shift2`, `batch2`, `product_date2`, `product_model2`, `remark1`, `remark2`)
VALUES
('LCP-001', 'DEV-01', 'ZhangSan', 'DayShift', 'Batch-A1', '2025-01-15', 'LCP-2024-A',
 'LiSi', 'NightShift', 'Batch-A2', '2025-01-16', 'LCP-2024-B',
 'ABCDEFGHIJKLMNOPQRSTUV

-- ========================================
-- 7. LCP 生产数据表（可选）
-- ========================================
DROP TABLE IF EXISTS `lcp_production_data`;
CREATE TABLE `lcp_production_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lcp_name` varchar(100) DEFAULT NULL COMMENT 'LCP品名',
  `device` varchar(50) DEFAULT NULL COMMENT '设备',
  `user_name` varchar(64) DEFAULT NULL COMMENT '作业员',
  `shift` varchar(20) DEFAULT NULL COMMENT '班次',
  `product_date` date DEFAULT NULL COMMENT '生产日期',
  `product_model` varchar(100) DEFAULT NULL COMMENT '产品型号',
  `lcp_item` varchar(100) DEFAULT NULL COMMENT 'LCP处置项次',
  `furnace_time` varchar(50) DEFAULT NULL COMMENT '三温炉时间',
  `speed` decimal(10,2) DEFAULT NULL COMMENT '速度',
  `temperature` varchar(50) DEFAULT NULL COMMENT '炉温',
  `remark1` text COMMENT '备注1',
  `remark2` text COMMENT '备注2',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_lcp_name` (`lcp_name`),
  KEY `idx_device` (`device`),
  KEY `idx_product_date` (`product_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='LCP生产数据表';

-- 8. 插入示例数据
INSERT INTO `lcp_production_data`
(`lcp_name`, `device`, `user_name`, `shift`, `product_date`, `product_model`, `lcp_item`, `furnace_time`, `speed`, `temperature`, `remark1`, `remark2`)
VALUES
('LCP-001', 'DEV-03', 'WangWu', 'Day', '2025-01-15', 'LCP-2024-A', 'ITEM-001', '110s', 14.5, '240C', 'Normal', 'OK'),
('LCP-002', 'DEV-04', 'ZhaoLiu', 'Night', '2025-01-16', 'LCP-2024-B', 'ITEM-002', '120s', 15.0, '250C', 'Normal', 'OK');

-- ========================================
-- 完成！
-- ========================================
-- 说明：
-- 1. ry_monitor 数据库：存储监控页面配置（monitor_config 表）
-- 2. ry_user 数据库：存储业务数据（gp_production_data、lcp_production_data 等表）
-- 3. 可以根据实际业务需求修改表结构和字段
-- ========================================

