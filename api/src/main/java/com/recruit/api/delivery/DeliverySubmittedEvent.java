package com.recruit.api.delivery;

import lombok.Data;

import java.io.Serializable;

/** 投递成功事件，供 delivery 发布、notification 消费。 */
@Data
public class DeliverySubmittedEvent implements Serializable {
    private String eventId;
    private Long deliveryId;
    private Long positionId;
    private Long applicantId;
    private Long recruiterId;
}
