package com.recruit.position.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位实体
 */
@Data
@TableName("`position`")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者用户id（招聘方） */
    private Long userId;

    private String title;

    private String city;

    /** 薪资下限(K) */
    private Integer salaryMin;

    /** 薪资上限(K) */
    private Integer salaryMax;

    /** 经验要求 */
    private String experience;

    /** 学历要求 */
    private String education;

    private String description;

    /** 状态 1上架 0下架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
