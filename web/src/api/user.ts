import request from './request'
import type { UserInfo } from '@/types'

export function getMe() {
  return request.get<unknown, UserInfo>('/user/me')
}
