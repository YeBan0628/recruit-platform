package com.recruit.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或 token 失效"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    ERROR(500, "系统错误");

    private final Integer code;
    private final String message;
}
