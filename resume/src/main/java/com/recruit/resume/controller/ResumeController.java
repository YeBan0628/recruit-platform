package com.recruit.resume.controller;

import com.recruit.common.result.Result;
import com.recruit.resume.dto.EducationRequest;
import com.recruit.resume.dto.ProjectRequest;
import com.recruit.resume.dto.ResumeDetailResponse;
import com.recruit.resume.dto.ResumeSaveRequest;
import com.recruit.resume.dto.MatchResult;
import com.recruit.resume.dto.SkillRequest;
import com.recruit.resume.dto.WorkRequest;
import com.recruit.resume.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final com.recruit.resume.service.DeepSeekMatchService deepSeekMatchService;

    @PostMapping("/match/{positionId}")
    public Result<MatchResult> match(@PathVariable Long positionId) {
        return Result.success(deepSeekMatchService.match(positionId));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ResumeSaveRequest request) {
        return Result.success(resumeService.create(request));
    }

    @GetMapping("/me")
    public Result<ResumeDetailResponse> getMine() {
        return Result.success(resumeService.getMine());
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ResumeSaveRequest request) {
        resumeService.update(request);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> delete() {
        resumeService.delete();
        return Result.success();
    }

    @PostMapping("/education")
    public Result<Long> addEducation(@Valid @RequestBody EducationRequest request) {
        return Result.success(resumeService.addEducation(request));
    }

    @PutMapping("/education/{id}")
    public Result<Void> updateEducation(@PathVariable Long id, @Valid @RequestBody EducationRequest request) {
        resumeService.updateEducation(id, request);
        return Result.success();
    }

    @DeleteMapping("/education/{id}")
    public Result<Void> deleteEducation(@PathVariable Long id) {
        resumeService.deleteEducation(id);
        return Result.success();
    }

    @PostMapping("/work")
    public Result<Long> addWork(@Valid @RequestBody WorkRequest request) {
        return Result.success(resumeService.addWork(request));
    }

    @PutMapping("/work/{id}")
    public Result<Void> updateWork(@PathVariable Long id, @Valid @RequestBody WorkRequest request) {
        resumeService.updateWork(id, request);
        return Result.success();
    }

    @DeleteMapping("/work/{id}")
    public Result<Void> deleteWork(@PathVariable Long id) {
        resumeService.deleteWork(id);
        return Result.success();
    }

    @PostMapping("/project")
    public Result<Long> addProject(@Valid @RequestBody ProjectRequest request) {
        return Result.success(resumeService.addProject(request));
    }

    @PutMapping("/project/{id}")
    public Result<Void> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        resumeService.updateProject(id, request);
        return Result.success();
    }

    @DeleteMapping("/project/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        resumeService.deleteProject(id);
        return Result.success();
    }

    @PostMapping("/skill")
    public Result<Long> addSkill(@Valid @RequestBody SkillRequest request) {
        return Result.success(resumeService.addSkill(request));
    }

    @PutMapping("/skill/{id}")
    public Result<Void> updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        resumeService.updateSkill(id, request);
        return Result.success();
    }

    @DeleteMapping("/skill/{id}")
    public Result<Void> deleteSkill(@PathVariable Long id) {
        resumeService.deleteSkill(id);
        return Result.success();
    }
}
