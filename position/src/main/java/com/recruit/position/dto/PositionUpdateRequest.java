package com.recruit.position.dto;

import lombok.Data;

/**
 * 更新职位请求
 */
@Data
public class PositionUpdateRequest {

    /** 操作人用户id，用于校验是否为职位发布者本人 */
    private Long userId;
    private String title;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String experience;
    private String education;
    private String description;
}
