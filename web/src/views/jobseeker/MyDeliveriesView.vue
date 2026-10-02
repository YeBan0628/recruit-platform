<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myDeliveries, withdrawDelivery } from '@/api/delivery'
import { getPosition } from '@/api/position'
import { useNotificationStore } from '@/stores/notification'
import type { Delivery } from '@/types'
import { DELIVERY_STATUS } from '@/types'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const notification = useNotificationStore()

const loading = ref(false)
const list = ref<Delivery[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const titles = ref<Record<number, string>>({})

function canWithdraw(status: number) {
  return status === 0 || status === 1 || status === 2
}

async function load() {
  loading.value = true
  try {
    const res = await myDeliveries(page.value, size.value)
    list.value = res.records
    total.value = res.total
    await enrichTitles()
  } finally {
    loading.value = false
  }
}

async function enrichTitles() {
  const ids = [...new Set(list.value.map((d) => d.positionId))].filter((id) => titles.value[id] === undefined)
  await Promise.all(
    ids.map(async (id) => {
      try {
        const p = await getPosition(id)
        titles.value[id] = p.title
      } catch {
        titles.value[id] = `职位 #${id}`
      }
    }),
  )
}

async function onWithdraw(d: Delivery) {
  await ElMessageBox.confirm('确定撤回这条投递吗？', '提示', { type: 'warning' })
  await withdrawDelivery(d.id)
  ElMessage.success('已撤回')
  notification.refresh()
  await load()
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="deliveries">
    <el-card shadow="never">
      <template #header><span class="title">我的投递</span></template>

      <el-empty v-if="!loading && list.length === 0" description="还没有投递任何职位">
        <el-button type="primary" @click="router.push('/jobs')">去找职位</el-button>
      </el-empty>

      <div v-for="d in list" :key="d.id" class="row">
        <div class="row-main" @click="router.push(`/jobs/${d.positionId}`)">
          <div class="pos-title">{{ titles[d.positionId] || `职位 #${d.positionId}` }}</div>
          <div class="meta">投递于 {{ formatDateTime(d.createTime) }}</div>
          <div v-if="d.message" class="msg">留言：{{ d.message }}</div>
        </div>
        <div class="row-ops">
          <el-tag :type="(DELIVERY_STATUS[d.status]?.tag as any) || 'info'">
            {{ DELIVERY_STATUS[d.status]?.label || '未知' }}
          </el-tag>
          <el-button v-if="canWithdraw(d.status)" text type="danger" @click="onWithdraw(d)">撤回</el-button>
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
.title {
  font-weight: 600;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.row:last-child {
  border-bottom: none;
}
.row-main {
  cursor: pointer;
  min-width: 0;
}
.pos-title {
  font-weight: 600;
  font-size: 15px;
}
.pos-title:hover {
  color: #00bebd;
}
.meta {
  color: #86909c;
  font-size: 13px;
  margin-top: 4px;
}
.msg {
  color: #4e5969;
  font-size: 13px;
  margin-top: 6px;
}
.row-ops {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
