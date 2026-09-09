package com.recruit.delivery.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.api.position.PositionApi;
import com.recruit.api.position.PositionDTO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.Result;
import com.recruit.common.result.ResultCode;
import com.recruit.delivery.dto.DeliveryCreateRequest;
import com.recruit.delivery.dto.DeliveryStatusRequest;
import com.recruit.delivery.entity.Delivery;
import com.recruit.delivery.mapper.DeliveryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final PositionApi positionApi;

    @Transactional
    public Long create(DeliveryCreateRequest request) {
        Long applicantId = currentUserId();
        PositionDTO position = getPosition(request.getPositionId());
        if (!Integer.valueOf(1).equals(position.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该职位暂未上架");
        }
        if (applicantId.equals(position.getUserId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能投递自己发布的职位");
        }
        Delivery exists = deliveryMapper.selectOne(new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getPositionId, request.getPositionId())
                .eq(Delivery::getApplicantId, applicantId));
        if (exists != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能重复投递该职位");
        }
        Delivery delivery = new Delivery();
        BeanUtils.copyProperties(request, delivery);
        delivery.setApplicantId(applicantId);
        delivery.setStatus(0);
        deliveryMapper.insert(delivery);
        return delivery.getId();
    }

    public Page<Delivery> listMine(Long page, Long size) {
        validatePage(page, size);
        return deliveryMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getApplicantId, currentUserId())
                .orderByDesc(Delivery::getCreateTime));
    }

    public Page<Delivery> listByPosition(Long positionId, Long page, Long size) {
        validatePage(page, size);
        PositionDTO position = getPosition(positionId);
        if (!currentUserId().equals(position.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return deliveryMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getPositionId, positionId)
                .orderByDesc(Delivery::getCreateTime));
    }

    @Transactional
    public void updateStatus(Long id, DeliveryStatusRequest request) {
        validateStatus(request.getStatus());
        Delivery delivery = getDelivery(id);
        PositionDTO position = getPosition(delivery.getPositionId());
        if (!currentUserId().equals(position.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (request.getStatus() == 0 || request.getStatus() == 5) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "招聘方不能设置该投递状态");
        }
        if (isTerminal(delivery.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该投递已结束，不能继续流转");
        }
        delivery.setStatus(request.getStatus());
        deliveryMapper.updateById(delivery);
    }

    @Transactional
    public void withdraw(Long id) {
        Delivery delivery = getDelivery(id);
        if (!currentUserId().equals(delivery.getApplicantId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (isTerminal(delivery.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该投递已结束，不能撤回");
        }
        delivery.setStatus(5);
        deliveryMapper.updateById(delivery);
    }

    private PositionDTO getPosition(Long positionId) {
        Result<PositionDTO> result = positionApi.getById(positionId);
        if (result == null || result.getData() == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "职位不存在");
        }
        return result.getData();
    }

    private Delivery getDelivery(Long id) {
        Delivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return delivery;
    }

    private Long currentUserId() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return StpUtil.getLoginIdAsLong();
    }

    private void validatePage(Long page, Long size) {
        if (page == null || page < 1 || size == null || size < 1 || size > 100) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "分页参数不合法");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || status < 0 || status > 5) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "投递状态不合法");
        }
    }

    private boolean isTerminal(Integer status) {
        return status != null && (status == 3 || status == 4 || status == 5);
    }
}
