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
    private String nickname;
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
}
