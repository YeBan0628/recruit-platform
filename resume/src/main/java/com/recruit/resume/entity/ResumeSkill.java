package com.recruit.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("resume_skill")
public class ResumeSkill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resumeId;
    private String skillName;
    private String proficiency;
}
