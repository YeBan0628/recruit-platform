package com.recruit.api.position;

import com.recruit.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 职位服务远程接口（delivery 服务通过 Feign 调用，校验职位是否存在）
 */
@FeignClient(name = "position-service", contextId = "positionApi")
public interface PositionApi {

    @GetMapping("/position/{id}")
    Result<PositionDTO> getById(@PathVariable("id") Long id);
}
