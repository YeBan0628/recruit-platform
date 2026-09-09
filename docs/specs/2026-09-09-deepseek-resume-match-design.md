# DeepSeek 简历职位匹配设计

## 1. 目标

在 resume-service 中增加基于 Spring AI 的职位匹配接口，使用 DeepSeek 对当前用户简历和指定职位进行直接打分，先实现同步 MVP，不引入向量检索。

## 2. 范围

包含：

- 当前登录用户简历与职位详情读取
- 结构化提示词组装
- DeepSeek Chat 调用
- 0～100 分匹配结果
- 总体结论、匹配优势、能力缺口

不包含：

- 向量数据库和 Embedding
- 匹配结果持久化
- 投递流程内同步调用
- AI 简历解析和模拟面试

## 3. 接口

- `POST /resume/match/{positionId}`
- 必须登录，从 Sa-Token 获取用户 ID
- 简历不存在返回业务错误
- 职位不存在返回 404
- 返回 `MatchResult`：`score`、`summary`、`strengths`、`gaps`

## 4. 模型配置

使用 Spring AI OpenAI 兼容客户端连接 DeepSeek：

- `DEEPSEEK_API_KEY`：必填密钥，不写入仓库
- `DEEPSEEK_BASE_URL`：默认 `https://api.deepseek.com`
- `DEEPSEEK_MODEL`：默认 `deepseek-chat`
- `RECRUIT_AI_ENABLED`：默认 `false`，关闭时接口返回明确配置错误

local 配置只保留环境变量占位符；没有密钥时不影响其他服务启动。

## 5. 调用与输出

- 将简历主信息、教育、工作、项目、技能和职位要求拼为纯文本提示词。
- 要求模型只返回 JSON，字段固定为 `score`、`summary`、`strengths`、`gaps`。
- 服务端校验分数范围；解析失败或模型异常统一返回 AI 调用失败，不泄漏密钥和原始异常。
- 单次请求同步等待模型响应，不在投递主流程中调用。

## 6. 依赖与兼容

- 使用与 Spring Boot 3.2.5 兼容的 Spring AI 0.8.1，通过官方 Spring Milestones 仓库解析依赖。
- 仅 resume-service 增加 Spring AI OpenAI Starter，不新增微服务。
- 复用现有 PositionApi 和 resume-service 内部数据，不修改数据库结构。
- Gateway 增加 `/resume/**` 路由无需变更。

## 7. 测试与验收

- 单元测试覆盖提示词构造、JSON 结果解析、分数边界和异常处理。
- 没有 API Key 时接口返回配置错误，其他服务启动正常。
- 配置有效时手动调用接口，返回结构化匹配结果。
- Maven 编译和测试通过。
