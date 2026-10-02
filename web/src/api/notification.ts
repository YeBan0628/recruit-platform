import request from './request'
import type { Notification, PageResult } from '@/types'

export function myNotifications(page = 1, size = 10) {
  return request.get<unknown, PageResult<Notification>>('/notification/me', {
    params: { page, size },
  })
}

export function unreadCount() {
  return request.get<unknown, number>('/notification/unread-count')
}

export function markRead(id: number) {
  return request.put<unknown, void>(`/notification/${id}/read`)
}
