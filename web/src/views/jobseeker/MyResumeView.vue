<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ResumeSubSection from '@/components/ResumeSubSection.vue'
import * as resumeApi from '@/api/resume'
import type { FullResume, ResumeForm, FieldDef } from '@/types'

const loading = ref(true)
const hasResume = ref(false)
const data = ref<FullResume | null>(null)

// 创建/编辑简历主体
const mainDialog = ref(false)
const mainForm = reactive<ResumeForm>({ name: '', phone: '', email: '', summary: '', status: 1 })

const eduFields: FieldDef[] = [
  { key: 'school', label: '学校', maxlength: 100 },
  { key: 'major', label: '专业', maxlength: 100 },
  { key: 'degree', label: '学历', maxlength: 30 },
  { key: 'startDate', label: '开始日期', type: 'date' },
  { key: 'endDate', label: '结束日期', type: 'date' },
  { key: 'description', label: '描述', type: 'textarea', maxlength: 500 },
]
const workFields: FieldDef[] = [
  { key: 'company', label: '公司', maxlength: 100 },
  { key: 'position', label: '职位', maxlength: 100 },
  { key: 'startDate', label: '开始日期', type: 'date' },
  { key: 'endDate', label: '结束日期', type: 'date' },
  { key: 'description', label: '描述', type: 'textarea', maxlength: 1000 },
]
const projectFields: FieldDef[] = [
  { key: 'projectName', label: '项目名称', maxlength: 100 },
  { key: 'role', label: '担任角色', maxlength: 100 },
  { key: 'startDate', label: '开始日期', type: 'date' },
  { key: 'endDate', label: '结束日期', type: 'date' },
  { key: 'description', label: '描述', type: 'textarea', maxlength: 1000 },
]
const skillFields: FieldDef[] = [
  { key: 'skillName', label: '技能名称', required: true, maxlength: 50 },
  { key: 'proficiency', label: '熟练程度', maxlength: 30, placeholder: '如：熟悉 / 精通' },
]

async function load() {
  loading.value = true
  try {
    data.value = await resumeApi.getMyResume()
    hasResume.value = true
  } catch {
    // 404 表示尚未创建简历（拦截器已提示，这里静默处理为空态）
    hasResume.value = false
    data.value = null
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(mainForm, { name: '', phone: '', email: '', summary: '', status: 1 })
  mainDialog.value = true
}

function openEditMain() {
  if (!data.value) return
  const r = data.value.resume
  Object.assign(mainForm, { name: r.name, phone: r.phone ?? '', email: r.email ?? '', summary: r.summary ?? '', status: r.status })
  mainDialog.value = true
}

async function saveMain() {
  if (!mainForm.name) {
    ElMessage.warning('请填写姓名')
    return
  }
  const payload: ResumeForm = {
    name: mainForm.name,
    phone: mainForm.phone || undefined,
    email: mainForm.email || undefined,
    summary: mainForm.summary || undefined,
    status: mainForm.status,
  }
  if (hasResume.value) {
    await resumeApi.updateResume(payload)
    ElMessage.success('简历已更新')
  } else {
    await resumeApi.createResume(payload)
    ElMessage.success('简历已创建')
  }
  mainDialog.value = false
  await load()
}

async function removeResume() {
  await ElMessageBox.confirm('删除简历将同时清除所有经历和技能，确定吗？', '警告', { type: 'warning' })
  await resumeApi.deleteResume()
  ElMessage.success('简历已删除')
  await load()
}

// 子项通用处理
function subHandlers(
  add: (p: any) => Promise<unknown>,
  update: (id: number, p: any) => Promise<unknown>,
  remove: (id: number) => Promise<unknown>,
) {
  return {
    async onAdd(p: any) {
      await add(p)
      ElMessage.success('已添加')
      await load()
    },
    async onEdit(id: number, p: any) {
      await update(id, p)
      ElMessage.success('已保存')
      await load()
    },
    async onRemove(id: number) {
      await ElMessageBox.confirm('确定删除这条记录吗？', '提示', { type: 'warning' })
      await remove(id)
      ElMessage.success('已删除')
      await load()
    },
  }
}

const edu = subHandlers(resumeApi.addEducation, resumeApi.updateEducation, resumeApi.deleteEducation)
const work = subHandlers(resumeApi.addWork, resumeApi.updateWork, resumeApi.deleteWork)
const proj = subHandlers(resumeApi.addProject, resumeApi.updateProject, resumeApi.deleteProject)
const skill = subHandlers(resumeApi.addSkill, resumeApi.updateSkill, resumeApi.deleteSkill)

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="resume">
    <!-- 未创建简历 -->
    <el-card v-if="!loading && !hasResume" shadow="never" class="empty-card">
      <el-empty description="你还没有创建简历">
        <el-button type="primary" @click="openCreate">创建简历</el-button>
      </el-empty>
    </el-card>

    <template v-if="hasResume && data">
      <el-card shadow="never" class="section">
        <template #header>
          <div class="section-head">
            <span class="section-title">简历主体</span>
            <div>
              <el-button text type="primary" @click="openEditMain">编辑</el-button>
              <el-button text type="danger" @click="removeResume">删除简历</el-button>
            </div>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ data.resume.name }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="data.resume.status === 1 ? 'success' : 'info'" size="small">
              {{ data.resume.status === 1 ? '启用' : '隐藏' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">{{ data.resume.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ data.resume.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">{{ data.resume.summary || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <ResumeSubSection
        title="教育经历"
        :items="data.educations"
        :fields="eduFields"
        @add="edu.onAdd"
        @edit="edu.onEdit"
        @remove="edu.onRemove"
      />
      <ResumeSubSection
        title="工作经历"
        :items="data.works"
        :fields="workFields"
        @add="work.onAdd"
        @edit="work.onEdit"
        @remove="work.onRemove"
      />
      <ResumeSubSection
        title="项目经历"
        :items="data.projects"
        :fields="projectFields"
        @add="proj.onAdd"
        @edit="proj.onEdit"
        @remove="proj.onRemove"
      />
      <ResumeSubSection
        title="技能"
        :items="data.skills"
        :fields="skillFields"
        @add="skill.onAdd"
        @edit="skill.onEdit"
        @remove="skill.onRemove"
      />
    </template>

    <el-dialog v-model="mainDialog" :title="hasResume ? '编辑简历' : '创建简历'" width="520px">
      <el-form label-position="top">
        <el-form-item label="姓名" required>
          <el-input v-model="mainForm.name" maxlength="50" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="mainForm.phone" maxlength="20" placeholder="选填" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="mainForm.email" maxlength="100" placeholder="选填" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="mainForm.summary" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="选填" />
        </el-form-item>
        <el-form-item label="简历状态">
          <el-radio-group v-model="mainForm.status">
            <el-radio-button :value="1">启用</el-radio-button>
            <el-radio-button :value="0">隐藏</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mainDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMain">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.section,
.empty-card {
  margin-bottom: 16px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title {
  font-weight: 600;
}
</style>
