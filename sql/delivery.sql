-- 招聘平台：投递表建表脚本

USE recruit;

CREATE TABLE IF NOT EXISTS `delivery` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `position_id`  BIGINT        NOT NULL COMMENT '职位id',
    `applicant_id` BIGINT        NOT NULL COMMENT '求职者用户id',
    `status`       TINYINT       NOT NULL DEFAULT 0 COMMENT '0投递中 1已查看 2面试中 3已录用 4已拒绝 5已撤回',
    `message`      VARCHAR(1000) DEFAULT NULL COMMENT '投递留言',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_delivery_position_applicant` (`position_id`, `applicant_id`),
    KEY `idx_delivery_applicant_id` (`applicant_id`),
    KEY `idx_delivery_position_id` (`position_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '职位投递记录';
