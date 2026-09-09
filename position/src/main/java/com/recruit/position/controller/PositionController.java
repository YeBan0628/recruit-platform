package com.recruit.position.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.api.position.PositionApi;
import com.recruit.api.position.PositionDTO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.common.result.ResultCode;
import com.recruit.position.dto.PositionCreateRequest;
import com.recruit.position.dto.PositionUpdateRequest;
import com.recruit.position.entity.Position;
import com.recruit.position.mapper.PositionMapper;
import lombok.RequiredArgsConstructor;
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

/**
 * 职位接口
 */
@RestController
@RequiredArgsConstructor
public class PositionController implements PositionApi {

    private final PositionMapper positionMapper;

    /** 发布职位 */
    @PostMapping("/position")
    public Result<Long> create(@RequestBody PositionCreateRequest request) {
        Position position = new Position();
        BeanUtils.copyProperties(request, position);
        position.setStatus(1);
        positionMapper.insert(position);
        return Result.success(position.getId());
    }

    /** 更新职位（仅发布者本人可操作） */
    @PutMapping("/position/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody PositionUpdateRequest request) {
        Position position = getOwnedPosition(id, request.getUserId());
        BeanUtils.copyProperties(request, position, "userId");
        positionMapper.updateById(position);
        return Result.success();
    }

    /** 上架/下架职位（仅发布者本人可操作） */
    @PutMapping("/position/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") Integer status,
                                      @RequestParam("userId") Long userId) {
        Position position = getOwnedPosition(id, userId);
        position.setStatus(status);
        positionMapper.updateById(position);
        return Result.success();
    }

    /** 删除职位（仅发布者本人可操作） */
    @DeleteMapping("/position/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
        getOwnedPosition(id, userId);
        positionMapper.deleteById(id);
        return Result.success();
    }

    /** 职位详情（供前端及 Feign 调用） */
    @Override
    @GetMapping("/position/{id}")
    public Result<PositionDTO> getById(@PathVariable("id") Long id) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            return Result.success(null);
        }
        PositionDTO dto = new PositionDTO();
        BeanUtils.copyProperties(position, dto);
        return Result.success(dto);
    }

    /** 职位分页搜索（仅展示上架中的职位） */
    @GetMapping("/position/page")
    public Result<Page<Position>> page(@RequestParam(value = "page", defaultValue = "1") Long page,
                                        @RequestParam(value = "size", defaultValue = "10") Long size,
                                        @RequestParam(value = "city", required = false) String city,
                                        @RequestParam(value = "keyword", required = false) String keyword) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1)
                .like(StringUtils.hasText(city), Position::getCity, city)
                .like(StringUtils.hasText(keyword), Position::getTitle, keyword)
                .orderByDesc(Position::getCreateTime);
        Page<Position> result = positionMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    /** 我发布的职位（管理端，含上架/下架全部状态） */
    @GetMapping("/position/user/{userId}")
    public Result<Page<Position>> listByUser(@PathVariable("userId") Long userId,
                                              @RequestParam(value = "page", defaultValue = "1") Long page,
                                              @RequestParam(value = "size", defaultValue = "10") Long size) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, userId)
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
}
