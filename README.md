# recruit-platform · 智能招聘平台

一个基于 **Spring Cloud Alibaba 微服务** 架构的招聘/求职平台（仿 Boss 直聘），支持职位搜索、简历投递、实时沟通，并集成 **Spring AI** 提供简历解析、职位匹配、AI 模拟面试等能力。

## 技术栈

- **语言/运行时**：JDK 17
- **基础框架**：Spring Boot 3.2.x、MyBatis-Plus
- **微服务**：Spring Cloud Alibaba 2023.x（Nacos、Sentinel）、Spring Cloud Gateway、OpenFeign
- **存储/中间件**：MySQL 8、Redis、RocketMQ 5.5、MinIO（预留）
- **鉴权**：Sa-Token
- **AI**：Spring AI 0.8.1 + DeepSeek（兼容 Spring Boot 3.2.5）

## DeepSeek 职位匹配

`resume-service` 已通过 Spring AI `ChatClient` 接入 DeepSeek。启动前在当前 PowerShell 会话设置：

```powershell
$env:RECRUIT_AI_ENABLED="true"
$env:DEEPSEEK_API_KEY="你的 DeepSeek API Key"
```

可选配置：`DEEPSEEK_BASE_URL`，默认 `https://api.deepseek.com`；`DEEPSEEK_MODEL`，默认 `deepseek-chat`。密钥只通过环境变量传入，不写入配置文件和 Git。

匹配接口：

```text
POST /resume/match/{positionId}
```

## 架构

```
gateway → 网关（路由 / 限流 / 鉴权 / WebSocket 代理）
auth    → 认证中心（注册 / 登录 / 令牌 / 验证码）
user    → 用户服务（求职者 & 企业 & 资料）
resume  → 简历服务（简历 CRUD + 学历/经历/项目）
position→ 职位服务（发布 / 搜索 / 收藏 / 热度）
delivery→ 投递服务（投递 / 状态机 / 通知）
chat    → 聊天服务（WebSocket 实时 IM / 未读数）
ai      → AI 服务（简历解析 / 职位匹配 / 模拟面试 / 智能问答）
```

## 核心亮点

- 投递状态机 + 事件驱动，RocketMQ 最终一致性
- Redis 多角色复用：缓存 / 计数 / 排行 / Geo 附近职位 / 分布式锁 / 在线状态 / 向量检索
- 简历 PDF 上传异步 AI 解析回填（结构化输出）
- 向量语义匹配替代传统关键词搜索，无需 Elasticsearch
- WebSocket 实时聊天 + 未读消息计数 + 可靠投递



## 目录结构

```
recruit-platform
├── gateway          # 网关
├── auth             # 认证中心
├── user             # 用户服务
├── resume           # 简历服务
├── position         # 职位服务
├── delivery         # 投递服务
├── chat             # 聊天服务
├── ai               # AI 服务
├── common           # 公共模块
├── api              # Feign 接口定义
└── sql              # 数据库脚本
```
