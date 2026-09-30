# Vue 3 前端设计（web/）

日期：2026-09-30
状态：已确认

## 1. 目标与范围

为 recruit-platform 搭建第一版 Web 前端，覆盖现有全部后端接口（docs/API.md）：

- 双角色单页应用（SPA）：求职者（type=1）+ 招聘方（type=2），登录后按角色展示界面
- 功能模块：注册登录、职位搜索/详情/热榜、简历 CRUD、投递与状态流转、通知、AI 简历职位匹配
- 桌面端优先，不做移动端适配；内容区固定最大宽度居中

不包含：实时聊天（chat 服务未实现）、SSR/SEO、E2E 测试框架。

## 2. 技术方案

Vite SPA（对比过 Nuxt 3 SSR 与双应用拆分，选择复杂度最低、后续可平滑升级的方案 A）。

技术栈：Vite 7 + Vue 3.5 + TypeScript + Pinia + Vue Router 4 + Element Plus + Axios。

## 3. 目录结构

```
recruit-platform/
└── web/
    ├── package.json
    ├── vite.config.ts          # dev proxy: /api → http://localhost:8080
    ├── index.html
    └── src/
        ├── main.ts
        ├── api/
        │   ├── request.ts      # Axios 实例：token 头、统一响应拆包、401 处理
        │   ├── auth.ts
        │   ├── user.ts
        │   ├── position.ts
        │   ├── resume.ts
        │   ├── delivery.ts
        │   └── notification.ts
        ├── types/              # 与 docs/API.md 字段一一对应的类型定义
        ├── stores/
        │   ├── auth.ts         # token + 用户信息（含 type），持久化 localStorage
        │   └── notification.ts # 未读数
        ├── router/index.ts     # 路由 + 登录守卫 + 角色守卫
        ├── layouts/            # JobseekerLayout / RecruiterLayout / BlankLayout
        └── views/              # 页面组件，按角色分目录
```

所有请求 baseURL 统一为 `/api`，开发期由 Vite 代理转发到网关 8080（rewrite 去掉 `/api` 前缀，因为网关路径是 `/auth/**`、`/position/**` 等），无跨域问题。

## 4. API 层约定

- 请求拦截器：从 auth store 读取 token，附加 `token: <value>` 请求头（Sa-Token 约定）
- 响应拦截器：判断**响应体中的 code**（HTTP 状态通常仍为 200）
  - `code === 200`：直接返回 `data` 字段
  - `code === 401`：清除本地凭证，跳转 `/login`
  - 其他：`ElMessage.error(message)` 提示并 reject
- 分页接口使用 MyBatis-Plus Page 结构，定义泛型 `PageResult<T>`（records/total/size/current/pages）
- 日期格式 `yyyy-MM-dd`，分页参数 page >= 1、1 <= size <= 100

## 5. 后端配套改动

当前登录接口（POST /auth/login）只返回 token 字符串，前端无法获知用户角色。需在 user-service 新增：

```
GET /user/me    # 需登录，返回 { id, phone, nickname, type }
```

前端登录成功后调用该接口获取角色，再跳转到对应角色首页。

## 6. 路由设计

| 路径 | 布局 | 权限 | 说明 |
| --- | --- | --- | --- |
| /login | Blank | 公开 | 手机号 + 密码登录 |
| /register | Blank | 公开 | 手机号 + 验证码 + 密码 + 角色选择（type 1/2） |
| /jobs | Jobseeker | 登录 + type=1 | 职位搜索：关键词/城市/薪资筛选 + 分页 + 热榜侧栏 |
| /jobs/:id | Jobseeker | 登录 + type=1 | 职位详情 + AI 匹配按钮 + 投递按钮 |
| /my/resume | Jobseeker | 登录 + type=1 | 简历主体 + 教育/工作/项目经历 + 技能分块编辑 |
| /my/deliveries | Jobseeker | 登录 + type=1 | 我的投递：状态标签 + 撤回操作 |
| /notifications | 当前角色布局 | 登录 | 通知列表 + 未读标记 + 标记已读（页面组件两端共用，路由各自注册） |
| /recruiter/positions | Recruiter | 登录 + type=2 | 我发布的职位：上下架/编辑/删除 |
| /recruiter/positions/new | Recruiter | 登录 + type=2 | 发布职位表单 |
| /recruiter/positions/:id/applications | Recruiter | 登录 + type=2 | 收到的投递：候选人列表 + 状态流转 |

守卫规则：无 token → 重定向 `/login`；角色不符 → 重定向各自首页（求职者 `/jobs`，招聘方 `/recruiter/positions`）。

顶部导航栏含未读通知角标：进入页面拉取一次 + 60 秒轮询。

## 7. 状态管理

- `auth` store：token、用户信息（含 type），持久化 localStorage；退出登录清空
- `notification` store：未读数
- 其余数据（职位列表、简历、投递）不做全局 store，页面内直接调 API 层获取

## 8. 错误处理

- 拦截器统一弹错（ElMessage.error）
- 页面级空态用 `el-empty`
- AI 匹配接口（POST /resume/match/{positionId}）耗时较长：按钮 loading 状态 + 60s 超时

## 9. 测试与验证

- Vitest 单测覆盖 `request.ts` 的响应拆包与 401 处理逻辑
- 集成验证：启动本地微服务后，浏览器走通完整链路
  注册 → 登录 → 搜职位 → 投简历 → 招聘方处理投递 → 通知已读
- 第一版不引入 E2E 框架，手动验证

## 10. 交付顺序

1. 后端补 GET /user/me
2. web/ 工程骨架（Vite + 路由 + 布局 + API 层 + stores）
3. 注册登录
4. 求职者端：职位搜索/详情 → 简历管理 → 投递 → 通知 → AI 匹配
5. 招聘方端：职位管理 → 投递处理 → 通知
6. 全链路手动验证 + 单测
