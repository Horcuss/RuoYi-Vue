-- ----------------------------
-- Table structure for table lcp_production_data
-- ----------------------------
DROP TABLE IF EXISTS `lcp_production_data`;
CREATE TABLE `lcp_production_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `lcp_name` varchar(100) DEFAULT NULL COMMENT 'LCP鍝佸悕',
  `device` varchar(50) DEFAULT NULL COMMENT '璁惧?',
  `operator1` varchar(64) DEFAULT NULL COMMENT '浣滀笟鍛?',
  `shift1` varchar(20) DEFAULT NULL COMMENT '鐝??1',
  `batch1` varchar(50) DEFAULT NULL COMMENT '绗??1',
  `product_date1` date DEFAULT NULL COMMENT '鐢熶骇鏃ユ湡1',
  `product_model1` varchar(100) DEFAULT NULL COMMENT '浜у搧鍨嬪彿1',
  `operator2` varchar(64) DEFAULT NULL COMMENT '浣滀笟鍛?',
  `shift2` varchar(20) DEFAULT NULL COMMENT '鐝??2',
  `batch2` varchar(50) DEFAULT NULL COMMENT '绗??2',
  `product_date2` date DEFAULT NULL COMMENT '鐢熶骇鏃ユ湡2',
  `product_model2` varchar(100) DEFAULT NULL COMMENT '浜у搧鍨嬪彿2',
  `remark1` text COMMENT '澶囨敞1',
  `remark2` text COMMENT '澶囨敞2',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lcp_device` (`lcp_name`,`device`),
  KEY `idx_lcp_name` (`lcp_name`),
  KEY `idx_device` (`device`),
  KEY `idx_product_date1` (`product_date1`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='LCP鐢熶骇鏁版嵁涓昏〃';

-- ----------------------------
-- Records of lcp_production_data
-- ----------------------------
INSERT INTO `lcp_production_data` (`id`,`lcp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('1','LCP-001','DEV-01','ZhangSan','DayShift','Batch-A1','2025-01-15','LCP-2024-A','LiSi','12','Batch-A2','2025-01-16','LCP-2024-B','ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890','Normal production, no abnormalities detected.','2025-10-06 16:27:36','2025-10-14 20:39:21');
INSERT INTO `lcp_production_data` (`id`,`lcp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('2','LCP-002','DEV-02','WangWu','DayShift','Batch-B1','2025-01-17','LCP-2024-C','ZhaoLiu','NightShift','Batch-B2','2025-01-18','LCP-2024-D','Quality check passed, all parameters within specification.','Temperature monitoring normal.','2025-10-06 16:27:36',NULL);
INSERT INTO `lcp_production_data` (`id`,`lcp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('3','LCP-003','DEV-03','SunQi','DayShift','Batch-C1','2025-01-19','LCP-2024-E','ZhouBa','NightShift','Batch-C2','2025-01-20','LCP-2024-F','Production efficiency improved by 5%.','Equipment maintenance completed.','2025-10-06 16:27:36',NULL);
INSERT INTO `lcp_production_data` (`id`,`lcp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('4','LCP-004','DEV-01','LiuJiu','DayShift','Batch-D1','2025-01-21','LCP-2024-G','ChenShi','NightShift','Batch-D2','2025-01-22','LCP-2024-H','New batch started with updated parameters.','Quality control inspection scheduled.','2025-10-06 16:27:36',NULL);
INSERT INTO `lcp_production_data` (`id`,`lcp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('5','LCP-005','DEV-02','YangShiyi','DayShift','Batch-E1','2025-01-23','LCP-2024-I','HuangShier','NightShift','Batch-E2','2025-01-24','LCP-2024-J','Production line optimization in progress.','Energy consumption reduced by 3%.','2025-10-06 16:27:36',NULL);
INSERT INTO `lcp_production_data` (`id`,`lcp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('6','LCP-006','DEV-03','WuShisan','DayShift','Batch-F1','2025-01-25','LCP-2024-K','ZhengShisi','NightShift','Batch-F2','2025-01-26','LCP-2024-L','Material quality excellent.','No defects detected in this batch.','2025-10-06 16:27:36',NULL);
INSERT INTO `lcp_production_data` (`id`,`lcp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('7','LCP-007','DEV-01','WangShiwu','DayShift','Batch-G1','2025-01-27','LCP-2024-M','ZhaoShiliu','NightShift','Batch-G2','2025-01-28','LCP-2024-N','Standard operating procedure followed.','All safety checks completed.','2025-10-06 16:27:36',NULL);
INSERT INTO `lcp_production_data` (`id`,`lcp_name`,`device`,`operator1`,`shift1`,`batch1`,`product_date1`,`product_model1`,`operator2`,`shift2`,`batch2`,`product_date2`,`product_model2`,`remark1`,`remark2`,`create_time`,`update_time`)  VALUES ('8','LCP-008','DEV-02','LiShiqi','DayShift','Batch-H1','2025-01-29','LCP-2024-O','ZhouShiba','NightShift','Batch-H2','2025-01-30','LCP-2024-P','Production target achieved.','Equipment performance stable.','2025-10-06 16:27:36',NULL);
