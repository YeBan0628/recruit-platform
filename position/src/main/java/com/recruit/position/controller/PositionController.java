package com.recruit.position.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.stp.StpUtil;
import com.recruit.api.position.PositionApi;
import com.recruit.api.position.PositionDTO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.common.result.ResultCode;
import com.recruit.position.dto.PositionCreateRequest;
import com.recruit.position.dto.PositionUpdateRequest;
import com.recruit.position.entity.Position;
import com.recruit.position.mapper.PositionMapper;
import jakarta.validation.Valid;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 职位接口
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PositionController implements PositionApi {

    private final PositionMapper positionMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DETAIL_KEY_PREFIX = "position:detail:";
    private static final String HOT_KEY = "position:hot:score";
    private static final Duration DETAIL_CACHE_TTL = Duration.ofMinutes(30);

    /** 发布职位 */
    @PostMapping("/position")
    public Result<Long> create(@Valid @RequestBody PositionCreateRequest request) {
        validateSalary(request.getSalaryMin(), request.getSalaryMax());
        Position position = new Position();
        BeanUtils.copyProperties(request, position);
        position.setUserId(currentUserId());
        position.setStatus(1);
        positionMapper.insert(position);
        evictDetailCache(position.getId());
        return Result.success(position.getId());
    }

    /** 更新职位（仅发布者本人可操作） */
    @PutMapping("/position/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @Valid @RequestBody PositionUpdateRequest request) {
        validateSalary(request.getSalaryMin(), request.getSalaryMax());
        Position position = getOwnedPosition(id, currentUserId());
        BeanUtils.copyProperties(request, position);
        positionMapper.updateById(position);
        evictDetailCache(id);
        return Result.success();
    }

    /** 上架/下架职位（仅发布者本人可操作） */
    @PutMapping("/position/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "职位状态只能是0或1");
        }
        Position position = getOwnedPosition(id, currentUserId());
        position.setStatus(status);
        positionMapper.updateById(position);
        evictDetailCache(id);
        return Result.success();
    }

    /** 删除职位（仅发布者本人可操作） */
    @DeleteMapping("/position/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        getOwnedPosition(id, currentUserId());
        positionMapper.deleteById(id);
        evictDetailCache(id);
        removeHotScore(id);
        return Result.success();
    }

    /** 职位详情（供前端及 Feign 调用） */
    @Override
    @GetMapping("/position/{id}")
    public Result<PositionDTO> getById(@PathVariable("id") Long id) {
        String cacheKey = DETAIL_KEY_PREFIX + id;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cached)) {
                PositionDTO dto = objectMapper.readValue(cached, PositionDTO.class);
                increaseHotScore(id, 1D);
                return Result.success(dto);
            }
        } catch (Exception e) {
            log.warn("读取职位缓存失败，回源数据库，positionId={}", id, e);
            try {
                stringRedisTemplate.delete(cacheKey);
            } catch (Exception ignored) {
                // Redis 故障时直接回源数据库
            }
        }
        Position position = positionMapper.selectById(id);
        if (position == null) {
            return Result.success(null);
        }
        PositionDTO dto = toDto(position);
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(dto), DETAIL_CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入职位缓存失败，positionId={}", id, e);
        }
        increaseHotScore(id, 1D);
        return Result.success(dto);
    }

    /** 热度榜（仅展示上架中的职位） */
    @GetMapping("/position/hot")
    public Result<List<PositionDTO>> hot(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        if (limit == null || limit < 1 || limit > 100) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "limit 参数不合法");
        }
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                    .reverseRangeWithScores(HOT_KEY, 0, limit - 1L);
            if (tuples == null || tuples.isEmpty()) {
                return Result.success(Collections.emptyList());
            }
            List<PositionDTO> result = new ArrayList<>();
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String value = tuple.getValue();
                if (!StringUtils.hasText(value)) {
                    continue;
                }
                Long positionId;
                try {
                    positionId = Long.valueOf(value);
                } catch (NumberFormatException e) {
                    stringRedisTemplate.opsForZSet().remove(HOT_KEY, value);
                    continue;
                }
                Position position = positionMapper.selectById(positionId);
                if (position == null || !Integer.valueOf(1).equals(position.getStatus())) {
                    stringRedisTemplate.opsForZSet().remove(HOT_KEY, value);
                    evictDetailCache(positionId);
                    continue;
                }
                result.add(toDto(position));
            }
            return Result.success(result);
        } catch (Exception e) {
            log.warn("查询职位热度榜失败", e);
            return Result.success(Collections.emptyList());
        }
    }

    /** 职位分页搜索（仅展示上架中的职位） */
    @GetMapping("/position/page")
    public Result<Page<Position>> page(@RequestParam(value = "page", defaultValue = "1") Long page,
                                        @RequestParam(value = "size", defaultValue = "10") Long size,
                                        @RequestParam(value = "city", required = false) String city,
                                        @RequestParam(value = "keyword", required = false) String keyword) {
        validatePage(page, size);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1)
                .like(StringUtils.hasText(city), Position::getCity, city)
                .like(StringUtils.hasText(keyword), Position::getTitle, keyword)
                .orderByDesc(Position::getCreateTime);
        Page<Position> result = positionMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    /** 我发布的职位（管理端，含上架/下架全部状态） */
    @GetMapping("/position/me")
    public Result<Page<Position>> listByUser(
                                              @RequestParam(value = "page", defaultValue = "1") Long page,
                                              @RequestParam(value = "size", defaultValue = "10") Long size) {
        validatePage(page, size);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, currentUserId())
                .orderByDesc(Position::getCreateTime);
        Page<Position> result = positionMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    private Position getOwnedPosition(Long id, Long userId) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!position.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return position;
    }

    private Long currentUserId() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return StpUtil.getLoginIdAsLong();
    }

    private PositionDTO toDto(Position position) {
        PositionDTO dto = new PositionDTO();
        BeanUtils.copyProperties(position, dto);
        return dto;
    }

    private void evictDetailCache(Long id) {
        try {
            stringRedisTemplate.delete(DETAIL_KEY_PREFIX + id);
        } catch (Exception e) {
            log.warn("删除职位缓存失败，positionId={}", id, e);
        }
    }

    private void increaseHotScore(Long id, double score) {
        try {
            stringRedisTemplate.opsForZSet().incrementScore(HOT_KEY, String.valueOf(id), score);
        } catch (Exception e) {
            log.warn("增加职位热度失败，positionId={}", id, e);
        }
    }

    private void removeHotScore(Long id) {
        try {
            stringRedisTemplate.opsForZSet().remove(HOT_KEY, String.valueOf(id));
        } catch (Exception e) {
            log.warn("删除职位热度失败，positionId={}", id, e);
        }
    }

    private void validateSalary(Integer salaryMin, Integer salaryMax) {
        if (salaryMin != null && salaryMax != null && salaryMin > salaryMax) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "最低薪资不能高于最高薪资");
        }
    }

    private void validatePage(Long page, Long size) {
        if (page == null || page < 1 || size == null || size < 1 || size > 100) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "分页参数不合法");
        }
    }
}
