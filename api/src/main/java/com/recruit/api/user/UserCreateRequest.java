package com.recruit.api.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建用户请求
 */
@Data
public class UserCreateRequest implements Serializable {

    private String phone;
    private String password;
    private String nickname;
    private Integer type;
}
