package com.recruit.delivery.service;

import cn.dev33.satoken.stp.StpUtil;
import com.recruit.api.delivery.DeliverySubmittedEvent;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final PositionApi positionApi;
    private final RocketMQTemplate rocketMQTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_KEY_PREFIX = "delivery:lock:";
    private static final String HOT_KEY = "position:hot:score";
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);

    @Transactional
    public Long create(DeliveryCreateRequest request) {
        Long applicantId = currentUserId();
        String lockKey = LOCK_KEY_PREFIX + applicantId + ":" + request.getPositionId();
        Boolean lockAcquired = tryAcquireLock(lockKey);
        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请勿重复提交");
        }
        try {
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
            DeliverySubmittedEvent event = new DeliverySubmittedEvent();
            event.setEventId(java.util.UUID.randomUUID().toString());
            event.setDeliveryId(delivery.getId());
            event.setPositionId(position.getId());
            event.setApplicantId(applicantId);
            event.setRecruiterId(position.getUserId());
            rocketMQTemplate.syncSend("delivery-submitted", MessageBuilder.withPayload(event)
                    .setHeader("KEYS", String.valueOf(delivery.getId())).build());
            increaseHotScore(position.getId(), 5D);
            return delivery.getId();
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能重复投递该职位");
        } finally {
            if (Boolean.TRUE.equals(lockAcquired)) {
                releaseLock(lockKey);
            }
        }
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

    private Boolean tryAcquireLock(String key) {
        try {
            return stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_TTL);
        } catch (Exception e) {
            log.warn("获取投递幂等锁失败，降级数据库防重，key={}", key, e);
            return null;
        }
    }

    private void releaseLock(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("释放投递幂等锁失败，key={}", key, e);
        }
    }

    private void increaseHotScore(Long positionId, double score) {
        try {
            stringRedisTemplate.opsForZSet().incrementScore(HOT_KEY, String.valueOf(positionId), score);
        } catch (Exception e) {
            log.warn("增加职位投递热度失败，positionId={}", positionId, e);
        }
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
