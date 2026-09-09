package com.recruit.resume.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EducationRequest {
    @Size(max = 100, message = "学校不能超过100个字符")
    private String school;
    @Size(max = 100, message = "专业不能超过100个字符")
    private String major;
    @Size(max = 30, message = "学历不能超过30个字符")
    private String degree;
    private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}
