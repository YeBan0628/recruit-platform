package com.recruit.resume.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequest {
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String projectName;
    @Size(max = 100, message = "角色不能超过100个字符")
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 1000, message = "描述不能超过1000个字符")
    private String description;
}
