package com.recruit.delivery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("delivery")
public class Delivery {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long positionId;
    private Long applicantId;
    private Integer status;
    private String message;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
