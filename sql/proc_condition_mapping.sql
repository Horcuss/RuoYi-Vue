-- 加工条件映射表
CREATE TABLE proc_condition_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    proc_condition_group VARCHAR(10) NOT NULL COMMENT '加工条件group（01=非GP，02=GP）',
    major_class_cd VARCHAR(10) NOT NULL COMMENT '大分类cd',
    minor_class_cd VARCHAR(10) NOT NULL COMMENT '中分类cd',
    proc_condition_type_cd VARCHAR(10) NOT NULL COMMENT '加工条件種cd',
    multi_key_type VARCHAR(10) NOT NULL COMMENT '多key区分（1=品名，2=有多key，3=其他）',
    proc_condition_seq INT NOT NULL COMMENT '加工条件序号',
    condition_name VARCHAR(200) NOT NULL COMMENT '加工条件名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_condition (proc_condition_group, major_class_cd, minor_class_cd,
                             proc_condition_type_cd, multi_key_type, proc_condition_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加工条件映射表';

-- 创建索引，优化查询性能
CREATE INDEX idx_type_cd ON proc_condition_mapping(proc_condition_type_cd);
CREATE INDEX idx_group_major_minor ON proc_condition_mapping(proc_condition_group, major_class_cd, minor_class_cd);
