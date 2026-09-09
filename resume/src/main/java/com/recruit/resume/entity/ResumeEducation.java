package com.recruit.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("resume_education")
public class ResumeEducation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resumeId;
    private String school;
    private String major;
    private String degree;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
