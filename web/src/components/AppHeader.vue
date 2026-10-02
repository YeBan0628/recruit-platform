<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'

interface MenuItem {
  to: string
  label: string
}

defineProps<{
  title: string
  menus: MenuItem[]
  notificationTo: string
}>()

const router = useRouter()
const auth = useAuthStore()
const notification = useNotificationStore()

onMounted(() => {
  notification.startPolling()
})

onUnmounted(() => {
  notification.stopPolling()
})

async function onLogout() {
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  auth.logout()
  notification.stopPolling()
  router.push('/login')
}
</script>

<template>
  <div class="app-header">
    <div class="header-inner">
      <div class="brand" @click="router.push(menus[0]?.to ?? '/')">
        <span class="logo">招</span>
        <span class="title">{{ title }}</span>
      </div>

      <el-menu :default-active="$route.path" mode="horizontal" router class="nav" :ellipsis="false">
        <el-menu-item v-for="m in menus" :key="m.to" :index="m.to">{{ m.label }}</el-menu-item>
      </el-menu>

      <div class="actions">
        <el-badge :value="notification.unread" :hidden="notification.unread === 0" :max="99">
          <el-button text circle @click="router.push(notificationTo)">
            <el-icon :size="20"><Bell /></el-icon>
          </el-button>
        </el-badge>

        <el-dropdown @command="(c: string) => c === 'logout' && onLogout()">
          <span class="user">
            {{ auth.user?.nickname || auth.user?.phone || '用户' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: var(--page-max-width);
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 16px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #00bebd;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.nav {
  flex: 1;
  border-bottom: none;
}
.actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}
.user {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  outline: none;
}
</style>
