package com.recruit.auth.dto;

import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {

    private String phone;
    private String password;
    private String nickname;
    private Integer type;
    private String code;
}
