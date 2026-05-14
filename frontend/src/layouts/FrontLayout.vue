<template>
  <div class="front-layout">
    <header class="site-header" :class="{ 'header-hidden': headerHidden }">
      <div class="container header-inner">
        <RouterLink class="logo" to="/">资源论坛</RouterLink>
        <nav class="nav-links">
          <RouterLink to="/">首页</RouterLink>
          <a v-if="!userStore.isLogin" href="javascript:void(0)" @click="openAuth(true)">发布资源</a>
          <RouterLink v-else to="/publish">发布资源</RouterLink>
          <a v-if="!userStore.isLogin" href="javascript:void(0)" @click="openAuth(true)">个人中心</a>
          <RouterLink v-else to="/profile">个人中心</RouterLink>
          <RouterLink v-if="userStore.isAdmin" to="/admin">后台管理</RouterLink>
        </nav>
        <div class="header-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索你想要的资源..."
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #suffix>
              <el-button class="search-btn" type="primary" size="default" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="header-actions">
          <template v-if="userStore.isLogin">
            <el-avatar v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" :size="32" />
            <el-avatar v-else :size="32">{{ userStore.userInfo?.nickname?.[0] }}</el-avatar>
            <span class="nickname">{{ userStore.userInfo?.nickname }}</span>
            <el-button link type="primary" @click="handleLogout">退出</el-button>
          </template>
          <template v-else>
            <el-button link type="primary" @click="openAuth(true)">登录</el-button>
            <el-button link type="primary" @click="openAuth(false)">注册</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="container content-area">
      <router-view />
    </main>

    <AuthDialog v-if="showAuth" v-model:visible="showAuth" :login-mode="authLoginMode" @success="onAuthSuccess" />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AuthDialog from '@/components/common/AuthDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')
const showAuth = ref(false)
const authLoginMode = ref(true)
const headerHidden = ref(false)
let lastScrollY = 0

function handleScroll() {
  const scrollY = window.scrollY
  if (scrollY <= 0) {
    headerHidden.value = false
  } else if (scrollY > lastScrollY) {
    headerHidden.value = true
  } else {
    headerHidden.value = false
  }
  lastScrollY = scrollY
}

onMounted(() => window.addEventListener('scroll', handleScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

watch(() => route.query.showAuth, (val) => {
  if (val === 'login') {
    openAuth(true)
    router.replace({ query: {} })
  }
})

function openAuth(loginMode) {
  authLoginMode.value = loginMode
  showAuth.value = true
}

function handleSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  router.push({ name: 'search', query: { keyword } })
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'info'
    })
    userStore.logout()
    router.push('/')
  } catch {
    // cancelled
  }
}

function onAuthSuccess() {
  // Refresh store data after login
}
</script>

<style scoped>
.header-search {
  flex: 0 0 400px;
}

.header-search .el-input {
  --el-input-height: 40px;
  --el-input-border-radius: 8px;
  --el-input-border-color: #e5e7eb;
  --el-input-hover-border-color: #409eff;
  --el-input-focus-border-color: #409eff;
  --el-input-bg-color: #f3f4f6;
  --el-input-hover-bg-color: #fff;
  --el-input-focus-bg-color: #fff;
  --el-input-text-color: #1f2937;
  --el-input-placeholder-color: #9ca3af;
  transition: all 0.2s;
}

.header-search .el-input:hover {
  --el-input-bg-color: #fff;
}

.header-search .el-input :deep(.el-input__wrapper) {
  padding-right: 4px;
}

.search-btn {
  height: 32px;
  border-radius: 6px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
