package com.recruit.resume.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkRequest {
    @Size(max = 100, message = "公司不能超过100个字符")
    private String company;
    @Size(max = 100, message = "职位不能超过100个字符")
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 1000, message = "描述不能超过1000个字符")
    private String description;
}
