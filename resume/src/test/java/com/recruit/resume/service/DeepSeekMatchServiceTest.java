package com.recruit.resume.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruit.api.position.PositionApi;
import com.recruit.resume.dto.MatchResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class DeepSeekMatchServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesStructuredModelResponse() {
        DeepSeekMatchService service = service(true, "key");
        ObjectNode response = objectMapper.createObjectNode();
        ObjectNode message = response.putArray("choices").addObject().putObject("message");
        message.put("content", "```json\n{\"score\":88,\"summary\":\"匹配良好\",\"strengths\":[\"Java\"],\"gaps\":[\"英语\"]}\n```");

        MatchResult result = service.parseResult(response);

        assertEquals(88, result.getScore());
        assertEquals("匹配良好", result.getSummary());
        assertEquals(1, result.getStrengths().size());
        assertEquals("英语", result.getGaps().get(0));
    }

    @Test
    void rejectsOutOfRangeScore() {
        DeepSeekMatchService service = service(true, "key");
        ObjectNode response = objectMapper.createObjectNode();
        ObjectNode message = response.putArray("choices").addObject().putObject("message");
        message.put("content", "{\"score\":101,\"summary\":\"x\"}");

        assertThrows(RuntimeException.class, () -> service.parseResult(response));
    }

    private DeepSeekMatchService service(boolean enabled, String apiKey) {
        return new DeepSeekMatchService(mock(ResumeService.class), mock(PositionApi.class),
                objectMapper, "https://api.deepseek.com", apiKey, "deepseek-chat", enabled);
    }
}
