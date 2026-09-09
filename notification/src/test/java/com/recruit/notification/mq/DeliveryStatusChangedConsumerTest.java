package com.recruit.notification.mq;

import com.recruit.api.delivery.DeliveryStatusChangedEvent;
import com.recruit.notification.entity.Notification;
import com.recruit.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryStatusChangedConsumerTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Test
    void createsApplicantNotificationForInterviewStatus() {
        when(notificationMapper.selectCount(any())).thenReturn(0L);
        DeliveryStatusChangedConsumer consumer = new DeliveryStatusChangedConsumer(notificationMapper);
        DeliveryStatusChangedEvent event = event(2);

        consumer.onMessage(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(captor.capture());
        Notification notification = captor.getValue();
        assertEquals("DELIVERY_INTERVIEW", notification.getType());
        assertEquals("你的投递进入面试环节", notification.getContent());
        assertEquals(100L, notification.getReceiverId());
        assertEquals(0, notification.getStatus());
    }

    @Test
    void ignoresDuplicateEvent() {
        when(notificationMapper.selectCount(any())).thenReturn(1L);
        DeliveryStatusChangedConsumer consumer = new DeliveryStatusChangedConsumer(notificationMapper);

        consumer.onMessage(event(3));

        verify(notificationMapper, never()).insert((Notification) any());
    }

    private DeliveryStatusChangedEvent event(int status) {
        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent();
        event.setEventId("event-1");
        event.setDeliveryId(10L);
        event.setApplicantId(100L);
        event.setRecruiterId(200L);
        event.setNewStatus(status);
        return event;
    }
}
