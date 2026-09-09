package com.recruit.api.position;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位信息 DTO
 */
@Data
public class PositionDTO implements Serializable {

    private Long id;
    private Long userId;
    private String title;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String experience;
    private String education;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
}
