package com.recruit.resume.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.api.position.PositionApi;
import com.recruit.api.position.PositionDTO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.resume.dto.MatchResult;
import com.recruit.resume.dto.ResumeDetailResponse;
import com.recruit.resume.entity.Resume;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DeepSeekMatchServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesStructuredModelResponse() {
        DeepSeekMatchService service = service(true, "key");

        MatchResult result = service.parseResult(
                "```json\n{\"score\":88,\"summary\":\"匹配良好\",\"strengths\":[\"Java\"],\"gaps\":[\"英语\"]}\n```");

        assertEquals(88, result.getScore());
        assertEquals("匹配良好", result.getSummary());
        assertEquals(1, result.getStrengths().size());
        assertEquals("英语", result.getGaps().get(0));
    }

    @Test
    void rejectsOutOfRangeScore() {
        DeepSeekMatchService service = service(true, "key");

        assertThrows(RuntimeException.class,
                () -> service.parseResult("{\"score\":101,\"summary\":\"x\"}"));
    }

    @Test
    void usesSpringAiChatClientForMatching() {
        ResumeService resumeService = mock(ResumeService.class);
        PositionApi positionApi = mock(PositionApi.class);
        ChatClient chatClient = mock(ChatClient.class);
        Resume resume = new Resume();
        resume.setName("张三");
        ResumeDetailResponse resumeDetail = new ResumeDetailResponse();
        resumeDetail.setResume(resume);
        resumeDetail.setEducations(List.of());
        resumeDetail.setWorks(List.of());
        resumeDetail.setProjects(List.of());
        resumeDetail.setSkills(List.of());
        PositionDTO position = new PositionDTO();
        position.setTitle("Java 后端工程师");
        when(resumeService.getMine()).thenReturn(resumeDetail);
        when(positionApi.getById(1L)).thenReturn(Result.success(position));
        when(chatClient.call(any(org.springframework.ai.chat.prompt.Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(
                        "{\"score\":90,\"summary\":\"匹配良好\",\"strengths\":[],\"gaps\":[]}"))));
        DeepSeekMatchService service = new DeepSeekMatchService(resumeService, positionApi,
                objectMapper, Optional.of(chatClient), "key", true);

        MatchResult result = service.match(1L);

        assertEquals(90, result.getScore());
    }

    @Test
    void rejectsMatchingWhenAiIsDisabled() {
        ResumeService resumeService = mock(ResumeService.class);
        PositionApi positionApi = mock(PositionApi.class);
        DeepSeekMatchService service = new DeepSeekMatchService(resumeService, positionApi,
                objectMapper, Optional.empty(), "", false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.match(1L));

        assertEquals("AI 匹配暂未配置", exception.getMessage());
        verifyNoInteractions(resumeService, positionApi);
    }

    private DeepSeekMatchService service(boolean enabled, String apiKey) {
        return new DeepSeekMatchService(mock(ResumeService.class), mock(PositionApi.class),
                objectMapper, Optional.of(mock(ChatClient.class)), apiKey, enabled);
    }
}
