import request from './request'
import type {
  Education,
  FullResume,
  MatchResult,
  Project,
  ResumeForm,
  Skill,
  Work,
} from '@/types'

export function getMyResume() {
  return request.get<unknown, FullResume>('/resume/me')
}

export function createResume(data: ResumeForm) {
  return request.post<unknown, number>('/resume', data)
}

export function updateResume(data: ResumeForm) {
  return request.put<unknown, void>('/resume', data)
}

export function deleteResume() {
  return request.delete<unknown, void>('/resume')
}

// 教育经历
export function addEducation(data: Education) {
  return request.post<unknown, number>('/resume/education', data)
}
export function updateEducation(id: number, data: Education) {
  return request.put<unknown, void>(`/resume/education/${id}`, data)
}
export function deleteEducation(id: number) {
  return request.delete<unknown, void>(`/resume/education/${id}`)
}

// 工作经历
export function addWork(data: Work) {
  return request.post<unknown, number>('/resume/work', data)
}
export function updateWork(id: number, data: Work) {
  return request.put<unknown, void>(`/resume/work/${id}`, data)
}
export function deleteWork(id: number) {
  return request.delete<unknown, void>(`/resume/work/${id}`)
}

// 项目经历
export function addProject(data: Project) {
  return request.post<unknown, number>('/resume/project', data)
}
export function updateProject(id: number, data: Project) {
  return request.put<unknown, void>(`/resume/project/${id}`, data)
}
export function deleteProject(id: number) {
  return request.delete<unknown, void>(`/resume/project/${id}`)
}

// 技能
export function addSkill(data: Skill) {
  return request.post<unknown, number>('/resume/skill', data)
}
export function updateSkill(id: number, data: Skill) {
  return request.put<unknown, void>(`/resume/skill/${id}`, data)
}
export function deleteSkill(id: number) {
  return request.delete<unknown, void>(`/resume/skill/${id}`)
}

// AI 简历职位匹配（耗时较长，单独设置 60s 超时）
export function matchPosition(positionId: number) {
  return request.post<unknown, MatchResult>(`/resume/match/${positionId}`, null, { timeout: 60000 })
}
