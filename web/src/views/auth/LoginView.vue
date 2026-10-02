<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ phone: '', password: '' })

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const token = await login({ phone: form.phone, password: form.password })
    auth.setToken(token)
    const user = await auth.fetchMe()
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || (user.type === 2 ? '/recruiter/positions' : '/jobs')
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <div class="auth-head">
        <span class="logo">招</span>
        <h2>登录智能招聘平台</h2>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password @keyup.enter="onSubmit" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form>
      <div class="auth-foot">
        还没有账号？<router-link to="/register" class="link">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00bebd 0%, #0090ff 100%);
}
.auth-card {
  width: 400px;
  padding: 8px 12px;
}
.auth-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.auth-head h2 {
  font-size: 20px;
  margin: 0;
}
.logo {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #00bebd;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.submit {
  width: 100%;
}
.auth-foot {
  margin-top: 16px;
  text-align: center;
  color: #86909c;
  font-size: 14px;
}
.link {
  color: #00bebd;
}
</style>
