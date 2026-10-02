package com.recruit.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.recruit.api.user.InternalUserDTO;
import com.recruit.api.user.UserApi;
import com.recruit.api.user.UserCreateRequest;
import com.recruit.auth.dto.LoginRequest;
import com.recruit.auth.dto.RegisterRequest;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.common.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserApi userApi;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private AuthService authService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        userApi = mock(UserApi.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        authService = new AuthService(userApi, redisTemplate);
    }

    @Test
    void sendCodeStoresSixDigitCodeWithTtl() {
        authService.sendCode("13800000000");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(org.mockito.ArgumentMatchers.eq("sms:code:13800000000"),
                codeCaptor.capture(), org.mockito.ArgumentMatchers.eq(5L),
                org.mockito.ArgumentMatchers.eq(TimeUnit.MINUTES));
        assertTrue(codeCaptor.getValue().matches("\\d{6}"));
    }

    @Test
    void registerSucceedsWithValidCode() {
        when(valueOperations.get("sms:code:13800000000")).thenReturn("123456");
        when(userApi.create(any(UserCreateRequest.class))).thenReturn(Result.success(1L));
        RegisterRequest request = registerRequest("123456");

        Long userId = authService.register(request);

        assertEquals(1L, userId);
        ArgumentCaptor<UserCreateRequest> captor = ArgumentCaptor.forClass(UserCreateRequest.class);
        verify(userApi).create(captor.capture());
        assertEquals("13800000000", captor.getValue().getPhone());
        assertEquals("pass123", captor.getValue().getPassword());
        assertEquals("张三", captor.getValue().getNickname());
        assertEquals(1, captor.getValue().getType());
    }

    @Test
    void registerRejectsWrongCode() {
        when(valueOperations.get("sms:code:13800000000")).thenReturn("654321");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.register(registerRequest("123456")));

        assertEquals("验证码错误或已过期", exception.getMessage());
        verifyNoInteractions(userApi);
    }

    @Test
    void registerRejectsExpiredCode() {
        when(valueOperations.get("sms:code:13800000000")).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> authService.register(registerRequest("123456")));
        verifyNoInteractions(userApi);
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        InternalUserDTO user = new InternalUserDTO();
        user.setId(1L);
        user.setPassword(PasswordUtil.encode("pass123"));
        when(userApi.getByPhone("13800000000")).thenReturn(Result.success(user));
        LoginRequest request = new LoginRequest();
        request.setPhone("13800000000");
        request.setPassword("pass123");

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getTokenValue).thenReturn("mock-token");

            String token = authService.login(request);

            assertEquals("mock-token", token);
            stp.verify(() -> StpUtil.login(1L));
        }
    }

    @Test
    void loginRejectsWrongPassword() {
        InternalUserDTO user = new InternalUserDTO();
        user.setId(1L);
        user.setPassword(PasswordUtil.encode("right-password"));
        when(userApi.getByPhone("13800000000")).thenReturn(Result.success(user));
        LoginRequest request = new LoginRequest();
        request.setPhone("13800000000");
        request.setPassword("wrong-password");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(request));

        assertEquals("账号或密码错误", exception.getMessage());
    }

    @Test
    void loginRejectsUnknownPhone() {
        when(userApi.getByPhone("13800000000")).thenReturn(Result.success(null));
        LoginRequest request = new LoginRequest();
        request.setPhone("13800000000");
        request.setPassword("pass123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(request));

        assertEquals("账号或密码错误", exception.getMessage());
    }

    private RegisterRequest registerRequest(String code) {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800000000");
        request.setPassword("pass123");
        request.setNickname("张三");
        request.setType(1);
        request.setCode(code);
        return request;
    }
}
