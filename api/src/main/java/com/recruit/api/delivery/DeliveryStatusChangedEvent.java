package com.recruit.api.delivery;

import lombok.Data;

import java.io.Serializable;

/** 投递状态变更事件，供 delivery 发布、notification 消费。 */
@Data
public class DeliveryStatusChangedEvent implements Serializable {
    private String eventId;
    private Long deliveryId;
    private Long positionId;
    private Long applicantId;
    private Long recruiterId;
    private Long operatorId;
    private Integer oldStatus;
    private Integer newStatus;
}
