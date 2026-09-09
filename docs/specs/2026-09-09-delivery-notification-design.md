# 投递状态通知与通知查询设计

## 1. 目标

完善现有投递业务的消息闭环：投递状态发生变化后，通过 RocketMQ 异步生成站内通知；用户可以查询自己的通知、未读数量并标记已读。

## 2. 范围

包含：

- 招聘方更新投递状态时通知求职者
- 求职者撤回投递时通知招聘方
- RocketMQ 状态变更事件及消费幂等
- 我的通知分页查询
- 未读通知数量查询
- 单条通知标记已读

不包含：

- WebSocket 实时推送
- 邮件、短信或移动端推送
- 通知删除、批量已读
- 新增数据库表或修改现有字段

## 3. 事件设计

新增 `DeliveryStatusChangedEvent`，字段包括 `eventId`、`deliveryId`、`positionId`、`applicantId`、`recruiterId`、`operatorId`、`oldStatus`、`newStatus`。

- Topic：`delivery-status-changed`
- 招聘方修改状态时，接收人是 `applicantId`
- 求职者撤回时，接收人是 `recruiterId`
- `eventId` 使用 UUID，通知表 `event_id` 查询用于幂等
- 只有数据库状态更新成功后才发送事件

## 4. 通知服务

通知消费者根据状态生成类型和内容：

| 新状态 | 类型 | 内容 |
| --- | --- | --- |
| 1 | DELIVERY_VIEWED | 招聘方已查看你的投递 |
| 2 | DELIVERY_INTERVIEW | 你的投递进入面试环节 |
| 3 | DELIVERY_HIRED | 恭喜，你已被录用 |
| 4 | DELIVERY_REJECTED | 很遗憾，你的投递未通过 |
| 5 | DELIVERY_WITHDRAWN | 求职者撤回了一份职位投递 |

通知状态沿用现有字段：`0` 未读、`1` 已读。

## 5. 查询接口

- `GET /notification/me?page=1&size=10`：查询当前登录用户通知，按创建时间倒序
- `GET /notification/unread-count`：查询当前用户未读数量
- `PUT /notification/{id}/read`：将本人通知标记为已读

所有接口从 Sa-Token 登录态获取用户 ID，禁止使用请求参数指定接收人；跨用户访问统一返回无权限。

## 6. 错误与一致性

- Redis、RocketMQ 异常不直接暴露基础设施信息，沿用全局异常处理。
- 消费者先按 `eventId` 查询，重复消息直接忽略。
- 状态更新事务提交后发送消息；消息发送失败记录日志并抛出异常，由 RocketMQ 重试机制处理。
- 通知查询不影响投递主流程。

## 7. 验收

- 五种状态变化均能生成对应通知，且接收人正确。
- 重复投递同一事件不会产生重复通知。
- 用户只能查询和修改自己的通知。
- 分页、未读数、标记已读接口经网关可访问。
- Maven 编译通过，完成至少一次手动接口验证。
