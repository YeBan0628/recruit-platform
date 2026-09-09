package com.recruit.resume.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.api.position.PositionApi;
import com.recruit.api.position.PositionDTO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.common.result.ResultCode;
import com.recruit.resume.dto.MatchResult;
import com.recruit.resume.dto.ResumeDetailResponse;
import com.recruit.resume.entity.ResumeEducation;
import com.recruit.resume.entity.ResumeProject;
import com.recruit.resume.entity.ResumeSkill;
import com.recruit.resume.entity.ResumeWork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeepSeekMatchService {

    private final ResumeService resumeService;
    private final PositionApi positionApi;
    private final ObjectMapper objectMapper;
    private final Optional<ChatClient> chatClient;
    private final String apiKey;
    private final boolean enabled;

    public DeepSeekMatchService(ResumeService resumeService,
                                PositionApi positionApi,
                                ObjectMapper objectMapper,
                                Optional<ChatClient> chatClient,
                                @Value("${recruit.ai.api-key:}") String apiKey,
                                @Value("${recruit.ai.enabled:false}") boolean enabled) {
        this.resumeService = resumeService;
        this.positionApi = positionApi;
        this.objectMapper = objectMapper;
        this.chatClient = chatClient;
        this.apiKey = apiKey;
        this.enabled = enabled;
    }

    public MatchResult match(Long positionId) {
        if (!enabled || apiKey == null || apiKey.isBlank() || chatClient.isEmpty()) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "AI 匹配暂未配置");
        }
        ResumeDetailResponse resume = resumeService.getMine();
        Result<PositionDTO> positionResult = positionApi.getById(positionId);
        if (positionResult == null || positionResult.getData() == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        String prompt = buildPrompt(resume, positionResult.getData());
        try {
            String content = chatClient.orElseThrow().call(new Prompt(List.of(
                            new SystemMessage("你是专业招聘顾问，只能输出合法 JSON，不要输出 Markdown。"),
                            new UserMessage(prompt))))
                    .getResult().getOutput().getContent();
            return parseResult(content);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("DeepSeek 匹配调用失败，positionId={}", positionId, e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "AI 匹配调用失败");
        }
    }

    String buildPrompt(ResumeDetailResponse resume, PositionDTO position) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下简历和职位信息评估匹配度。评分范围 0-100，必须返回 JSON：")
                .append("{\"score\":整数,\"summary\":\"总体结论\",\"strengths\":[\"优势\"],\"gaps\":[\"缺口\"]}\n\n");
        prompt.append("职位：").append(position.getTitle()).append("\n")
                .append("城市：").append(position.getCity()).append("\n")
                .append("经验要求：").append(position.getExperience()).append("\n")
                .append("学历要求：").append(position.getEducation()).append("\n")
                .append("职位描述：").append(position.getDescription()).append("\n\n");
        prompt.append("简历：").append(resume.getResume().getName()).append("\n")
                .append("个人简介：").append(resume.getResume().getSummary()).append("\n");
        prompt.append("教育经历：\n");
        for (ResumeEducation item : safe(resume.getEducations())) {
            prompt.append(item.getSchool()).append("，").append(item.getMajor()).append("，")
                    .append(item.getDegree()).append("\n");
        }
        prompt.append("工作经历：\n");
        for (ResumeWork item : safe(resume.getWorks())) {
            prompt.append(item.getCompany()).append("，").append(item.getPosition()).append("：")
                    .append(item.getDescription()).append("\n");
        }
        prompt.append("项目经历：\n");
        for (ResumeProject item : safe(resume.getProjects())) {
            prompt.append(item.getProjectName()).append("，角色：").append(item.getRole()).append("：")
                    .append(item.getDescription()).append("\n");
        }
        prompt.append("技能：\n");
        for (ResumeSkill item : safe(resume.getSkills())) {
            prompt.append(item.getSkillName()).append("（").append(item.getProficiency()).append("）\n");
        }
        return prompt.toString();
    }

    MatchResult parseResult(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "AI 返回结果为空");
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "AI 返回格式错误");
        }
        try {
            MatchResult result = objectMapper.readValue(content.substring(start, end + 1), MatchResult.class);
            if (result.getScore() == null || result.getScore() < 0 || result.getScore() > 100) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "AI 返回分数不合法");
            }
            if (result.getStrengths() == null) {
                result.setStrengths(List.of());
            }
            if (result.getGaps() == null) {
                result.setGaps(List.of());
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "AI 返回格式错误");
        }
    }

    private <T> List<T> safe(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}
