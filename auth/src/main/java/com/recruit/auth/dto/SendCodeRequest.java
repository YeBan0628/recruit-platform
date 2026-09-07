package com.recruit.auth.dto;

import lombok.Data;

/**
 * 发送验证码请求
 */
@Data
public class SendCodeRequest {

    private String phone;
}
