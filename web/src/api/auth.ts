import request from './request'
import type { LoginRequest, RegisterRequest } from '@/types'

// 发送模拟验证码：data 为 null，6 位验证码打印在 auth-service 控制台并写入 Redis（5 分钟有效）
export function sendCode(phone: string) {
  return request.post<unknown, null>('/auth/code', { phone })
}

export function register(data: RegisterRequest) {
  return request.post<unknown, number>('/auth/register', data)
}

// 登录成功返回 Sa-Token 字符串
export function login(data: LoginRequest) {
  return request.post<unknown, string>('/auth/login', data)
}
