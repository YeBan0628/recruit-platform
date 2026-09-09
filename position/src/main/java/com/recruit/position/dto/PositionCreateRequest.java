package com.recruit.position.dto;

import lombok.Data;

/**
 * 发布职位请求
 */
@Data
public class PositionCreateRequest {

    private Long userId;
    private String title;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String experience;
    private String education;
    private String description;
}
