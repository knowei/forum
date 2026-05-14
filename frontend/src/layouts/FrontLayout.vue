<template>
  <div class="front-layout">
    <header class="site-header" :class="{ 'header-hidden': headerHidden }">
      <div class="container header-inner">
        <RouterLink class="logo" to="/">资源论坛</RouterLink>
        <nav class="nav-links">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/notes">随手记</RouterLink>
          <button v-if="!userStore.isLogin" class="nav-btn" @click="openAuth(true)">发布资源</button>
          <RouterLink v-else to="/publish">发布资源</RouterLink>
          <button v-if="!userStore.isLogin" class="nav-btn" @click="openAuth(true)">个人中心</button>
          <RouterLink v-else to="/profile">个人中心</RouterLink>
          <RouterLink v-if="userStore.isAdmin" to="/admin">后台管理</RouterLink>
        </nav>
        <div class="header-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索你想要的资源…"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon aria-hidden="true"><Search /></el-icon>
            </template>
            <template #suffix>
              <el-button class="search-btn" type="primary" size="default" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="header-actions">
          <template v-if="userStore.isLogin">
            <el-popover placement="bottom-end" :width="360" trigger="click" @show="fetchNotifications">
              <template #reference>
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notif-badge">
                  <el-button class="notif-btn" circle>
                    <el-icon :size="18" aria-hidden="true"><Bell /></el-icon>
                  </el-button>
                </el-badge>
              </template>
              <div class="notif-popover">
                <div class="notif-popover__header">
                  <span class="notif-popover__title">通知</span>
                  <el-button v-if="notifications.length" link type="primary" size="small" @click="handleMarkAllRead">全部标为已读</el-button>
                </div>
                <div v-if="notifications.length" class="notif-list">
                  <div
                    v-for="n in notifications"
                    :key="n.id"
                    class="notif-item"
                    :class="{ 'notif-item--unread': !n.read }"
                    @click="handleClickNotification(n)"
                  >
                    <div class="notif-item__dot" v-if="!n.read" />
                    <div class="notif-item__body">
                      <div class="notif-item__title">{{ n.title }}</div>
                      <div class="notif-item__content">{{ n.content }}</div>
                      <div class="notif-item__time">{{ formatTime(n.createTime) }}</div>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无通知" :image-size="40" />
              </div>
            </el-popover>
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
      <router-view v-slot="{ Component, route }">
        <Transition name="fade-slide" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </router-view>
    </main>

    <AuthDialog v-if="showAuth" v-model:visible="showAuth" :login-mode="authLoginMode" @success="onAuthSuccess" />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead } from '@/api/user'
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
  fetchUnreadCount()
}

// ---- Notifications ----
const notifications = ref([])
const unreadCount = ref(0)
let notifInterval = null

async function fetchNotifications() {
  try {
    notifications.value = await getNotifications()
  } catch { /* ignore */ }
}

async function fetchUnreadCount() {
  try {
    const { count } = await getUnreadCount()
    unreadCount.value = count
  } catch { /* ignore */ }
}

async function handleClickNotification(n) {
  if (!n.read) {
    try {
      await markNotificationRead(n.id)
      n.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch { /* ignore */ }
  }
  if (n.relatedId) {
    router.push(`/resource/${n.relatedId}`)
  }
}

async function handleMarkAllRead() {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach((n) => { n.read = true })
    unreadCount.value = 0
  } catch { /* ignore */ }
}

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return value.replace('T', ' ').slice(0, 16)
}

watch(() => userStore.isLogin, (loggedIn) => {
  if (loggedIn) {
    fetchUnreadCount()
    notifInterval = setInterval(fetchUnreadCount, 30000)
  } else {
    notifications.value = []
    unreadCount.value = 0
    if (notifInterval) {
      clearInterval(notifInterval)
      notifInterval = null
    }
  }
})

onMounted(() => {
  if (userStore.isLogin) {
    fetchUnreadCount()
    notifInterval = setInterval(fetchUnreadCount, 30000)
  }
})

onUnmounted(() => {
  if (notifInterval) {
    clearInterval(notifInterval)
  }
})
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
  transition: background-color 0.2s, border-color 0.2s;
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

.nav-btn {
  background: none;
  border: none;
  padding: 0;
  font: inherit;
  color: inherit;
  cursor: pointer;
  font-size: 14px;
  line-height: inherit;
}

.nav-btn:hover {
  color: #409eff;
}

/* ---- Notifications ---- */
.notif-badge {
  margin-right: 4px;
}

.notif-btn {
  border: none;
  background: transparent;
  color: #6b7280;
  width: 32px;
  height: 32px;
}

.notif-btn:hover {
  background: #f3f4f6;
  color: #409eff;
}

.notif-popover__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.notif-popover__title {
  font-weight: 600;
  font-size: 15px;
}

.notif-list {
  display: flex;
  flex-direction: column;
  max-height: 360px;
  overflow-y: auto;
}

.notif-item {
  display: flex;
  gap: 8px;
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px solid #f9fafb;
  transition: background 0.15s;
}

.notif-item:hover {
  background: #f9fafb;
  margin: 0 -12px;
  padding: 10px 12px;
  border-radius: 6px;
}

.notif-item--unread {
  background: #f0f7ff;
  margin: 0 -12px;
  padding: 10px 12px;
  border-radius: 6px;
}

.notif-item--unread:hover {
  background: #e6f0fa;
}

.notif-item__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
  margin-top: 6px;
}

.notif-item__body {
  flex: 1;
  min-width: 0;
}

.notif-item__title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.notif-item__content {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.notif-item__time {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
