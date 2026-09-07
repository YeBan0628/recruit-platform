package com.recruit.auth.controller;

import com.recruit.auth.dto.LoginRequest;
import com.recruit.auth.dto.RegisterRequest;
import com.recruit.auth.dto.SendCodeRequest;
import com.recruit.auth.service.AuthService;
import com.recruit.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/code")
    public Result<Void> sendCode(@RequestBody SendCodeRequest request) {
        authService.sendCode(request.getPhone());
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Long> register(@RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
}
