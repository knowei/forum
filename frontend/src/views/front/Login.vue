<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">登录</h1>
      <el-form :model="form" @submit.prevent="handleSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password placeholder="密码" autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="handleSubmit">登录</el-button>
        <div style="text-align:right;margin-top:8px">
          <router-link to="/forgot-password" style="font-size:13px;color:#9ca3af">忘记密码？</router-link>
        </div>
      </el-form>
      <div class="auth-footer">
        还没有账号？<router-link to="/register">去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleSubmit() {
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>
