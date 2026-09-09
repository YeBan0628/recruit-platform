package com.recruit.delivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryStatusRequest {
    @NotNull(message = "投递状态不能为空")
    private Integer status;
}
