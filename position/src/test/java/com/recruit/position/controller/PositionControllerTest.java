package com.recruit.position.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.api.position.PositionDTO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.common.result.ResultCode;
import com.recruit.position.dto.PositionCreateRequest;
import com.recruit.position.entity.Position;
import com.recruit.position.mapper.PositionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PositionControllerTest {

    private PositionMapper positionMapper;
    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;
    private ZSetOperations<String, String> zSetOperations;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private PositionController controller;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        positionMapper = mock(PositionMapper.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        zSetOperations = mock(ZSetOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        controller = new PositionController(positionMapper, stringRedisTemplate, objectMapper);
    }

    @Test
    void createRejectsInvertedSalaryRange() {
        PositionCreateRequest request = createRequest();
        request.setSalaryMin(40);
        request.setSalaryMax(20);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.create(request));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        assertEquals("最低薪资不能高于最高薪资", exception.getMessage());
        verifyNoInteractions(positionMapper);
    }

    @Test
    void createPublishesPositionForCurrentUser() {
        doAnswer(invocation -> {
            Position position = invocation.getArgument(0);
            position.setId(10L);
            return 1;
        }).when(positionMapper).insert(any(Position.class));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            Result<Long> result = controller.create(createRequest());

            assertEquals(10L, result.getData());
        }
        ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
        verify(positionMapper).insert(captor.capture());
        Position saved = captor.getValue();
        assertEquals(100L, saved.getUserId());
        assertEquals(1, saved.getStatus());
        assertEquals("Java 工程师", saved.getTitle());
        verify(stringRedisTemplate).delete("position:detail:10");
    }

    @Test
    void createRejectsAnonymousUser() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> controller.create(createRequest()));

            assertEquals(ResultCode.UNAUTHORIZED.getCode(), exception.getCode());
        }
        verifyNoInteractions(positionMapper);
    }

    @Test
    void updateRejectsForeignPosition() {
        Position owned = position(1L, 200L, 1);
        when(positionMapper.selectById(1L)).thenReturn(owned);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> controller.updateStatus(1L, 0));

            assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
        }
    }

    @Test
    void updateRejectsPositionNotFound() {
        when(positionMapper.selectById(99L)).thenReturn(null);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> controller.updateStatus(99L, 0));

            assertEquals(ResultCode.NOT_FOUND.getCode(), exception.getCode());
        }
    }

    @Test
    void updateStatusRejectsInvalidValue() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.updateStatus(1L, 5));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        verifyNoInteractions(positionMapper);
    }

    @Test
    void getByIdReturnsCachedDtoWithoutDbAccess() throws Exception {
        PositionDTO cached = new PositionDTO();
        cached.setId(1L);
        cached.setTitle("Java 工程师");
        when(valueOperations.get("position:detail:1"))
                .thenReturn(objectMapper.writeValueAsString(cached));

        Result<PositionDTO> result = controller.getById(1L);

        assertEquals("Java 工程师", result.getData().getTitle());
        verifyNoInteractions(positionMapper);
        verify(zSetOperations).incrementScore("position:hot:score", "1", 1D);
    }

    @Test
    void getByIdFallsBackToDbAndWritesCacheOnMiss() {
        when(valueOperations.get("position:detail:1")).thenReturn(null);
        when(positionMapper.selectById(1L)).thenReturn(position(1L, 100L, 1));

        Result<PositionDTO> result = controller.getById(1L);

        assertEquals(1L, result.getData().getId());
        assertEquals("Java 工程师", result.getData().getTitle());
        verify(valueOperations).set(eq("position:detail:1"), anyString(),
                eq(Duration.ofMinutes(30)));
    }

    @Test
    void getByIdReturnsNullDataWhenPositionMissing() {
        when(valueOperations.get("position:detail:99")).thenReturn(null);
        when(positionMapper.selectById(99L)).thenReturn(null);

        Result<PositionDTO> result = controller.getById(99L);

        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void hotRejectsInvalidLimit() {
        assertThrows(BusinessException.class, () -> controller.hot(0));
        assertThrows(BusinessException.class, () -> controller.hot(101));
    }

    @Test
    void hotReturnsEmptyListWhenRedisFails() {
        when(stringRedisTemplate.opsForZSet()).thenThrow(new IllegalStateException("redis down"));

        Result<List<PositionDTO>> result = controller.hot(10);

        assertTrue(result.getData().isEmpty());
    }

    @Test
    void hotFiltersOfflineAndUnknownPositions() {
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("1", 30D));
        tuples.add(new DefaultTypedTuple<>("2", 20D));
        tuples.add(new DefaultTypedTuple<>("3", 10D));
        when(zSetOperations.reverseRangeWithScores("position:hot:score", 0, 9L))
                .thenReturn(tuples);
        when(positionMapper.selectById(1L)).thenReturn(position(1L, 100L, 1));
        when(positionMapper.selectById(2L)).thenReturn(position(2L, 100L, 0));
        when(positionMapper.selectById(3L)).thenReturn(null);

        Result<List<PositionDTO>> result = controller.hot(10);

        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getData().get(0).getId());
        verify(zSetOperations).remove("position:hot:score", "2");
        verify(zSetOperations).remove("position:hot:score", "3");
    }

    @Test
    void pageRejectsInvalidParams() {
        assertThrows(BusinessException.class, () -> controller.page(0L, 10L, null, null));
        assertThrows(BusinessException.class, () -> controller.page(1L, 0L, null, null));
        assertThrows(BusinessException.class, () -> controller.page(1L, 101L, null, null));
        verifyNoInteractions(positionMapper);
    }

    @Test
    void pageReturnsMapperResult() {
        Page<Position> page = new Page<>(1, 10);
        page.setRecords(List.of(position(1L, 100L, 1)));
        page.setTotal(1);
        when(positionMapper.selectPage(any(), any())).thenReturn(page);

        Result<Page<Position>> result = controller.page(1L, 10L, "北京", "Java");

        assertEquals(1, result.getData().getTotal());
        assertEquals("Java 工程师", result.getData().getRecords().get(0).getTitle());
    }

    @Test
    void listByUserQueriesCurrentUsersPositions() {
        Page<Position> page = new Page<>(1, 10);
        page.setRecords(List.of(position(1L, 100L, 1)));
        when(positionMapper.selectPage(any(), any())).thenReturn(page);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            Result<Page<Position>> result = controller.listByUser(1L, 10L);

            assertEquals(1, result.getData().getRecords().size());
        }
    }

    private PositionCreateRequest createRequest() {
        PositionCreateRequest request = new PositionCreateRequest();
        request.setTitle("Java 工程师");
        request.setCity("北京");
        request.setSalaryMin(20);
        request.setSalaryMax(40);
        request.setExperience("3-5年");
        request.setEducation("本科");
        request.setDescription("负责后端服务开发");
        return request;
    }

    private Position position(Long id, Long userId, Integer status) {
        Position position = new Position();
        position.setId(id);
        position.setUserId(userId);
        position.setTitle("Java 工程师");
        position.setCity("北京");
        position.setSalaryMin(20);
        position.setSalaryMax(40);
        position.setStatus(status);
        return position;
    }
}
