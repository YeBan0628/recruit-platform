-- 招聘平台：用户表建库建表脚本

CREATE DATABASE IF NOT EXISTS recruit DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE recruit;

CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `type`        TINYINT      DEFAULT 1 COMMENT '类型 1求职者 2招聘方',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';
