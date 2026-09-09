package com.recruit.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SkillRequest {
    @NotBlank(message = "技能名称不能为空")
    @Size(max = 50, message = "技能名称不能超过50个字符")
    private String skillName;
    @Size(max = 30, message = "熟练度不能超过30个字符")
    private String proficiency;
}
