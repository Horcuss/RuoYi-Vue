-- ----------------------------
-- Table structure for table gp_production_data
-- ----------------------------
DROP TABLE IF EXISTS `gp_production_data`;
CREATE TABLE `gp_production_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='GP生产数据主表';

-- ----------------------------
-- Records of gp_production_data
-- ----------------------------
INSERT INTO `gp_production_data` (`id`,`gp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('1','GP-001','DEV-01','ZhangSan','DayShift','Batch-A1','2025-01-15','GP-2024-A','LiSi','NightShift','Batch-A2','2025-01-16','GP-2024-B','GP生产正常，质量稳定，符合标准要求。所有检查项目均在合格范围内。','GP加工过程顺利，设备运行正常，温度控制精确，产品质量优良。','2025-10-06 17:21:29',NULL);
INSERT INTO `gp_production_data` (`id`,`gp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('2','GP-002','DEV-02','WangWu','DayShift','Batch-B1','2025-01-17','GP-2024-C','ZhaoLiu','NightShift','Batch-B2','2025-01-18','GP-2024-D','GP生产过程中温度略有波动，已调整参数，后续生产正常。','GP质量检查合格，所有指标符合要求，可以继续生产。','2025-10-06 17:21:29',NULL);
INSERT INTO `gp_production_data` (`id`,`gp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('3','GP-003','DEV-03','SunQi','DayShift','Batch-C1','2025-01-19','GP-2024-E','ZhouBa','NightShift','Batch-C2','2025-01-20','GP-2024-F','GP生产效率高，产品质量稳定，设备运行良好。','GP加工标准执行到位，各项参数控制精确，产品合格率100%。','2025-10-06 17:21:29',NULL);
INSERT INTO `gp_production_data` (`id`,`gp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('4','GP-004','DEV-04','WuJiu','DayShift','Batch-D1','2025-01-21','GP-2024-G','ZhengShi','NightShift','Batch-D2','2025-01-22','GP-2024-H','GP生产正常，质量优良，符合客户要求。','GP加工过程稳定，温度控制良好，产品质量达标。','2025-10-06 17:21:29',NULL);
