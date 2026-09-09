package com.recruit.resume.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.ResultCode;
import com.recruit.resume.dto.EducationRequest;
import com.recruit.resume.dto.ProjectRequest;
import com.recruit.resume.dto.ResumeDetailResponse;
import com.recruit.resume.dto.ResumeSaveRequest;
import com.recruit.resume.dto.SkillRequest;
import com.recruit.resume.dto.WorkRequest;
import com.recruit.resume.entity.Resume;
import com.recruit.resume.entity.ResumeEducation;
import com.recruit.resume.entity.ResumeProject;
import com.recruit.resume.entity.ResumeSkill;
import com.recruit.resume.entity.ResumeWork;
import com.recruit.resume.mapper.ResumeEducationMapper;
import com.recruit.resume.mapper.ResumeMapper;
import com.recruit.resume.mapper.ResumeProjectMapper;
import com.recruit.resume.mapper.ResumeSkillMapper;
import com.recruit.resume.mapper.ResumeWorkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeMapper resumeMapper;
    private final ResumeEducationMapper educationMapper;
    private final ResumeWorkMapper workMapper;
    private final ResumeProjectMapper projectMapper;
    private final ResumeSkillMapper skillMapper;

    @Transactional
    public Long create(ResumeSaveRequest request) {
        Long userId = currentUserId();
        Resume exists = resumeMapper.selectOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, userId));
        if (exists != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前用户已有简历");
        }
        Resume resume = new Resume();
        BeanUtils.copyProperties(request, resume);
        resume.setUserId(userId);
        if (resume.getStatus() == null) {
            resume.setStatus(1);
        }
        validateStatus(resume.getStatus());
        resumeMapper.insert(resume);
        return resume.getId();
    }

    public ResumeDetailResponse getMine() {
        Resume resume = getOwnedResume();
        ResumeDetailResponse response = new ResumeDetailResponse();
        response.setResume(resume);
        response.setEducations(educationMapper.selectList(new LambdaQueryWrapper<ResumeEducation>()
                .eq(ResumeEducation::getResumeId, resume.getId())
                .orderByDesc(ResumeEducation::getStartDate)));
        response.setWorks(workMapper.selectList(new LambdaQueryWrapper<ResumeWork>()
                .eq(ResumeWork::getResumeId, resume.getId())
                .orderByDesc(ResumeWork::getStartDate)));
        response.setProjects(projectMapper.selectList(new LambdaQueryWrapper<ResumeProject>()
                .eq(ResumeProject::getResumeId, resume.getId())
                .orderByDesc(ResumeProject::getStartDate)));
        response.setSkills(skillMapper.selectList(new LambdaQueryWrapper<ResumeSkill>()
                .eq(ResumeSkill::getResumeId, resume.getId())
                .orderByAsc(ResumeSkill::getSkillName)));
        return response;
    }

    @Transactional
    public void update(ResumeSaveRequest request) {
        Resume resume = getOwnedResume();
        Integer currentStatus = resume.getStatus();
        BeanUtils.copyProperties(request, resume);
        if (resume.getStatus() == null) {
            resume.setStatus(currentStatus);
        }
        validateStatus(resume.getStatus());
        resumeMapper.updateById(resume);
    }

    @Transactional
    public void delete() {
        Resume resume = getOwnedResume();
        educationMapper.delete(new LambdaQueryWrapper<ResumeEducation>().eq(ResumeEducation::getResumeId, resume.getId()));
        workMapper.delete(new LambdaQueryWrapper<ResumeWork>().eq(ResumeWork::getResumeId, resume.getId()));
        projectMapper.delete(new LambdaQueryWrapper<ResumeProject>().eq(ResumeProject::getResumeId, resume.getId()));
        skillMapper.delete(new LambdaQueryWrapper<ResumeSkill>().eq(ResumeSkill::getResumeId, resume.getId()));
        resumeMapper.deleteById(resume.getId());
    }

    @Transactional
    public Long addEducation(EducationRequest request) {
        ResumeEducation education = new ResumeEducation();
        BeanUtils.copyProperties(request, education);
        education.setResumeId(getOwnedResume().getId());
        validateDates(education.getStartDate(), education.getEndDate());
        educationMapper.insert(education);
        return education.getId();
    }

    @Transactional
    public void updateEducation(Long id, EducationRequest request) {
        ResumeEducation education = ownedEducation(id);
        BeanUtils.copyProperties(request, education);
        validateDates(education.getStartDate(), education.getEndDate());
        educationMapper.updateById(education);
    }

    @Transactional
    public void deleteEducation(Long id) {
        educationMapper.deleteById(ownedEducation(id).getId());
    }

    @Transactional
    public Long addWork(WorkRequest request) {
        ResumeWork work = new ResumeWork();
        BeanUtils.copyProperties(request, work);
        work.setResumeId(getOwnedResume().getId());
        validateDates(work.getStartDate(), work.getEndDate());
        workMapper.insert(work);
        return work.getId();
    }

    @Transactional
    public void updateWork(Long id, WorkRequest request) {
        ResumeWork work = ownedWork(id);
        BeanUtils.copyProperties(request, work);
        validateDates(work.getStartDate(), work.getEndDate());
        workMapper.updateById(work);
    }

    @Transactional
    public void deleteWork(Long id) {
        workMapper.deleteById(ownedWork(id).getId());
    }

    @Transactional
    public Long addProject(ProjectRequest request) {
        ResumeProject project = new ResumeProject();
        BeanUtils.copyProperties(request, project);
        project.setResumeId(getOwnedResume().getId());
        validateDates(project.getStartDate(), project.getEndDate());
        projectMapper.insert(project);
        return project.getId();
    }

    @Transactional
    public void updateProject(Long id, ProjectRequest request) {
        ResumeProject project = ownedProject(id);
        BeanUtils.copyProperties(request, project);
        validateDates(project.getStartDate(), project.getEndDate());
        projectMapper.updateById(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectMapper.deleteById(ownedProject(id).getId());
    }

    @Transactional
    public Long addSkill(SkillRequest request) {
        ResumeSkill skill = new ResumeSkill();
        BeanUtils.copyProperties(request, skill);
        skill.setResumeId(getOwnedResume().getId());
        skillMapper.insert(skill);
        return skill.getId();
    }

    @Transactional
    public void updateSkill(Long id, SkillRequest request) {
        ResumeSkill skill = ownedSkill(id);
        BeanUtils.copyProperties(request, skill);
        skillMapper.updateById(skill);
    }

    @Transactional
    public void deleteSkill(Long id) {
        skillMapper.deleteById(ownedSkill(id).getId());
    }

    private Resume getOwnedResume() {
        Resume resume = resumeMapper.selectOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, currentUserId()));
        if (resume == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "请先创建简历");
        }
        return resume;
    }

    private ResumeEducation ownedEducation(Long id) {
        Resume resume = getOwnedResume();
        ResumeEducation education = educationMapper.selectById(id);
        if (education == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        checkOwner(education.getResumeId(), resume.getId());
        return education;
    }

    private ResumeWork ownedWork(Long id) {
        Resume resume = getOwnedResume();
        ResumeWork work = workMapper.selectById(id);
        if (work == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        checkOwner(work.getResumeId(), resume.getId());
        return work;
    }

    private ResumeProject ownedProject(Long id) {
        Resume resume = getOwnedResume();
        ResumeProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        checkOwner(project.getResumeId(), resume.getId());
        return project;
    }

    private ResumeSkill ownedSkill(Long id) {
        Resume resume = getOwnedResume();
        ResumeSkill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        checkOwner(skill.getResumeId(), resume.getId());
        return skill;
    }

    private void checkOwner(Long childResumeId, Long resumeId) {
        if (!resumeId.equals(childResumeId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private Long currentUserId() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return StpUtil.getLoginIdAsLong();
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "简历状态只能是0或1");
        }
    }

    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "开始日期不能晚于结束日期");
        }
    }
}
