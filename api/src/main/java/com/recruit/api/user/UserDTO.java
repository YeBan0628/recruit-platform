package com.recruit.api.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 DTO
 */
@Data
public class UserDTO implements Serializable {

    private Long id;
    private String phone;
    /** 密码（加密后，仅内部 Feign 调用返回，不对前端暴露） */
    private String password;
    private String nickname;
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
}
