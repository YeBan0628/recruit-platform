<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getPosition } from '@/api/position'
import { createDelivery } from '@/api/delivery'
import { matchPosition } from '@/api/resume'
import type { MatchResult, Position } from '@/types'
import { formatSalary, formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const positionId = Number(route.params.id)

const loading = ref(true)
const position = ref<Position | null>(null)

const applyVisible = ref(false)
const applyMessage = ref('')
const applying = ref(false)

const matching = ref(false)
const match = ref<MatchResult | null>(null)

async function load() {
  loading.value = true
  try {
    position.value = await getPosition(positionId)
  } catch {
    position.value = null
  } finally {
    loading.value = false
  }
}

async function onApply() {
  applying.value = true
  try {
    await createDelivery({ positionId, message: applyMessage.value || undefined })
    ElMessage.success('投递成功')
    applyVisible.value = false
    applyMessage.value = ''
  } finally {
    applying.value = false
  }
}

async function onMatch() {
  matching.value = true
  match.value = null
  try {
    match.value = await matchPosition(positionId)
  } finally {
    matching.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="detail">
    <el-button text :icon="ArrowLeft" @click="router.back()">返回</el-button>

    <el-empty v-if="!loading && !position" description="职位不存在或已下架" />

    <template v-if="position">
      <el-card shadow="never" class="head-card">
        <div class="head">
          <div>
            <h1 class="title">{{ position.title }}</h1>
            <div class="tags">
              <el-tag v-if="position.city" type="info">{{ position.city }}</el-tag>
              <el-tag v-if="position.experience" type="info">{{ position.experience }}</el-tag>
              <el-tag v-if="position.education" type="info">{{ position.education }}</el-tag>
            </div>
            <div class="time">发布于 {{ formatDateTime(position.createTime) }}</div>
          </div>
          <div class="salary">{{ formatSalary(position.salaryMin, position.salaryMax) }}</div>
        </div>
        <div class="ops">
          <el-button type="primary" size="large" @click="applyVisible = true">投递简历</el-button>
          <el-button size="large" :loading="matching" @click="onMatch">AI 匹配度分析</el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="desc-card">
        <template #header><span class="card-title">职位描述</span></template>
        <pre class="desc">{{ position.description || '暂无职位描述' }}</pre>
      </el-card>

      <el-card v-if="match || matching" shadow="never" class="match-card" v-loading="matching">
        <template #header><span class="card-title">AI 匹配结果</span></template>
        <template v-if="match">
          <div class="score-row">
            <el-progress type="dashboard" :percentage="match.score" :width="120" />
            <p class="summary">{{ match.summary }}</p>
          </div>
          <el-divider />
          <div class="match-cols">
            <div class="col">
              <h4 class="strength">✓ 匹配优势</h4>
              <ul>
                <li v-for="(s, i) in match.strengths" :key="i">{{ s }}</li>
                <li v-if="match.strengths.length === 0" class="muted">无</li>
              </ul>
            </div>
            <div class="col">
              <h4 class="gap">! 待提升项</h4>
              <ul>
                <li v-for="(g, i) in match.gaps" :key="i">{{ g }}</li>
                <li v-if="match.gaps.length === 0" class="muted">无</li>
              </ul>
            </div>
          </div>
        </template>
      </el-card>
    </template>

    <el-dialog v-model="applyVisible" title="投递简历" width="480px">
      <el-form label-position="top">
        <el-form-item label="投递留言（选填）">
          <el-input
            v-model="applyMessage"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="向招聘方简单介绍自己"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="onApply">确认投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.head-card {
  margin: 12px 0 16px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.title {
  font-size: 24px;
  margin: 0 0 10px;
}
.tags {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.time {
  color: #86909c;
  font-size: 13px;
}
.salary {
  color: #ff6b35;
  font-size: 24px;
  font-weight: 700;
}
.ops {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}
.desc-card,
.match-card {
  margin-bottom: 16px;
}
.card-title {
  font-weight: 600;
}
.desc {
  white-space: pre-wrap;
  font-family: inherit;
  margin: 0;
  line-height: 1.7;
  color: #4e5969;
}
.score-row {
  display: flex;
  align-items: center;
  gap: 24px;
}
.summary {
  flex: 1;
  color: #4e5969;
  line-height: 1.7;
  margin: 0;
}
.match-cols {
  display: flex;
  gap: 32px;
}
.col {
  flex: 1;
}
.col h4 {
  margin: 0 0 8px;
}
.strength {
  color: #00b42a;
}
.gap {
  color: #ff7d00;
}
.col ul {
  margin: 0;
  padding-left: 18px;
  color: #4e5969;
  line-height: 1.9;
}
.muted {
  color: #c9cdd4;
  list-style: none;
}
</style>
