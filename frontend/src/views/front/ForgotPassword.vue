<template>
  <div class="forgot-page">
    <div class="page-card forgot-card">
      <h2 class="forgot-title">重置密码</h2>

      <template v-if="step === 1">
        <p class="forgot-desc">输入注册时使用的邮箱地址，我们将发送验证码。</p>
        <el-form @submit.prevent="handleSendCode">
          <el-form-item>
            <el-input v-model="email" placeholder="请输入邮箱" size="large" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="sending" style="width:100%" @click="handleSendCode">发送验证码</el-button>
        </el-form>
      </template>

      <template v-if="step === 2">
        <p class="forgot-desc">验证码已发送至 <strong>{{ email }}</strong>，请输入验证码和新密码。</p>
        <el-form @submit.prevent="handleReset">
          <el-form-item>
            <el-input v-model="code" placeholder="请输入验证码" size="large" maxlength="6" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="newPassword" type="password" placeholder="请输入新密码" size="large" show-password />
          </el-form-item>
          <el-button type="primary" size="large" :loading="resetting" style="width:100%" @click="handleReset">重置密码</el-button>
        </el-form>
      </template>

      <template v-if="step === 3">
        <el-result icon="success" title="密码重置成功" sub-title="请使用新密码登录">
          <template #extra>
            <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
          </template>
        </el-result>
      </template>

      <div class="forgot-footer">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { forgotPassword, resetPassword } from '@/api/auth'

const step = ref(1)
const email = ref('')
const code = ref('')
const newPassword = ref('')
const sending = ref(false)
const resetting = ref(false)

async function handleSendCode() {
  if (!email.value) return
  sending.value = true
  try {
    await forgotPassword(email.value)
    ElMessage.success('验证码已发送')
    step.value = 2
  } finally {
    sending.value = false
  }
}

async function handleReset() {
  if (!code.value || !newPassword.value) return
  resetting.value = true
  try {
    await resetPassword(email.value, code.value, newPassword.value)
    step.value = 3
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped>
.forgot-page {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.forgot-card {
  width: 420px;
  padding: 40px;
}

.forgot-title {
  margin: 0 0 8px;
  font-size: 24px;
  text-align: center;
}

.forgot-desc {
  color: #6b7280;
  font-size: 14px;
  margin: 0 0 24px;
  text-align: center;
  line-height: 1.6;
}

.forgot-footer {
  margin-top: 24px;
  text-align: center;
}
</style>
