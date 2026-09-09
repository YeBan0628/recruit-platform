package com.recruit.notification.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.api.delivery.DeliveryStatusChangedEvent;
import com.recruit.notification.entity.Notification;
import com.recruit.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@RocketMQMessageListener(topic = "delivery-status-changed", consumerGroup = "notification-status-group")
public class DeliveryStatusChangedConsumer implements RocketMQListener<DeliveryStatusChangedEvent> {

    private final NotificationMapper notificationMapper;

    @Override
    public void onMessage(DeliveryStatusChangedEvent event) {
        if (event == null || event.getEventId() == null || event.getNewStatus() == null) {
            return;
        }
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getEventId, event.getEventId()));
        if (count != null && count > 0) {
            return;
        }
        Integer newStatus = event.getNewStatus();
        String type = typeOf(newStatus);
        String content = contentOf(newStatus);
        Long receiverId = newStatus == 5 ? event.getRecruiterId() : event.getApplicantId();
        if (type == null || content == null || receiverId == null) {
            log.warn("忽略无法识别的投递状态事件，eventId={}, status={}", event.getEventId(), newStatus);
            return;
        }
        Notification notification = new Notification();
        notification.setEventId(event.getEventId());
        notification.setDeliveryId(event.getDeliveryId());
        notification.setReceiverId(receiverId);
        notification.setType(type);
        notification.setContent(content);
        notification.setStatus(0);
        notificationMapper.insert(notification);
    }

    private String typeOf(Integer status) {
        return switch (status) {
            case 1 -> "DELIVERY_VIEWED";
            case 2 -> "DELIVERY_INTERVIEW";
            case 3 -> "DELIVERY_HIRED";
            case 4 -> "DELIVERY_REJECTED";
            case 5 -> "DELIVERY_WITHDRAWN";
            default -> null;
        };
    }

    private String contentOf(Integer status) {
        return switch (status) {
            case 1 -> "招聘方已查看你的投递";
            case 2 -> "你的投递进入面试环节";
            case 3 -> "恭喜，你已被录用";
            case 4 -> "很遗憾，你的投递未通过";
            case 5 -> "求职者撤回了一份职位投递";
            default -> null;
        };
    }
}
