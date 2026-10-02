<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { register, sendCode } from '@/api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)

const form = reactive({
  phone: '',
  code: '',
  password: '',
  nickname: '',
  type: 1,
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

async function onSendCode() {
  if (!/^1\d{10}$/.test(form.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  sending.value = true
  try {
    await sendCode(form.phone)
    ElMessage.success('验证码已发送，请查看 auth-service 控制台获取 6 位验证码')
    countdown.value = 60
    const timer = window.setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) window.clearInterval(timer)
    }, 1000)
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register({
      phone: form.phone,
      password: form.password,
      nickname: form.nickname || undefined,
      type: form.type,
      code: form.code,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
        <h2>注册账号</h2>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent>
        <el-form-item label="身份">
          <el-radio-group v-model="form.type">
            <el-radio-button :value="1">求职者</el-radio-button>
            <el-radio-button :value="2">招聘方</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="6 位验证码" maxlength="6" />
            <el-button :loading="sending" :disabled="countdown > 0" @click="onSendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少 6 位" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="选填" maxlength="30" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">注册</el-button>
      </el-form>
      <div class="auth-foot">
        已有账号？<router-link to="/login" class="link">去登录</router-link>
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
  padding: 24px 0;
}
.auth-card {
  width: 420px;
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
.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
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
