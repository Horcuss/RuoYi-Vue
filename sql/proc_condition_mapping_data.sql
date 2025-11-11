-- 加工条件映射表数据
-- 格式：group、大分类cd、中分类cd、加工条件種cd、多key区分、序号、条件名称

INSERT INTO proc_condition_mapping
(proc_condition_group, major_class_cd, minor_class_cd, proc_condition_type_cd, multi_key_type, proc_condition_seq, condition_name)
VALUES
('02', '001', 'N01', '001', '1', 1, 'XXX'),
('02', '001', 'N01', '001', '2', 2, '设备类型'),
('02', '001', 'N01', '001', '2', 3, 'XXX'),
('02', '001', 'N01', '001', '2', 4, 'XXX'),
('02', '001', 'N01', '001', '3', 5, 'XXX'),
('02', '001', 'N01', '001', '3', 6, 'XXX'),
('02', '001', 'N01', '001', '3', 7, 'XXX'),
('02', '001', 'N01', '001', '3', 8, 'XXX'),
('02', '001', 'N01', '001', '3', 9, 'XXX');
