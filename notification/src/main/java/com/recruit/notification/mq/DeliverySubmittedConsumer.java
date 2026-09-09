package com.recruit.notification.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.api.delivery.DeliverySubmittedEvent;
import com.recruit.notification.entity.Notification;
import com.recruit.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "delivery-submitted", consumerGroup = "notification-delivery-group")
public class DeliverySubmittedConsumer implements RocketMQListener<DeliverySubmittedEvent> {

    private final NotificationMapper notificationMapper;

    @Override
    public void onMessage(DeliverySubmittedEvent event) {
        if (event == null || event.getEventId() == null) {
            return;
        }
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getEventId, event.getEventId()));
        if (count != null && count > 0) {
            return;
        }
        Notification notification = new Notification();
        notification.setEventId(event.getEventId());
        notification.setDeliveryId(event.getDeliveryId());
        notification.setReceiverId(event.getRecruiterId());
        notification.setType("DELIVERY_SUBMITTED");
        notification.setContent("收到新的职位投递");
        notification.setStatus(0);
        notificationMapper.insert(notification);
    }
}
