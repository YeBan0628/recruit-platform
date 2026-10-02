<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { deliveriesByPosition, updateDeliveryStatus } from '@/api/delivery'
import { getPosition } from '@/api/position'
import { useNotificationStore } from '@/stores/notification'
import type { Delivery } from '@/types'
import { DELIVERY_STATUS } from '@/types'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const notification = useNotificationStore()
const positionId = Number(route.params.id)

const loading = ref(false)
const positionTitle = ref('')
const list = ref<Delivery[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

// 招聘方允许的状态流转
const NEXT: Record<number, number[]> = {
  0: [1, 2, 3, 4],
  1: [2, 3, 4],
  2: [3, 4],
}

const TAG_TYPE: Record<number, string> = {
  1: 'primary',
  2: 'warning',
  3: 'success',
  4: 'danger',
}

async function load() {
  loading.value = true
  try {
    const res = await deliveriesByPosition(positionId, page.value, size.value)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadTitle() {
  try {
    const p = await getPosition(positionId)
    positionTitle.value = p.title
  } catch {
    positionTitle.value = `职位 #${positionId}`
  }
}

async function setStatus(d: Delivery, status: number) {
  await updateDeliveryStatus(d.id, status)
  ElMessage.success(`已更新为「${DELIVERY_STATUS[status]?.label}」`)
  notification.refresh()
  await load()
}

onMounted(() => {
  loadTitle()
  load()
})
</script>

<template>
  <div v-loading="loading" class="applications">
    <el-button text :icon="ArrowLeft" @click="router.push('/recruiter/positions')">返回</el-button>
    <el-card shadow="never" class="card">
      <template #header>
        <span class="title">「{{ positionTitle }}」收到的投递</span>
      </template>

      <el-empty v-if="!loading && list.length === 0" description="还没有收到投递" />

      <div v-for="d in list" :key="d.id" class="row">
        <div class="row-main">
          <div class="row-top">
            <span class="applicant">候选人 #{{ d.applicantId }}</span>
            <el-tag :type="(DELIVERY_STATUS[d.status]?.tag as any) || 'info'" size="small">
              {{ DELIVERY_STATUS[d.status]?.label || '未知' }}
            </el-tag>
          </div>
          <div v-if="d.message" class="msg">{{ d.message }}</div>
          <div v-else class="msg muted">无留言</div>
          <div class="time">投递于 {{ formatDateTime(d.createTime) }}</div>
        </div>
        <div class="row-ops">
          <template v-if="NEXT[d.status]?.length">
            <el-button
              v-for="s in NEXT[d.status]"
              :key="s"
              size="small"
              :type="(TAG_TYPE[s] as any)"
              plain
              @click="setStatus(d, s)"
            >
              {{ DELIVERY_STATUS[s]?.label }}
            </el-button>
          </template>
          <span v-else class="final">流程已结束</span>
        </div>
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
.card {
  margin-top: 12px;
}
.title {
  font-weight: 600;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.row:last-child {
  border-bottom: none;
}
.row-main {
  min-width: 0;
}
.row-top {
  display: flex;
  align-items: center;
  gap: 10px;
}
.applicant {
  font-weight: 600;
}
.msg {
  margin-top: 6px;
  color: #4e5969;
  line-height: 1.6;
}
.msg.muted {
  color: #c9cdd4;
}
.time {
  margin-top: 4px;
  color: #86909c;
  font-size: 12px;
}
.row-ops {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.final {
  color: #c9cdd4;
  font-size: 13px;
}
.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
