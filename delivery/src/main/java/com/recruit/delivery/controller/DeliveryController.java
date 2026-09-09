package com.recruit.delivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.result.Result;
import com.recruit.delivery.dto.DeliveryCreateRequest;
import com.recruit.delivery.dto.DeliveryStatusRequest;
import com.recruit.delivery.entity.Delivery;
import com.recruit.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public Result<Long> create(@Valid @RequestBody DeliveryCreateRequest request) {
        return Result.success(deliveryService.create(request));
    }

    @GetMapping("/me")
    public Result<Page<Delivery>> listMine(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return Result.success(deliveryService.listMine(page, size));
    }

    @GetMapping("/position/{positionId}")
    public Result<Page<Delivery>> listByPosition(@PathVariable Long positionId,
                                                   @RequestParam(value = "page", defaultValue = "1") Long page,
                                                   @RequestParam(value = "size", defaultValue = "10") Long size) {
        return Result.success(deliveryService.listByPosition(positionId, page, size));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody DeliveryStatusRequest request) {
        deliveryService.updateStatus(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        deliveryService.withdraw(id);
        return Result.success();
    }
}
