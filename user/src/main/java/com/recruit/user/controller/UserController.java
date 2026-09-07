package com.recruit.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.api.user.UserApi;
import com.recruit.api.user.UserCreateRequest;
import com.recruit.api.user.UserDTO;
import com.recruit.common.result.Result;
import com.recruit.common.util.PasswordUtil;
import com.recruit.user.entity.User;
import com.recruit.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口（实现 UserApi，供 auth 服务 Feign 调用）
 */
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserMapper userMapper;

    @Override
    @PostMapping("/user")
    public Result<Long> create(@RequestBody UserCreateRequest request) {
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setType(request.getType() == null ? 1 : request.getType());
        user.setStatus(1);
        userMapper.insert(user);
        return Result.success(user.getId());
    }

    @Override
    @GetMapping("/user/phone/{phone}")
    public Result<UserDTO> getByPhone(@PathVariable("phone") String phone) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            return Result.success(null);
        }
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return Result.success(dto);
    }
}
