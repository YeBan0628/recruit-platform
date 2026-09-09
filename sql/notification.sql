-- 招聘平台：通知表建表脚本

USE recruit;

CREATE TABLE IF NOT EXISTS `notification` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `event_id`    VARCHAR(64)    NOT NULL COMMENT '业务事件id，用于幂等',
    `delivery_id` BIGINT        NOT NULL COMMENT '投递id',
    `receiver_id` BIGINT        NOT NULL COMMENT '接收者用户id',
    `type`        VARCHAR(50)   NOT NULL COMMENT '通知类型',
    `content`     VARCHAR(500)  NOT NULL COMMENT '通知内容',
    `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notification_event_id` (`event_id`),
    KEY `idx_notification_receiver_id` (`receiver_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '站内通知';
