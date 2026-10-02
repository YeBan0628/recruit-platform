<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import { searchPositions, hotPositions } from '@/api/position'
import type { Position } from '@/types'
import { formatSalary } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const list = ref<Position[]>([])
const total = ref(0)
const hot = ref<Position[]>([])

const query = reactive({ page: 1, size: 10, keyword: '', city: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await searchPositions({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      city: query.city || undefined,
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadHot() {
  try {
    hot.value = await hotPositions(10)
  } catch {
    hot.value = []
  }
}

function onSearch() {
  query.page = 1
  loadList()
}

function onReset() {
  query.keyword = ''
  query.city = ''
  query.page = 1
  loadList()
}

onMounted(() => {
  loadList()
  loadHot()
})
</script>

<template>
  <div class="job-list">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="职位名称关键词" clearable style="width: 240px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.city" placeholder="城市" clearable style="width: 160px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="content">
      <div class="main">
        <div v-loading="loading" class="list-wrap">
          <el-empty v-if="!loading && list.length === 0" description="暂无匹配的职位" />
          <div
            v-for="p in list"
            :key="p.id"
            class="job-card"
            @click="router.push(`/jobs/${p.id}`)"
          >
            <div class="job-top">
              <span class="job-title">{{ p.title }}</span>
              <span class="job-salary">{{ formatSalary(p.salaryMin, p.salaryMax) }}</span>
            </div>
            <div class="job-tags">
              <el-tag v-if="p.city" size="small" type="info">{{ p.city }}</el-tag>
              <el-tag v-if="p.experience" size="small" type="info">{{ p.experience }}</el-tag>
              <el-tag v-if="p.education" size="small" type="info">{{ p.education }}</el-tag>
            </div>
            <p class="job-desc">{{ p.description || '暂无职位描述' }}</p>
          </div>
        </div>

        <el-pagination
          v-if="total > 0"
          class="pager"
          layout="prev, pager, next, total"
          :total="total"
          :page-size="query.size"
          :current-page="query.page"
          @current-change="(p: number) => { query.page = p; loadList() }"
        />
      </div>

      <aside class="side">
        <el-card shadow="never">
          <template #header>
            <span class="side-title">🔥 职位热榜</span>
          </template>
          <el-empty v-if="hot.length === 0" description="暂无热榜" :image-size="60" />
          <ol class="hot-list">
            <li v-for="(h, i) in hot" :key="h.id" @click="router.push(`/jobs/${h.id}`)">
              <span class="rank" :class="{ top: i < 3 }">{{ i + 1 }}</span>
              <span class="hot-name">{{ h.title }}</span>
              <span class="hot-salary">{{ formatSalary(h.salaryMin, h.salaryMax) }}</span>
            </li>
          </ol>
        </el-card>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.search-card {
  margin-bottom: 16px;
}
.content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.main {
  flex: 1;
  min-width: 0;
}
.list-wrap {
  min-height: 200px;
}
.job-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  border: 1px solid #f0f0f0;
}
.job-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}
.job-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.job-title {
  font-size: 17px;
  font-weight: 600;
}
.job-salary {
  color: #ff6b35;
  font-weight: 600;
}
.job-tags {
  margin: 8px 0;
  display: flex;
  gap: 8px;
}
.job-desc {
  margin: 0;
  color: #86909c;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.pager {
  margin-top: 16px;
  justify-content: center;
}
.side {
  width: 300px;
  flex-shrink: 0;
}
.side-title {
  font-weight: 600;
}
.hot-list {
  margin: 0;
  padding: 0;
  list-style: none;
}
.hot-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  cursor: pointer;
  font-size: 14px;
}
.hot-list li:hover .hot-name {
  color: #00bebd;
}
.rank {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  background: #f0f0f0;
  color: #86909c;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.rank.top {
  background: #ff6b35;
  color: #fff;
}
.hot-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hot-salary {
  color: #ff6b35;
  font-size: 13px;
  flex-shrink: 0;
}
</style>
