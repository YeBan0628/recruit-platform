package com.recruit.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruit.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
