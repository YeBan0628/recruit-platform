import request from './request'
import type { PageResult, Position, PositionForm, PositionQuery } from '@/types'

export function searchPositions(query: PositionQuery) {
  return request.get<unknown, PageResult<Position>>('/position/page', { params: query })
}

export function hotPositions(limit = 10) {
  return request.get<unknown, Position[]>('/position/hot', { params: { limit } })
}

export function getPosition(id: number) {
  return request.get<unknown, Position>(`/position/${id}`)
}

export function myPositions(page = 1, size = 10) {
  return request.get<unknown, PageResult<Position>>('/position/me', { params: { page, size } })
}

export function createPosition(data: PositionForm) {
  return request.post<unknown, number>('/position', data)
}

export function updatePosition(id: number, data: PositionForm) {
  return request.put<unknown, void>(`/position/${id}`, data)
}

export function updatePositionStatus(id: number, status: number) {
  return request.put<unknown, void>(`/position/${id}/status`, null, { params: { status } })
}

export function deletePosition(id: number) {
  return request.delete<unknown, void>(`/position/${id}`)
}
