import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

export const TOKEN_KEY = 'recruit_token'
export const USER_KEY = 'recruit_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

// 后端统一响应体
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

// 纯函数：根据响应体 code 判定结果，便于单测
export type Resolved<T> =
  | { ok: true; data: T }
  | { ok: false; code: number; message: string }

export function resolveBody<T>(body: ApiResult<T>): Resolved<T> {
  if (body.code === 200) {
    return { ok: true, data: body.data }
  }
  return { ok: false, code: body.code, message: body.message || '请求失败' }
}

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json; charset=UTF-8' },
})

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers ?? {}
    // Sa-Token 约定：请求头名为 token
    ;(config.headers as Record<string, string>).token = token
  }
  return config
})

service.interceptors.response.use(
  (response: AxiosResponse<ApiResult<unknown>>) => {
    const resolved = resolveBody(response.data)
    if (resolved.ok) {
      // 拆包：直接返回 data 字段
      return resolved.data as never
    }
    if (resolved.code === 401) {
      clearAuth()
      ElMessage.error(resolved.message || '未登录或登录已失效')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(resolved.message))
    }
    ElMessage.error(resolved.message)
    return Promise.reject(new Error(resolved.message))
  },
  (error) => {
    const msg = error?.message?.includes('timeout') ? '请求超时，请稍后重试' : '网络异常，请检查后端服务'
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

// 拆包后返回的直接是 data，因此泛型 T 即业务数据类型
export default service
