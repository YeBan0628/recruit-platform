package com.recruit.resume.dto;

import com.recruit.resume.entity.Resume;
import com.recruit.resume.entity.ResumeEducation;
import com.recruit.resume.entity.ResumeProject;
import com.recruit.resume.entity.ResumeSkill;
import com.recruit.resume.entity.ResumeWork;
import lombok.Data;

import java.util.List;

@Data
public class ResumeDetailResponse {
    private Resume resume;
    private List<ResumeEducation> educations;
    private List<ResumeWork> works;
    private List<ResumeProject> projects;
    private List<ResumeSkill> skills;
}
