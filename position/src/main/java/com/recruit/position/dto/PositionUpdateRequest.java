package com.recruit.position.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 更新职位请求
 */
@Data
public class PositionUpdateRequest {

    @Size(max = 50, message = "职位名称不能超过50个字符")
    private String title;

    @Size(max = 30, message = "城市不能超过30个字符")
    private String city;

    @Min(value = 0, message = "最低薪资不能为负数")
    private Integer salaryMin;

    @Min(value = 0, message = "最高薪资不能为负数")
    private Integer salaryMax;

    @Size(max = 20, message = "经验要求不能超过20个字符")
    private String experience;
    private String education;
    private String description;
}
