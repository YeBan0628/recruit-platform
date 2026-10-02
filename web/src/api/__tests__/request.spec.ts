import { describe, it, expect, beforeEach } from 'vitest'
import { resolveBody, getToken, clearAuth, TOKEN_KEY, USER_KEY } from '@/api/request'

describe('resolveBody 响应拆包', () => {
  it('code 200 时返回 ok 并携带 data', () => {
    const res = resolveBody({ code: 200, message: '成功', data: { id: 1 } })
    expect(res).toEqual({ ok: true, data: { id: 1 } })
  })

  it('code 200 且 data 为 null 时仍算成功', () => {
    const res = resolveBody<number | null>({ code: 200, message: '成功', data: null })
    expect(res.ok).toBe(true)
  })

  it('code 401 时返回未授权失败', () => {
    const res = resolveBody({ code: 401, message: '未登录或 token 失效', data: null })
    expect(res).toEqual({ ok: false, code: 401, message: '未登录或 token 失效' })
  })

  it('业务失败且 message 为空时使用兜底文案', () => {
    const res = resolveBody({ code: 500, message: '', data: null })
    expect(res.ok).toBe(false)
    if (!res.ok) {
      expect(res.message).toBe('请求失败')
    }
  })

  it('code 403 返回对应的失败码', () => {
    const res = resolveBody({ code: 403, message: '无权限', data: null })
    expect(res).toEqual({ ok: false, code: 403, message: '无权限' })
  })
})

describe('本地凭证存储', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('无 token 时 getToken 返回 null', () => {
    expect(getToken()).toBeNull()
  })

  it('写入后可读取 token', () => {
    localStorage.setItem(TOKEN_KEY, 'abc-123')
    expect(getToken()).toBe('abc-123')
  })

  it('clearAuth 清除 token 与 user', () => {
    localStorage.setItem(TOKEN_KEY, 'abc-123')
    localStorage.setItem(USER_KEY, '{"id":1}')
    clearAuth()
    expect(getToken()).toBeNull()
    expect(localStorage.getItem(USER_KEY)).toBeNull()
  })
})
