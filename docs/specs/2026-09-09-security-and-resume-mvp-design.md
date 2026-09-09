# 职位鉴权加固与简历 MVP 设计

## 1. 目标与范围

本阶段解决两个直接影响后续开发的问题：

1. 职位服务不再信任请求体或查询参数中的 `userId`，所有需要登录的操作从 Sa-Token 登录态获取当前用户。
2. 用户密码只在认证服务与用户服务之间的内部 Feign 调用中使用，禁止通过网关对外返回。
3. 新增 `resume-service` 的最小可用版本，支持一个用户维护一份主简历及教育、工作、项目、技能信息。

本阶段不包含 RocketMQ、Spring AI、文件上传、聊天、职位匹配和复杂投递状态机。

## 2. 方案选择

### 采用：业务服务本地解析共享 Sa-Token

认证服务登录后使用 Sa-Token 生成 token，并通过 Redis 保存会话；`position-service` 与未来的 `resume-service` 使用同一 token-name、Redis 和 Sa-Token Redis 插件解析 token。Gateway 只负责路由和透传 token，不负责替业务服务决定资源归属。

这样做的优点是：权限判断靠近业务、服务可独立验证、没有每次请求的鉴权远程调用，也能支持服务被内部直连时仍然校验登录态。

### 未采用的方案

- 只在 Gateway 注入用户 ID：实现较快，但绕过 Gateway 或内部调用时无法保证身份可信。
- 每次请求调用统一鉴权服务：边界清晰，但增加网络依赖和故障点，不适合当前本地演示阶段。

## 3. 安全边界设计

### 3.1 Token 与 Redis

- auth、position、resume 使用相同的 `sa-token.token-name=token`。
- auth 和需要校验登录态的业务服务引入 `sa-token-redis-jackson`，共用 Redis。
- Gateway 原样透传 `token` 请求头。
- 未登录或 token 失效统一返回 401；资源存在但不属于当前用户统一返回 403。

### 3.2 用户 DTO 隔离

- `InternalUserDTO`：仅供 auth Feign 使用，包含 BCrypt 密码。
- `UserResponse`：对外返回，不包含密码。
- 用户手机号查询调整为内部路径 `/internal/user/phone/{phone}`，不配置 Gateway 路由。
- `UserApi` 只保留认证服务需要的内部接口；后续用户资料接口单独设计，不复用认证 DTO。

### 3.3 职位接口

以下接口要求登录，并从 `StpUtil.getLoginIdAsLong()` 获取用户 ID：

- `POST /position`
- `PUT /position/{id}`
- `PUT /position/{id}/status`
- `DELETE /position/{id}`
- `GET /position/me`

职位公开详情和上架职位分页搜索继续允许匿名访问。创建/修改请求体中删除 `userId`；上下架、删除不再接受 `userId` 查询参数。

## 4. Resume Service 设计

### 4.1 模块与依赖

新增 `resume` Maven 模块，端口使用 8084，注册名 `resume-service`。依赖 Spring Web、Nacos Discovery、MyBatis-Plus、MySQL、Sa-Token Redis、`recruit-common` 和 `recruit-api`。

Gateway 增加 `/resume/** -> lb://resume-service` 路由。

### 4.2 数据模型

- `resume`：`id`、`user_id`（唯一）、`name`、`phone`、`email`、`summary`、`status`、创建/更新时间。
- `resume_education`：`id`、`resume_id`、学校、专业、学历、开始/结束日期、描述。
- `resume_work`：`id`、`resume_id`、公司、职位、开始/结束日期、描述。
- `resume_project`：`id`、`resume_id`、项目名称、角色、开始/结束日期、描述。
- `resume_skill`：`id`、`resume_id`、技能名称、熟练度。

所有子表通过 `resume_id` 关联主表；查询和修改前先校验主表归属当前用户，避免仅凭子表 ID 越权。

### 4.3 MVP 接口

- `POST /resume`：创建当前用户的简历；已有简历返回业务错误。
- `GET /resume/me`：查询当前用户简历及四类子项。
- `PUT /resume`：修改主简历字段。
- `DELETE /resume`：删除当前用户简历及其子项。
- `POST/PUT/DELETE /resume/education/{id}`
- `POST/PUT/DELETE /resume/work/{id}`
- `POST/PUT/DELETE /resume/project/{id}`
- `POST/PUT/DELETE /resume/skill/{id}`

子项新增时只接受业务字段，`resumeId` 从当前用户的主简历解析，不接受客户端指定其他简历 ID。

## 5. 事务与错误处理

- 主简历删除使用事务，先删子表再删主表。
- 主简历创建、子项写入和归属校验失败时返回统一 `Result`。
- 统一处理 Sa-Token 未登录异常为 401；业务资源不存在为 404；归属不匹配为 403。
- 本阶段不增加跨服务分布式事务；简历数据全部落在 resume 服务自己的表中。

## 6. 验证标准

1. Maven 根工程编译通过。
2. 登录得到的 token 可经 Gateway 访问职位创建、修改、上下架、删除和 `/position/me`。
3. 不带 token 或伪造 `userId` 不能越权；非发布者返回 403。
4. `/internal/user/phone/{phone}` 不经过 Gateway，auth 登录仍能正常调用并校验密码。
5. 简历主表和四类子表可创建、查询、更新、删除；不同用户 token 不能读取或修改他人简历。
6. 测试数据可清理，数据库脚本可重复执行而不破坏已有表。

## 7. 实施顺序

1. 补充 Sa-Token Redis 依赖与共享配置，修复用户内部 DTO 和内部路径。
2. 修改职位请求、控制器和异常映射，完成登录态鉴权。
3. 编译并做职位接口回归验证。
4. 新增 resume 模块、SQL、API DTO、实体、Mapper、Service、Controller。
5. 增加 Gateway 路由，编译并进行简历接口验证。

