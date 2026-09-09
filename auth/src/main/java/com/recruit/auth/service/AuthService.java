package com.recruit.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.recruit.api.user.UserApi;
import com.recruit.api.user.UserCreateRequest;
import com.recruit.api.user.InternalUserDTO;
import com.recruit.auth.dto.LoginRequest;
import com.recruit.auth.dto.RegisterRequest;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.common.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CODE_PREFIX = "sms:code:";

    private final UserApi userApi;
    private final StringRedisTemplate redisTemplate;

    /**
     * 发送验证码（存 Redis，5 分钟有效）
     */
    public void sendCode(String phone) {
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        redisTemplate.opsForValue().set(CODE_PREFIX + phone, code, 5, TimeUnit.MINUTES);
        // TODO 实际项目接入短信服务，这里仅打印模拟
        System.out.println("[模拟短信] 验证码: " + code);
    }

    /**
     * 注册：校验验证码 → Feign 调用 user 服务创建用户
     */
    public Long register(RegisterRequest request) {
        String cached = redisTemplate.opsForValue().get(CODE_PREFIX + request.getPhone());
        if (cached == null || !cached.equals(request.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setPhone(request.getPhone());
        createRequest.setPassword(request.getPassword());
        createRequest.setNickname(request.getNickname());
        createRequest.setType(request.getType());
        Result<Long> result = userApi.create(createRequest);
        return result.getData();
    }

    /**
     * 登录：Feign 查用户 → 校验密码 → Sa-Token 发 token
     */
    public String login(LoginRequest request) {
        Result<InternalUserDTO> result = userApi.getByPhone(request.getPhone());
        InternalUserDTO user = result.getData();
        if (user == null || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }
}
