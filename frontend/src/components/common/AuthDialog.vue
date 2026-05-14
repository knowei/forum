<template>
  <el-dialog v-model="visible_" width="400px" top="18vh" :close-on-click-modal="false" destroy-on-close class="auth-dialog">
    <div class="auth-dialog__body">
      <h1 class="auth-dialog__brand">资源论坛</h1>

      <el-form v-if="isLoginMode" :model="loginForm" @submit.prevent="handleLogin" class="auth-form">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="用户名" class="auth-input" autocomplete="username" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" type="password" show-password placeholder="密码" class="auth-input" autocomplete="current-password" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住登录</el-checkbox>
        </el-form-item>
        <el-button type="primary" :loading="loginLoading" class="auth-btn" @click="handleLogin">登录</el-button>
      </el-form>

      <el-form v-else :model="registerForm" @submit.prevent="handleRegister" class="auth-form">
        <el-form-item>
          <el-input v-model="registerForm.username" placeholder="用户名" class="auth-input" autocomplete="username" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="registerForm.nickname" placeholder="昵称" class="auth-input" autocomplete="off" />
        </el-form-item>
        <el-form-item>
          <div class="auth-email-row">
            <el-input v-model="registerForm.email" placeholder="邮箱" class="auth-input auth-email-input" autocomplete="email" />
            <el-button :disabled="sendCodeDisabled || !registerForm.email" :loading="sendingCode" class="auth-code-btn" @click="handleSendCode">
              {{ sendCodeText }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-input v-model="registerForm.code" placeholder="验证码" class="auth-input" autocomplete="one-time-code" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="registerForm.password" type="password" show-password placeholder="密码" class="auth-input" autocomplete="new-password" />
        </el-form-item>
        <el-button type="primary" :loading="registerLoading" class="auth-btn" @click="handleRegister">注册</el-button>
      </el-form>

      <div class="auth-dialog__footer">
        <template v-if="isLoginMode">
          还没有账号？
          <el-button link type="primary" @click="switchToRegister">去注册</el-button>
        </template>
        <template v-else>
          已有账号？
          <el-button link type="primary" @click="switchToLogin">去登录</el-button>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { sendVerifyCode } from '@/api/auth'
import { setRememberedUser, getRememberedUser, clearRememberedUser } from '@/utils/auth'

const props = defineProps({
  visible: { type: Boolean, default: false },
  loginMode: { type: Boolean, default: true }
})

const emit = defineEmits(['update:visible', 'success'])

const userStore = useUserStore()
const isLoginMode = ref(props.loginMode)
const loginLoading = ref(false)
const registerLoading = ref(false)
const rememberMe = ref(false)
const sendingCode = ref(false)
const sendCodeDisabled = ref(false)
const countdown = ref(0)
let countdownTimer = null
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', nickname: '', email: '', code: '', password: '' })

const visible_ = ref(props.visible)
watch(() => props.visible, (v) => {
  visible_.value = v
  if (v) {
    isLoginMode.value = props.loginMode
    // Restore remembered user
    const remembered = getRememberedUser()
    if (remembered) {
      loginForm.username = remembered.username
      loginForm.password = remembered.password
      rememberMe.value = true
    } else {
      loginForm.username = ''
      loginForm.password = ''
      rememberMe.value = false
    }
  }
})
watch(visible_, (v) => { emit('update:visible', v) })

function switchToRegister() {
  isLoginMode.value = false
}

function switchToLogin() {
  isLoginMode.value = true
}

const sendCodeText = computed(() => {
  if (sendingCode.value) return '发送中'
  if (countdown.value > 0) return countdown.value + 's'
  return '发送验证码'
})

async function handleSendCode() {
  const email = registerForm.email.trim()
  if (!email || !/^[\w.-]+@[\w.-]+\.\w{2,}$/.test(email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }
  sendingCode.value = true
  try {
    await sendVerifyCode(email)
    ElMessage.success('验证码已发送')
    sendCodeDisabled.value = true
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        countdownTimer = null
        sendCodeDisabled.value = false
      }
    }, 1000)
  } catch {
    // Error message handled by interceptor
  } finally {
    sendingCode.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

async function handleLogin() {
  loginLoading.value = true
  try {
    await userStore.login(loginForm)
    if (rememberMe.value) {
      setRememberedUser({ username: loginForm.username, password: loginForm.password })
    } else {
      clearRememberedUser()
    }
    ElMessage.success('登录成功')
    visible_.value = false
    emit('success')
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  registerLoading.value = true
  try {
    await userStore.register(registerForm)
    ElMessage.success('注册成功，请登录')
    isLoginMode.value = true
    registerForm.username = ''
    registerForm.nickname = ''
    registerForm.email = ''
    registerForm.password = ''
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.auth-dialog :deep(.el-dialog__header) {
  display: none;
}

.auth-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.auth-dialog :deep(.el-dialog) {
  overscroll-behavior: contain;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.auth-dialog__body {
  padding: 28px 32px 24px;
}

.auth-dialog__brand {
  text-align: center;
  font-size: 26px;
  font-weight: 700;
  color: #409eff;
  margin: 0 0 24px;
}

.auth-form .el-form-item {
  margin-bottom: 18px;
}

.auth-input :deep(.el-input__wrapper) {
  box-shadow: none;
  border: none;
  border-radius: 0;
  border-bottom: 1px solid #dcdfe6;
  padding-left: 0;
  padding-right: 0;
  background: transparent;
}

.auth-input :deep(.el-input__wrapper:hover) {
  border-bottom-color: #409eff;
}

.auth-input :deep(.el-input__wrapper.is-focus) {
  border-bottom-color: #409eff;
  box-shadow: none;
}

.auth-input :deep(.el-input__inner) {
  padding-left: 0;
}

.auth-email-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.auth-email-input {
  flex: 1;
}

.auth-code-btn {
  flex-shrink: 0;
  height: 40px;
  border-radius: 8px;
}

.auth-btn {
  width: 100%;
  height: 44px;
  border-radius: 22px;
  font-size: 16px;
}

.auth-dialog__footer {
  margin-top: 20px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}
</style>
