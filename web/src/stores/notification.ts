import { defineStore } from 'pinia'
import { ref } from 'vue'
import { unreadCount } from '@/api/notification'
import { useAuthStore } from './auth'

export const useNotificationStore = defineStore('notification', () => {
  const unread = ref(0)
  let timer: number | null = null

  async function refresh() {
    const auth = useAuthStore()
    if (!auth.isLogin) {
      unread.value = 0
      return
    }
    try {
      unread.value = await unreadCount()
    } catch {
      // 拉取失败不阻塞界面
    }
  }

  function startPolling() {
    stopPolling()
    refresh()
    timer = window.setInterval(refresh, 60000)
  }

  function stopPolling() {
    if (timer !== null) {
      window.clearInterval(timer)
      timer = null
    }
  }

  return { unread, refresh, startPolling, stopPolling }
})
