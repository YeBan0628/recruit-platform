package com.recruit.api.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 仅供认证服务内部调用的用户 DTO，包含密码摘要。 */
@Data
public class InternalUserDTO implements Serializable {

    private Long id;
    private String phone;
    private String password;
    private String nickname;
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
}
