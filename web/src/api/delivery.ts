import request from './request'
import type { Delivery, DeliveryCreate, PageResult } from '@/types'

export function createDelivery(data: DeliveryCreate) {
  return request.post<unknown, number>('/delivery', data)
}

export function myDeliveries(page = 1, size = 10) {
  return request.get<unknown, PageResult<Delivery>>('/delivery/me', { params: { page, size } })
}

export function deliveriesByPosition(positionId: number, page = 1, size = 10) {
  return request.get<unknown, PageResult<Delivery>>(`/delivery/position/${positionId}`, {
    params: { page, size },
  })
}

export function updateDeliveryStatus(id: number, status: number) {
  return request.put<unknown, void>(`/delivery/${id}/status`, { status })
}

export function withdrawDelivery(id: number) {
  return request.put<unknown, void>(`/delivery/${id}/withdraw`)
}
