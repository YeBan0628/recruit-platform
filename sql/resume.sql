-- 招聘平台：简历服务表建表脚本

USE recruit;

CREATE TABLE IF NOT EXISTS `resume` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '用户id',
    `name`        VARCHAR(50)  NOT NULL COMMENT '姓名',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `summary`     VARCHAR(1000) DEFAULT NULL COMMENT '个人简介',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1启用 0隐藏',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_resume_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '简历主表';

CREATE TABLE IF NOT EXISTS `resume_education` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `resume_id`   BIGINT       NOT NULL COMMENT '简历id',
    `school`      VARCHAR(100) DEFAULT NULL COMMENT '学校',
    `major`       VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `degree`      VARCHAR(30)  DEFAULT NULL COMMENT '学历',
    `start_date`  DATE         DEFAULT NULL COMMENT '开始日期',
    `end_date`    DATE         DEFAULT NULL COMMENT '结束日期',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`),
    KEY `idx_education_resume_id` (`resume_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '教育经历';

CREATE TABLE IF NOT EXISTS `resume_work` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `resume_id`   BIGINT       NOT NULL COMMENT '简历id',
    `company`     VARCHAR(100) DEFAULT NULL COMMENT '公司',
    `position`    VARCHAR(100) DEFAULT NULL COMMENT '职位',
    `start_date`  DATE         DEFAULT NULL COMMENT '开始日期',
    `end_date`    DATE         DEFAULT NULL COMMENT '结束日期',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`),
    KEY `idx_work_resume_id` (`resume_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '工作经历';

CREATE TABLE IF NOT EXISTS `resume_project` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `resume_id`    BIGINT       NOT NULL COMMENT '简历id',
    `project_name` VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    `role`         VARCHAR(100) DEFAULT NULL COMMENT '项目角色',
    `start_date`   DATE         DEFAULT NULL COMMENT '开始日期',
    `end_date`     DATE         DEFAULT NULL COMMENT '结束日期',
    `description`  VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`),
    KEY `idx_project_resume_id` (`resume_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '项目经历';

CREATE TABLE IF NOT EXISTS `resume_skill` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `resume_id`   BIGINT       NOT NULL COMMENT '简历id',
    `skill_name`  VARCHAR(50)  NOT NULL COMMENT '技能名称',
    `proficiency` VARCHAR(30)  DEFAULT NULL COMMENT '熟练度',
    PRIMARY KEY (`id`),
    KEY `idx_skill_resume_id` (`resume_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '技能标签';
