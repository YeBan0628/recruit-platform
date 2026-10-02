<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myNotifications, markRead } from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'
import type { Notification } from '@/types'
import { formatDateTime } from '@/utils/format'

const notification = useNotificationStore()
const loading = ref(false)
const list = ref<Notification[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const TYPE_LABEL: Record<string, string> = {
  DELIVERY_SUBMITTED: '收到新投递',
  DELIVERY_VIEWED: '投递已被查看',
  DELIVERY_INTERVIEW: '进入面试环节',
  DELIVERY_HIRED: '已被录用',
  DELIVERY_REJECTED: '投递未通过',
  DELIVERY_WITHDRAWN: '候选人撤回投递',
}

async function load() {
  loading.value = true
  try {
    const res = await myNotifications(page.value, size.value)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function onRead(n: Notification) {
  if (n.status === 1) return
  await markRead(n.id)
  n.status = 1
  ElMessage.success('已标记为已读')
  notification.refresh()
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="notifications">
    <el-card shadow="never">
      <template #header><span class="title">通知中心</span></template>

      <el-empty v-if="!loading && list.length === 0" description="暂无通知" />

      <div
        v-for="n in list"
        :key="n.id"
        class="row"
        :class="{ unread: n.status === 0 }"
        @click="onRead(n)"
      >
        <div class="row-main">
          <div class="row-top">
            <el-tag size="small" type="info">{{ TYPE_LABEL[n.type] || n.type }}</el-tag>
            <el-badge v-if="n.status === 0" is-dot class="dot" />
          </div>
          <div class="content">{{ n.content }}</div>
          <div class="time">{{ formatDateTime(n.createTime) }}</div>
        </div>
        <el-button v-if="n.status === 0" text type="primary" @click.stop="onRead(n)">标记已读</el-button>
        <span v-else class="read-text">已读</span>
      </div>

      <el-pagination
        v-if="total > size"
        class="pager"
        layout="prev, pager, next, total"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="(p: number) => { page = p; load() }"
      />
    </el-card>
  </div>
</template>

<style scoped>
.title {
  font-weight: 600;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  border-radius: 6px;
}
.row:hover {
  background: #f7f8fa;
}
.row.unread {
  background: #f0fbfb;
}
.row-main {
  min-width: 0;
}
.row-top {
  display: flex;
  align-items: center;
  gap: 8px;
}
.dot {
  margin-top: -8px;
}
.content {
  margin-top: 6px;
  color: #1f2329;
  line-height: 1.6;
}
.time {
  margin-top: 4px;
  color: #86909c;
  font-size: 12px;
}
.read-text {
  color: #c9cdd4;
  font-size: 13px;
}
.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
