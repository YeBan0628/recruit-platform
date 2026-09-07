package com.recruit.api.user;

import com.recruit.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务远程接口（auth 服务通过 Feign 调用）
 */
@FeignClient(name = "user-service", contextId = "userApi")
public interface UserApi {

    @PostMapping("/user")
    Result<Long> create(@RequestBody UserCreateRequest request);

    @GetMapping("/user/phone/{phone}")
    Result<UserDTO> getByPhone(@PathVariable("phone") String phone);
}
