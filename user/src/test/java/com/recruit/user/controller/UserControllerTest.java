package com.recruit.user.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.recruit.api.user.InternalUserDTO;
import com.recruit.api.user.UserCreateRequest;
import com.recruit.common.result.Result;
import com.recruit.common.result.ResultCode;
import com.recruit.common.util.PasswordUtil;
import com.recruit.user.entity.User;
import com.recruit.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserMapper userMapper;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        userController = new UserController(userMapper);
    }

    @Test
    void createEncodesPasswordAndAppliesDefaults() {
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(100L);
            return 1;
        }).when(userMapper).insert(any(User.class));
        UserCreateRequest request = new UserCreateRequest();
        request.setPhone("13800000000");
        request.setPassword("pass123");
        request.setNickname("张三");

        Result<Long> result = userController.create(request);

        assertEquals(100L, result.getData());
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();
        assertEquals("13800000000", saved.getPhone());
        assertEquals("张三", saved.getNickname());
        assertEquals(1, saved.getType());
        assertEquals(1, saved.getStatus());
        assertTrue(PasswordUtil.matches("pass123", saved.getPassword()));
    }

    @Test
    void createKeepsExplicitRecruiterType() {
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(101L);
            return 1;
        }).when(userMapper).insert(any(User.class));
        UserCreateRequest request = new UserCreateRequest();
        request.setPhone("13900000000");
        request.setPassword("pass123");
        request.setNickname("李四");
        request.setType(2);

        userController.create(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals(2, captor.getValue().getType());
    }

    @Test
    void getByPhoneReturnsNullDataWhenUserMissing() {
        when(userMapper.selectOne(any())).thenReturn(null);

        Result<InternalUserDTO> result = userController.getByPhone("13800000000");

        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByPhoneCopiesUserFieldsToDto() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800000000");
        user.setPassword(PasswordUtil.encode("pass123"));
        user.setNickname("张三");
        user.setType(1);
        user.setStatus(1);
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user);

        Result<InternalUserDTO> result = userController.getByPhone("13800000000");

        InternalUserDTO dto = result.getData();
        assertEquals(1L, dto.getId());
        assertEquals("13800000000", dto.getPhone());
        assertEquals("张三", dto.getNickname());
        assertEquals(1, dto.getType());
        assertEquals(1, dto.getStatus());
        assertTrue(PasswordUtil.matches("pass123", dto.getPassword()));
    }
}
