package com.recruit.delivery.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliveryCreateRequest {
    @NotNull(message = "职位不能为空")
    private Long positionId;
    @Size(max = 1000, message = "投递留言不能超过1000个字符")
    private String message;
}
