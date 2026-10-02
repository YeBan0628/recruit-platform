<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createPosition, updatePosition, getPosition } from '@/api/position'
import type { PositionForm } from '@/types'

const route = useRoute()
const router = useRouter()
const idParam = route.params.id as string | undefined
const isEdit = computed(() => !!idParam)
const formRef = ref<FormInstance>()
const loading = ref(false)
const fetching = ref(false)

const form = reactive<PositionForm>({
  title: '',
  city: '',
  salaryMin: undefined,
  salaryMax: undefined,
  experience: '',
  education: '',
  description: '',
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  salaryMax: [
    {
      validator: (_r, value, cb) => {
        if (value != null && form.salaryMin != null && value < form.salaryMin) {
          cb(new Error('最高月薪不能低于最低月薪'))
        } else cb()
      },
      trigger: 'blur',
    },
  ],
}

async function loadPosition() {
  if (!isEdit.value) return
  fetching.value = true
  try {
    const p = await getPosition(Number(idParam))
    Object.assign(form, {
      title: p.title,
      city: p.city ?? '',
      salaryMin: p.salaryMin,
      salaryMax: p.salaryMax,
      experience: p.experience ?? '',
      education: p.education ?? '',
      description: p.description ?? '',
    })
  } finally {
    fetching.value = false
  }
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const payload: PositionForm = {
      title: form.title,
      city: form.city || undefined,
      salaryMin: form.salaryMin,
      salaryMax: form.salaryMax,
      experience: form.experience || undefined,
      education: form.education || undefined,
      description: form.description || undefined,
    }
    if (isEdit.value) {
      await updatePosition(Number(idParam), payload)
      ElMessage.success('职位已更新')
    } else {
      await createPosition(payload)
      ElMessage.success('职位已发布')
    }
    router.push('/recruiter/positions')
  } finally {
    loading.value = false
  }
}

onMounted(loadPosition)
</script>

<template>
  <div v-loading="fetching" class="edit">
    <el-button text :icon="ArrowLeft" @click="router.back()">返回</el-button>
    <el-card shadow="never" class="card">
      <template #header><span class="title">{{ isEdit ? '编辑职位' : '发布职位' }}</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 640px">
        <el-form-item label="职位名称" prop="title">
          <el-input v-model="form.title" maxlength="50" placeholder="如：Java 后端开发工程师" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" maxlength="30" placeholder="如：杭州" />
        </el-form-item>
        <el-form-item label="月薪范围">
          <div class="salary-row">
            <el-input-number v-model="form.salaryMin" :min="0" :max="1000" controls-position="right" placeholder="最低" />
            <span class="unit">K —</span>
            <el-input-number v-model="form.salaryMax" :min="0" :max="1000" controls-position="right" placeholder="最高" />
            <span class="unit">K</span>
          </div>
        </el-form-item>
        <el-form-item label="经验要求">
          <el-input v-model="form.experience" maxlength="20" placeholder="如：应届生 / 3-5年" />
        </el-form-item>
        <el-form-item label="学历要求">
          <el-input v-model="form.education" maxlength="20" placeholder="如：本科" />
        </el-form-item>
        <el-form-item label="职位描述">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="岗位职责、任职要求等" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">{{ isEdit ? '保存修改' : '发布' }}</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
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
.salary-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.unit {
  color: #86909c;
}
</style>
