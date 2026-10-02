import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMe } from '@/api/user'
import { TOKEN_KEY, USER_KEY, getToken, clearAuth } from '@/api/request'
import type { UserInfo } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserInfo | null>(readUser())

  function readUser(): UserInfo | null {
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw) as UserInfo
    } catch {
      return null
    }
  }

  const isLogin = computed(() => !!token.value)
  const isJobseeker = computed(() => user.value?.type === 1)
  const isRecruiter = computed(() => user.value?.type === 2)
  const role = computed(() => user.value?.type ?? null)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem(TOKEN_KEY, t)
  }

  function setUser(u: UserInfo) {
    user.value = u
    localStorage.setItem(USER_KEY, JSON.stringify(u))
  }

  // 登录成功后调用，拉取用户信息（含角色）
  async function fetchMe() {
    const u = await getMe()
    setUser(u)
    return u
  }

  function logout() {
    token.value = null
    user.value = null
    clearAuth()
  }

  return { token, user, isLogin, isJobseeker, isRecruiter, role, setToken, setUser, fetchMe, logout }
})
