-- 招聘平台：职位表建表脚本

USE recruit;

CREATE TABLE IF NOT EXISTS `position` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '发布者用户id(招聘方)',
    `title`       VARCHAR(50)  NOT NULL COMMENT '职位名称',
    `city`        VARCHAR(30)  DEFAULT NULL COMMENT '工作城市',
    `salary_min`  INT          DEFAULT NULL COMMENT '薪资下限(K)',
    `salary_max`  INT          DEFAULT NULL COMMENT '薪资上限(K)',
    `experience`  VARCHAR(20)  DEFAULT NULL COMMENT '经验要求',
    `education`   VARCHAR(20)  DEFAULT NULL COMMENT '学历要求',
    `description` TEXT         COMMENT '职位描述',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态 1上架 0下架',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_city` (`city`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '职位表';
