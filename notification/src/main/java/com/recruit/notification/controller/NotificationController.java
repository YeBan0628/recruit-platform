package com.recruit.notification.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.common.result.ResultCode;
import com.recruit.notification.entity.Notification;
import com.recruit.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;

    @GetMapping("/me")
    public Result<Page<Notification>> listMine(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        validatePage(page, size);
        Page<Notification> result = notificationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getReceiverId, currentUserId())
                        .orderByDesc(Notification::getCreateTime));
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, currentUserId())
                .eq(Notification::getStatus, 0));
        return Result.success(count == null ? 0L : count);
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!currentUserId().equals(notification.getReceiverId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!Integer.valueOf(1).equals(notification.getStatus())) {
            notification.setStatus(1);
            notificationMapper.updateById(notification);
        }
        return Result.success();
    }

    private Long currentUserId() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return StpUtil.getLoginIdAsLong();
    }

    private void validatePage(Long page, Long size) {
        if (page == null || page < 1 || size == null || size < 1 || size > 100) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "分页参数不合法");
        }
    }
}
