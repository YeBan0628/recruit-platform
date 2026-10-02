// 与 docs/API.md 字段一一对应的类型定义

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 用户
export interface UserInfo {
  id: number
  phone: string
  nickname?: string
  type: number // 1 求职者, 2 招聘方
  status?: number
  createTime?: string
}

export interface LoginRequest {
  phone: string
  password: string
}

export interface RegisterRequest {
  phone: string
  password: string
  nickname?: string
  type?: number
  code: string
}

// 职位
export interface Position {
  id: number
  userId: number
  title: string
  city?: string
  salaryMin?: number
  salaryMax?: number
  experience?: string
  education?: string
  description?: string
  status: number // 0 下架, 1 上架
  createTime?: string
  updateTime?: string
}

export interface PositionForm {
  title: string
  city?: string
  salaryMin?: number
  salaryMax?: number
  experience?: string
  education?: string
  description?: string
}

export interface PositionQuery {
  page?: number
  size?: number
  city?: string
  keyword?: string
}

// 简历
export interface ResumeMain {
  id: number
  userId: number
  name: string
  phone?: string
  email?: string
  summary?: string
  status: number // 0 隐藏, 1 启用
  createTime?: string
  updateTime?: string
}

export interface ResumeForm {
  name: string
  phone?: string
  email?: string
  summary?: string
  status?: number
}

export interface Education {
  id?: number
  school?: string
  major?: string
  degree?: string
  startDate?: string
  endDate?: string
  description?: string
}

export interface Work {
  id?: number
  company?: string
  position?: string
  startDate?: string
  endDate?: string
  description?: string
}

export interface Project {
  id?: number
  projectName?: string
  role?: string
  startDate?: string
  endDate?: string
  description?: string
}

export interface Skill {
  id?: number
  skillName: string
  proficiency?: string
}

export interface FullResume {
  resume: ResumeMain
  educations: Education[]
  works: Work[]
  projects: Project[]
  skills: Skill[]
}

export interface MatchResult {
  score: number
  summary: string
  strengths: string[]
  gaps: string[]
}

// 投递
export interface Delivery {
  id: number
  positionId: number
  applicantId: number
  status: number // 0投递中 1已查看 2面试中 3已录用 4已拒绝 5已撤回
  message?: string
  createTime?: string
  updateTime?: string
}

export interface DeliveryCreate {
  positionId: number
  message?: string
}

// 通知
export interface Notification {
  id: number
  eventId?: string
  deliveryId?: number
  receiverId: number
  type: string
  content: string
  status: number // 0 未读, 1 已读
  createTime?: string
}

// 简历子项表单字段定义（供 ResumeSubSection 通用组件使用）
export interface FieldDef {
  key: string
  label: string
  type?: 'text' | 'textarea' | 'date'
  required?: boolean
  maxlength?: number
  placeholder?: string
}

// 投递状态元数据
export const DELIVERY_STATUS: Record<number, { label: string; tag: string }> = {
  0: { label: '投递中', tag: 'info' },
  1: { label: '已查看', tag: 'primary' },
  2: { label: '面试中', tag: 'warning' },
  3: { label: '已录用', tag: 'success' },
  4: { label: '已拒绝', tag: 'danger' },
  5: { label: '已撤回', tag: 'info' },
}
