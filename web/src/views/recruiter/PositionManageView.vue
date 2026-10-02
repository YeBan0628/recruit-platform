<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { myPositions, updatePositionStatus, deletePosition } from '@/api/position'
import type { Position } from '@/types'
import { formatSalary, formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const list = ref<Position[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function load() {
  loading.value = true
  try {
    const res = await myPositions(page.value, size.value)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(p: Position) {
  const next = p.status === 1 ? 0 : 1
  await updatePositionStatus(p.id, next)
  ElMessage.success(next === 1 ? '已上架' : '已下架')
  await load()
}

async function onDelete(p: Position) {
  await ElMessageBox.confirm(`确定删除职位「${p.title}」吗？`, '警告', { type: 'warning' })
  await deletePosition(p.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="manage">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <span class="title">我发布的职位</span>
          <el-button type="primary" :icon="Plus" @click="router.push('/recruiter/positions/new')">发布职位</el-button>
        </div>
      </template>

      <el-empty v-if="!loading && list.length === 0" description="还没有发布职位">
        <el-button type="primary" @click="router.push('/recruiter/positions/new')">发布第一个职位</el-button>
      </el-empty>

      <el-table v-if="list.length > 0" :data="list" style="width: 100%">
        <el-table-column label="职位" min-width="200">
          <template #default="{ row }">
            <div class="pos-title">{{ row.title }}</div>
            <div class="pos-sub">{{ row.city || '未填城市' }} · {{ formatDateTime(row.createTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="薪资" width="120">
          <template #default="{ row }">
            <span class="salary">{{ formatSalary(row.salaryMin, row.salaryMax) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="router.push(`/recruiter/positions/${row.id}/applications`)">查看投递</el-button>
            <el-button text type="primary" @click="router.push(`/recruiter/positions/${row.id}/edit`)">编辑</el-button>
            <el-button text @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button text type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-weight: 600;
}
.pos-title {
  font-weight: 600;
}
.pos-sub {
  color: #86909c;
  font-size: 12px;
  margin-top: 2px;
}
.salary {
  color: #ff6b35;
  font-weight: 600;
}
.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
