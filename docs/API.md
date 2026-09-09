# 智能招聘平台接口文档

> 更新时间：2026-09-09  
> 适用范围：当前仓库已经实现的后端接口  
> 网关基础地址：http://localhost:8080

## 1. 服务与端口

前端和接口测试工具应优先通过 Gateway 调用。

| 服务 | 服务名 | 本地端口 | 网关路径 |
| --- | --- | ---: | --- |
| 网关 | gateway-service | 8080 | - |
| 认证 | auth-service | 8081 | /auth/** |
| 用户 | user-service | 8082 | /user/** |
| 职位 | position-service | 8083 | /position/** |
| 简历 | resume-service | 8084 | /resume/** |
| 投递 | delivery-service | 8085 | /delivery/** |
| 通知 | notification-service | 8086 | /notification/** |

访问 http://localhost:8080/ 返回 404 属于正常现象，因为项目没有配置根路径接口。请访问具体业务路径，例如 http://localhost:8080/position/page。

## 2. 调用约定

### 2.1 请求格式

- JSON 请求头：Content-Type: application/json; charset=UTF-8
- 日期格式：yyyy-MM-dd
- 页面从第 1 页开始
- 分页参数：page >= 1，1 <= size <= 100

### 2.2 登录凭证

登录成功后，响应的 data 是 Sa-Token。调用需要登录的接口时添加请求头：

~~~http
token: 3ad9c670-xxxx-xxxx-xxxx-xxxxxxxxxxxx
~~~

本文用 YOUR_TOKEN 表示登录返回的 Token，不包含真实凭证。

### 2.3 统一响应

~~~json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
~~~

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 业务状态码 |
| message | string | 结果说明 |
| data | 任意类型或 null | 响应数据 |

通用业务状态码：

| code | message | 含义 |
| ---: | --- | --- |
| 200 | 成功 | 调用成功 |
| 400 | 参数错误 | 参数校验或业务规则不满足 |
| 401 | 未登录或 token 失效 | 未携带有效 Token |
| 403 | 无权限 | 当前用户不是资源所有者 |
| 404 | 资源不存在 | 职位、简历、投递或通知不存在 |
| 500 | 系统错误 | 未处理异常或外部服务调用失败 |

注意：当前全局异常处理器把业务码放在响应体中，HTTP 状态通常仍为 200。前端应优先判断响应体中的 code，不能只判断 HTTP 状态。

### 2.4 分页响应

职位、投递和通知的分页接口使用 MyBatis-Plus Page。常用字段如下：

~~~json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [],
    "total": 0,
    "size": 10,
    "current": 1,
    "pages": 0
  }
}
~~~

不同 MyBatis-Plus 序列化配置下可能出现 orders、searchCount 等额外框架字段，业务侧主要使用 records、total、size、current 和 pages。

## 3. 快速联调

下面的 PowerShell 示例先登录，再携带 Token 查询我的简历：

~~~powershell
$baseUrl = "http://localhost:8080"

$loginBody = @{
    phone = "13800000001"
    password = "Test123456"
} | ConvertTo-Json

$loginResult = Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/login" -ContentType "application/json; charset=UTF-8" -Body $loginBody
$headers = @{ token = $loginResult.data }
Invoke-RestMethod -Method Get -Uri "$baseUrl/resume/me" -Headers $headers
~~~

PowerShell 中不要使用 $PID 保存业务数据，它是系统只读变量。包含多个查询参数时直接使用 &，不要写成 %26。

## 4. 接口总览

### 4.1 认证

| 方法 | 路径 | 登录 | 功能 |
| --- | --- | --- | --- |
| POST | /auth/code | 否 | 发送模拟验证码 |
| POST | /auth/register | 否 | 注册用户 |
| POST | /auth/login | 否 | 登录并获取 Token |

### 4.2 职位

| 方法 | 路径 | 登录 | 功能 |
| --- | --- | --- | --- |
| POST | /position | 是 | 发布职位 |
| PUT | /position/{id} | 是 | 修改本人发布的职位 |
| PUT | /position/{id}/status | 是 | 上架或下架职位 |
| DELETE | /position/{id} | 是 | 删除本人发布的职位 |
| GET | /position/{id} | 否 | 查询职位详情 |
| GET | /position/hot | 否 | 查询职位热榜 |
| GET | /position/page | 否 | 分页搜索上架职位 |
| GET | /position/me | 是 | 查询本人发布的职位 |

### 4.3 简历

| 方法 | 路径 | 登录 | 功能 |
| --- | --- | --- | --- |
| POST | /resume | 是 | 创建简历 |
| GET | /resume/me | 是 | 查询我的完整简历 |
| PUT | /resume | 是 | 修改简历 |
| DELETE | /resume | 是 | 删除简历及其子项 |
| POST | /resume/education | 是 | 新增教育经历 |
| PUT | /resume/education/{id} | 是 | 修改教育经历 |
| DELETE | /resume/education/{id} | 是 | 删除教育经历 |
| POST | /resume/work | 是 | 新增工作经历 |
| PUT | /resume/work/{id} | 是 | 修改工作经历 |
| DELETE | /resume/work/{id} | 是 | 删除工作经历 |
| POST | /resume/project | 是 | 新增项目经历 |
| PUT | /resume/project/{id} | 是 | 修改项目经历 |
| DELETE | /resume/project/{id} | 是 | 删除项目经历 |
| POST | /resume/skill | 是 | 新增技能 |
| PUT | /resume/skill/{id} | 是 | 修改技能 |
| DELETE | /resume/skill/{id} | 是 | 删除技能 |
| POST | /resume/match/{positionId} | 是 | AI 简历职位匹配 |

### 4.4 投递

| 方法 | 路径 | 登录 | 功能 |
| --- | --- | --- | --- |
| POST | /delivery | 是 | 投递职位 |
| GET | /delivery/me | 是 | 查询我的投递 |
| GET | /delivery/position/{positionId} | 是 | 招聘方查询职位收到的投递 |
| PUT | /delivery/{id}/status | 是 | 招聘方更新投递状态 |
| PUT | /delivery/{id}/withdraw | 是 | 求职者撤回投递 |

### 4.5 通知

| 方法 | 路径 | 登录 | 功能 |
| --- | --- | --- | --- |
| GET | /notification/me | 是 | 查询我的通知 |
| GET | /notification/unread-count | 是 | 查询未读数量 |
| PUT | /notification/{id}/read | 是 | 标记通知已读 |

## 5. 认证接口

### 5.1 发送验证码

POST /auth/code

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phone | string | 是 | 接收验证码的手机号 |

~~~json
{
  "phone": "13800000001"
}
~~~

成功响应：

~~~json
{
  "code": 200,
  "message": "成功",
  "data": null
}
~~~

当前未接入短信平台。6 位验证码会打印在 auth-service 控制台，并写入 Redis，5 分钟有效。

### 5.2 注册

POST /auth/register

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phone | string | 是 | 手机号 |
| password | string | 是 | 登录密码，入库前使用 BCrypt 加密 |
| nickname | string | 否 | 昵称 |
| type | integer | 否 | 1 求职者，2 招聘方；不传默认 1 |
| code | string | 是 | 发送验证码接口生成的验证码 |

~~~json
{
  "phone": "13800000001",
  "password": "Test123456",
  "nickname": "张同学",
  "type": 1,
  "code": "123456"
}
~~~

成功时 data 为新用户 ID：

~~~json
{
  "code": 200,
  "message": "成功",
  "data": 1001
}
~~~

验证码错误或过期时返回：

~~~json
{
  "code": 500,
  "message": "验证码错误或已过期",
  "data": null
}
~~~

### 5.3 登录

POST /auth/login

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phone | string | 是 | 注册手机号 |
| password | string | 是 | 登录密码 |

~~~json
{
  "phone": "13800000001",
  "password": "Test123456"
}
~~~

成功时 data 为后续请求使用的 Token：

~~~json
{
  "code": 200,
  "message": "成功",
  "data": "3ad9c670-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
~~~

账号不存在或密码错误时返回 code 500，message 为“账号或密码错误”。

## 6. 职位接口

### 6.1 职位字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 职位 ID |
| userId | long | 发布者用户 ID |
| title | string | 职位名称 |
| city | string | 城市 |
| salaryMin | integer | 最低月薪，单位 K |
| salaryMax | integer | 最高月薪，单位 K |
| experience | string | 经验要求 |
| education | string | 学历要求 |
| description | string | 职位描述 |
| status | integer | 0 下架，1 上架 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间；主要出现在分页实体中 |

### 6.2 发布职位

POST /position，需要登录。

| 字段 | 类型 | 必填 | 限制 |
| --- | --- | --- | --- |
| title | string | 是 | 非空，最多 50 字符 |
| city | string | 否 | 最多 30 字符 |
| salaryMin | integer | 否 | 大于等于 0 |
| salaryMax | integer | 否 | 大于等于 0，且不低于 salaryMin |
| experience | string | 否 | 最多 20 字符 |
| education | string | 否 | 最多 20 字符 |
| description | string | 否 | 职位描述 |

~~~http
POST /position
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "title": "Java 后端开发工程师",
  "city": "杭州",
  "salaryMin": 15,
  "salaryMax": 25,
  "experience": "应届生",
  "education": "本科",
  "description": "负责招聘平台微服务模块开发。"
}
~~~

成功时 data 为职位 ID，职位默认上架：

~~~json
{
  "code": 200,
  "message": "成功",
  "data": 2001
}
~~~

### 6.3 修改职位

PUT /position/{id}，需要登录且只能修改本人发布的职位。

请求字段与发布职位相同；更新 DTO 中字段均允许不传。建议前端提交完整职位内容，避免数据库非空字段被空值覆盖。

~~~http
PUT /position/2001
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "title": "Java 后端开发工程师",
  "city": "上海",
  "salaryMin": 18,
  "salaryMax": 28,
  "experience": "应届生",
  "education": "本科",
  "description": "负责 Spring Cloud 微服务开发。"
}
~~~

成功响应的 data 为 null。职位不存在返回 404，不是发布者返回 403。

### 6.4 上架或下架职位

PUT /position/{id}/status?status={status}，需要登录且只能操作本人发布的职位。

| 参数 | 位置 | 类型 | 说明 |
| --- | --- | --- | --- |
| id | path | long | 职位 ID |
| status | query | integer | 0 下架，1 上架 |

~~~http
PUT /position/2001/status?status=0
token: YOUR_TOKEN
~~~

成功响应的 data 为 null。其他状态返回 code 400。

### 6.5 删除职位

DELETE /position/{id}，需要登录且只能删除本人发布的职位。

~~~http
DELETE /position/2001
token: YOUR_TOKEN
~~~

成功响应的 data 为 null，同时清理该职位详情缓存和热度数据。

### 6.6 查询职位详情

GET /position/{id}，无需登录。

~~~http
GET /position/2001
~~~

成功响应：

~~~json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 2001,
    "userId": 1002,
    "title": "Java 后端开发工程师",
    "city": "杭州",
    "salaryMin": 15,
    "salaryMax": 25,
    "experience": "应届生",
    "education": "本科",
    "description": "负责招聘平台微服务模块开发。",
    "status": 1,
    "createTime": "2026-09-09T10:30:00"
  }
}
~~~

当前实现中，职位不存在时返回 code 200、data null。每次查询详情会为职位增加 1 点 Redis 热度。

### 6.7 查询职位热榜

GET /position/hot?limit=10，无需登录。

| 参数 | 类型 | 必填 | 默认值 | 限制 |
| --- | --- | --- | ---: | --- |
| limit | integer | 否 | 10 | 1 到 100 |

data 为职位数组，仅返回仍处于上架状态的职位。Redis 不可用或暂无热度数据时返回空数组。

### 6.8 分页搜索职位

GET /position/page?page=1&size=10&city=杭州&keyword=Java，无需登录。

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| page | long | 否 | 1 | 页码 |
| size | long | 否 | 10 | 每页数量，最大 100 |
| city | string | 否 | - | 城市模糊匹配 |
| keyword | string | 否 | - | 职位名称模糊匹配 |

仅查询上架职位，按创建时间倒序返回。

### 6.9 查询我发布的职位

GET /position/me?page=1&size=10，需要登录。

返回当前登录用户发布的全部职位，包括上架和下架状态，并按创建时间倒序排列。

## 7. 简历接口

所有简历接口都需要登录。每个用户只能拥有一份简历，教育、工作、项目和技能子项都只能操作本人简历的数据。

### 7.1 简历主体字段

| 字段 | 类型 | 必填 | 限制 |
| --- | --- | --- | --- |
| name | string | 是 | 非空，最多 50 字符 |
| phone | string | 否 | 最多 20 字符 |
| email | string | 否 | 合法邮箱，最多 100 字符 |
| summary | string | 否 | 最多 1000 字符 |
| status | integer | 否 | 0 隐藏，1 启用；创建时默认 1 |

### 7.2 创建简历

POST /resume

~~~http
POST /resume
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "name": "张同学",
  "phone": "13800000001",
  "email": "zhang@example.com",
  "summary": "熟悉 Java、Spring Boot 和 Spring Cloud。",
  "status": 1
}
~~~

成功时 data 为简历 ID。当前用户已经有简历时返回 code 400。

### 7.3 查询我的完整简历

GET /resume/me

~~~http
GET /resume/me
token: YOUR_TOKEN
~~~

成功响应：

~~~json
{
  "code": 200,
  "message": "成功",
  "data": {
    "resume": {
      "id": 3001,
      "userId": 1001,
      "name": "张同学",
      "phone": "13800000001",
      "email": "zhang@example.com",
      "summary": "熟悉 Java、Spring Boot 和 Spring Cloud。",
      "status": 1,
      "createTime": "2026-09-09T11:00:00",
      "updateTime": "2026-09-09T11:00:00"
    },
    "educations": [],
    "works": [],
    "projects": [],
    "skills": []
  }
}
~~~

没有简历时返回 code 404，message 为“请先创建简历”。

### 7.4 修改简历

PUT /resume

请求字段与创建简历相同。name 仍为必填字段，因此这是完整表单更新，不是 PATCH。

~~~json
{
  "name": "张同学",
  "phone": "13800000001",
  "email": "zhang@example.com",
  "summary": "熟悉微服务、MySQL、Redis 和 RocketMQ。",
  "status": 1
}
~~~

成功响应的 data 为 null；status 不是 0 或 1 时返回 code 400。

### 7.5 删除简历

DELETE /resume

删除当前用户的简历主体，并同步删除其教育、工作、项目和技能记录。成功响应的 data 为 null。

### 7.6 教育经历

请求字段：

| 字段 | 类型 | 必填 | 限制 |
| --- | --- | --- | --- |
| school | string | 否 | 最多 100 字符 |
| major | string | 否 | 最多 100 字符 |
| degree | string | 否 | 最多 30 字符 |
| startDate | date | 否 | yyyy-MM-dd |
| endDate | date | 否 | yyyy-MM-dd，不早于 startDate |
| description | string | 否 | 最多 500 字符 |

新增：

~~~http
POST /resume/education
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "school": "示例大学",
  "major": "软件工程",
  "degree": "本科",
  "startDate": "2022-09-01",
  "endDate": "2026-06-30",
  "description": "主修 Java、数据库和计算机网络。"
}
~~~

成功时 data 为教育经历 ID。

修改：PUT /resume/education/{id}，请求体同上，成功时 data 为 null。  
删除：DELETE /resume/education/{id}，成功时 data 为 null。

记录不存在返回 404；记录不属于当前用户返回 403。

### 7.7 工作经历

请求字段：

| 字段 | 类型 | 必填 | 限制 |
| --- | --- | --- | --- |
| company | string | 否 | 最多 100 字符 |
| position | string | 否 | 最多 100 字符 |
| startDate | date | 否 | yyyy-MM-dd |
| endDate | date | 否 | yyyy-MM-dd，不早于 startDate |
| description | string | 否 | 最多 1000 字符 |

新增：

~~~http
POST /resume/work
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "company": "示例科技有限公司",
  "position": "Java 开发实习生",
  "startDate": "2025-07-01",
  "endDate": "2025-09-01",
  "description": "参与微服务接口开发和数据库设计。"
}
~~~

成功时 data 为工作经历 ID。

修改：PUT /resume/work/{id}，请求体同上，成功时 data 为 null。  
删除：DELETE /resume/work/{id}，成功时 data 为 null。

### 7.8 项目经历

请求字段：

| 字段 | 类型 | 必填 | 限制 |
| --- | --- | --- | --- |
| projectName | string | 否 | 最多 100 字符 |
| role | string | 否 | 最多 100 字符 |
| startDate | date | 否 | yyyy-MM-dd |
| endDate | date | 否 | yyyy-MM-dd，不早于 startDate |
| description | string | 否 | 最多 1000 字符 |

新增：

~~~http
POST /resume/project
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "projectName": "智能招聘平台",
  "role": "后端开发",
  "startDate": "2026-03-01",
  "endDate": "2026-09-01",
  "description": "基于 Spring Cloud Alibaba 实现职位、简历和投递流程。"
}
~~~

成功时 data 为项目经历 ID。

修改：PUT /resume/project/{id}，请求体同上，成功时 data 为 null。  
删除：DELETE /resume/project/{id}，成功时 data 为 null。

### 7.9 技能

请求字段：

| 字段 | 类型 | 必填 | 限制 |
| --- | --- | --- | --- |
| skillName | string | 是 | 非空，最多 50 字符 |
| proficiency | string | 否 | 最多 30 字符 |

新增：

~~~http
POST /resume/skill
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "skillName": "Spring Cloud Alibaba",
  "proficiency": "熟悉"
}
~~~

成功时 data 为技能 ID。

修改：PUT /resume/skill/{id}，请求体同上，成功时 data 为 null。  
删除：DELETE /resume/skill/{id}，成功时 data 为 null。

### 7.10 AI 简历职位匹配

POST /resume/match/{positionId}，需要登录。

| 参数 | 位置 | 类型 | 说明 |
| --- | --- | --- | --- |
| positionId | path | long | 需要匹配的职位 ID |

~~~http
POST /resume/match/2001
token: YOUR_TOKEN
~~~

成功响应：

~~~json
{
  "code": 200,
  "message": "成功",
  "data": {
    "score": 88,
    "summary": "技术栈与职位要求匹配良好。",
    "strengths": [
      "具备 Java 和 Spring Cloud 项目经验",
      "熟悉 MySQL、Redis 和 RocketMQ"
    ],
    "gaps": [
      "正式工作经验较少"
    ]
  }
}
~~~

调用条件：

- 当前用户已经创建简历
- 指定职位存在
- resume-service 已启用 AI，并正确设置 DeepSeek API Key

AI 未启用或未配置密钥时：

~~~json
{
  "code": 500,
  "message": "AI 匹配暂未配置",
  "data": null
}
~~~

模型请求失败、返回空内容或 JSON 格式异常时，接口返回相应的 code 500 错误。

## 8. 投递接口

所有投递接口都需要登录。投递和状态变化会同步发送 RocketMQ 消息，因此本地联调这些写接口前应启动 RocketMQ。

### 8.1 投递字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 投递 ID |
| positionId | long | 职位 ID |
| applicantId | long | 求职者用户 ID |
| status | integer | 投递状态 |
| message | string | 投递留言 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 8.2 投递职位

POST /delivery

| 字段 | 类型 | 必填 | 限制 |
| --- | --- | --- | --- |
| positionId | long | 是 | 目标职位 ID |
| message | string | 否 | 最多 1000 字符 |

~~~http
POST /delivery
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "positionId": 2001,
  "message": "您好，我对该职位很感兴趣。"
}
~~~

成功时 data 为投递 ID，初始状态为 0，并为职位增加 5 点热度。

投递限制：

- 职位必须存在且处于上架状态
- 不能投递自己发布的职位
- 同一用户不能重复投递同一职位
- Redis 锁用于短时防重；Redis 不可用时降级为数据库唯一约束防重

### 8.3 查询我的投递

GET /delivery/me?page=1&size=10

返回当前登录用户发起的投递，按创建时间倒序排列。

### 8.4 招聘方查询职位收到的投递

GET /delivery/position/{positionId}?page=1&size=10

只有职位发布者可以查询，其他用户返回 code 403。返回指定职位的投递记录，按创建时间倒序排列。

### 8.5 招聘方更新投递状态

PUT /delivery/{id}/status

~~~http
PUT /delivery/4001/status
token: YOUR_TOKEN
Content-Type: application/json
~~~

~~~json
{
  "status": 2
}
~~~

只有对应职位的发布者可以操作。招聘方只能设置 1 已查看、2 面试中、3 已录用或 4 已拒绝，不能设置 0 或 5。

### 8.6 求职者撤回投递

PUT /delivery/{id}/withdraw

只有投递人本人可以撤回。状态为已录用、已拒绝或已撤回时不能再次撤回。成功后状态变为 5。

### 8.7 投递状态流转

| 值 | 状态 | 是否终态 |
| ---: | --- | --- |
| 0 | 投递中 | 否 |
| 1 | 已查看 | 否 |
| 2 | 面试中 | 否 |
| 3 | 已录用 | 是 |
| 4 | 已拒绝 | 是 |
| 5 | 已撤回 | 是 |

招聘方允许的流转：

| 当前状态 | 可变更为 |
| --- | --- |
| 0 投递中 | 1 已查看、2 面试中、3 已录用、4 已拒绝 |
| 1 已查看 | 2 面试中、3 已录用、4 已拒绝 |
| 2 面试中 | 3 已录用、4 已拒绝 |
| 3 已录用 | 不允许继续流转 |
| 4 已拒绝 | 不允许继续流转 |
| 5 已撤回 | 不允许继续流转 |

状态变化会通过 RocketMQ 异步生成通知。

## 9. 通知接口

所有通知接口都需要登录。通知状态：0 未读，1 已读。

### 9.1 通知字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 通知 ID |
| eventId | string | MQ 事件唯一标识 |
| deliveryId | long | 关联投递 ID |
| receiverId | long | 接收用户 ID |
| type | string | 通知类型 |
| content | string | 通知内容 |
| status | integer | 0 未读，1 已读 |
| createTime | datetime | 创建时间 |

通知类型：

| type | 接收方 | 说明 |
| --- | --- | --- |
| DELIVERY_SUBMITTED | 招聘方 | 收到新的职位投递 |
| DELIVERY_VIEWED | 求职者 | 招聘方已查看投递 |
| DELIVERY_INTERVIEW | 求职者 | 投递进入面试环节 |
| DELIVERY_HIRED | 求职者 | 已被录用 |
| DELIVERY_REJECTED | 求职者 | 投递未通过 |
| DELIVERY_WITHDRAWN | 招聘方 | 求职者撤回投递 |

### 9.2 查询我的通知

GET /notification/me?page=1&size=10

返回当前用户收到的通知，按创建时间倒序排列。

### 9.3 查询未读数量

GET /notification/unread-count

~~~json
{
  "code": 200,
  "message": "成功",
  "data": 3
}
~~~

### 9.4 标记通知已读

PUT /notification/{id}/read

只能操作当前用户收到的通知。重复标记已读也返回成功；通知不存在返回 404，不属于当前用户返回 403。

## 10. 内部微服务接口

以下接口供 auth-service 通过 OpenFeign 调用，不属于前端业务 API。

### 10.1 创建用户

POST /user

user-service 直连地址：http://localhost:8082/user

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| phone | string | 手机号 |
| password | string | 明文密码，user-service 入库前进行 BCrypt 加密 |
| nickname | string | 昵称 |
| type | integer | 1 求职者，2 招聘方；空值默认 1 |

成功时 data 为用户 ID。

安全说明：当前 Gateway 配置了 /user/** 路由，因此该接口技术上也可从 8080 访问，但它的设计用途仅是 auth-service 内部调用，前端不要直接使用。

### 10.2 按手机号查询内部用户

GET /internal/user/phone/{phone}

user-service 直连地址示例：http://localhost:8082/internal/user/phone/13800000001

该接口返回 InternalUserDTO，其中包含 BCrypt 密码摘要，仅供 auth-service 校验登录密码。当前 Gateway 没有配置 /internal/user/** 路由，前端无法通过 8080 正常访问，也不应直接调用。

内部响应字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 用户 ID |
| phone | string | 手机号 |
| password | string | BCrypt 密码摘要，敏感字段 |
| nickname | string | 昵称 |
| type | integer | 1 求职者，2 招聘方 |
| status | integer | 0 禁用，1 正常 |
| createTime | datetime | 创建时间 |

本文不提供包含密码摘要的响应示例。

## 11. 常见错误与联调提示

### 11.1 8080 根路径返回 404

Gateway 只负责转发已配置的业务路径，没有首页控制器。请使用 /auth/**、/position/**、/resume/**、/delivery/** 或 /notification/**。

### 11.2 返回“未登录或 token 失效”

确认请求头名称为 token，并且值只包含登录响应的 data，不要添加 Bearer 前缀：

~~~http
token: YOUR_TOKEN
~~~

### 11.3 注册时验证码错误

先调用 POST /auth/code，再从 auth-service 控制台查看模拟验证码，并在 5 分钟内注册。

### 11.4 投递或修改状态失败

确认 Nacos、MySQL、Redis 和 RocketMQ 均已启动。delivery-service 使用 RocketMQ 同步发送消息，消息服务不可用可能导致写操作失败。

### 11.5 AI 匹配未配置

在 resume-service 的本地配置或环境变量中设置 DeepSeek API Key，并启用 recruit.ai.enabled。不要把真实 Key 提交到 Git。

### 11.6 本地 Profile

启动 auth-service、user-service、position-service、resume-service、delivery-service 和 notification-service 时，应根据各服务配置激活 local Profile，以便读取 application-local.yml 中的本地连接信息。
