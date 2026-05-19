<template>
  <div class="front-layout">
    <header class="site-header" :class="{ 'header-hidden': headerHidden }">
      <div class="container header-inner">
        <button class="hamburger" aria-label="导航菜单" @click="drawerVisible = true">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <RouterLink class="logo" to="/">柠檬网</RouterLink>
        <nav class="nav-links">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/notes">随手记</RouterLink>
          <button v-if="!userStore.isLogin" class="nav-btn" @click="openAuth(true)">发布资源</button>
          <RouterLink v-else to="/publish">发布资源</RouterLink>
          <button v-if="!userStore.isLogin" class="nav-btn" @click="openAuth(true)">个人中心</button>
          <RouterLink v-else to="/profile">个人中心</RouterLink>
          <RouterLink v-if="userStore.isLogin" to="/messages">
            私信
            <span v-if="msgUnreadCount" class="msg-badge">{{ msgUnreadCount > 99 ? '99+' : msgUnreadCount }}</span>
          </RouterLink>
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
          <button class="theme-toggle" @click="themeStore.toggle" :aria-label="themeStore.isDark ? '切换亮色模式' : '切换暗色模式'">
            <svg v-if="themeStore.isDark" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
          </button>
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
            <el-button link type="primary" class="link-logout" @click="handleLogout">退出</el-button>
          </template>
          <template v-else>
            <el-button link type="primary" class="link-login" @click="openAuth(true)">登录</el-button>
            <el-button link type="primary" class="link-register" @click="openAuth(false)">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- Mobile Drawer -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="280px" title="导航" :with-header="true">
      <div class="mobile-drawer__body">
        <div class="mobile-drawer__search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索…"
            clearable
            @keyup.enter="doDrawerSearch"
          >
            <template #prefix>
              <el-icon aria-hidden="true"><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <nav class="mobile-drawer__nav">
          <RouterLink class="mobile-nav-item" to="/" @click="drawerVisible = false">首页</RouterLink>
          <RouterLink class="mobile-nav-item" to="/notes" @click="drawerVisible = false">随手记</RouterLink>
          <button v-if="!userStore.isLogin" class="mobile-nav-item mobile-nav-item--btn" @click="closeDrawerAndOpenAuth(true)">发布资源</button>
          <RouterLink v-else class="mobile-nav-item" to="/publish" @click="drawerVisible = false">发布资源</RouterLink>
          <button v-if="!userStore.isLogin" class="mobile-nav-item mobile-nav-item--btn" @click="closeDrawerAndOpenAuth(true)">个人中心</button>
          <RouterLink v-else class="mobile-nav-item" to="/profile" @click="drawerVisible = false">个人中心</RouterLink>
          <RouterLink v-if="userStore.isLogin" class="mobile-nav-item" to="/messages" @click="drawerVisible = false">私信</RouterLink>
          <RouterLink v-if="userStore.isAdmin" class="mobile-nav-item" to="/admin" @click="drawerVisible = false">后台管理</RouterLink>
        </nav>
        <div class="mobile-drawer__footer">
          <template v-if="userStore.isLogin">
            <div class="mobile-drawer__user">
              <el-avatar v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" :size="36" />
              <el-avatar v-else :size="36">{{ userStore.userInfo?.nickname?.[0] }}</el-avatar>
              <span>{{ userStore.userInfo?.nickname }}</span>
            </div>
            <el-button class="mobile-drawer__logout" @click="handleLogoutMobile">退出登录</el-button>
          </template>
          <template v-else>
            <div class="mobile-drawer__auth">
              <el-button type="primary" @click="closeDrawerAndOpenAuth(true)">登录</el-button>
              <el-button @click="closeDrawerAndOpenAuth(false)">注册</el-button>
            </div>
          </template>
        </div>
      </div>
    </el-drawer>
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
import { useThemeStore } from '@/stores/theme'
import { getNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead } from '@/api/user'
import AuthDialog from '@/components/common/AuthDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const searchKeyword = ref('')
const showAuth = ref(false)
const authLoginMode = ref(true)
const headerHidden = ref(false)
const drawerVisible = ref(false)
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

function closeDrawerAndOpenAuth(loginMode) {
  drawerVisible.value = false
  openAuth(loginMode)
}

function doDrawerSearch() {
  drawerVisible.value = false
  handleSearch()
}

async function handleLogoutMobile() {
  drawerVisible.value = false
  await handleLogout()
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

const msgUnreadCount = ref(0)

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
  window.addEventListener('msg-unread', (e) => {
    msgUnreadCount.value = e.detail
  })
})

onUnmounted(() => {
  if (notifInterval) {
    clearInterval(notifInterval)
  }
})
</script>

<style scoped>
/* ---- iOS Search Bar ---- */
.header-search {
  flex: 0 0 400px;
}

.header-search .el-input {
  --el-input-height: 36px;
  --el-input-border-radius: 10px;
  --el-input-border-color: transparent;
  --el-input-hover-border-color: transparent;
  --el-input-focus-border-color: var(--tint);
  --el-input-bg-color: var(--bg-fill);
  --el-input-hover-bg-color: var(--bg-fill);
  --el-input-focus-bg-color: var(--bg-card);
  --el-input-text-color: var(--text-primary);
  --el-input-placeholder-color: var(--text-muted);
  transition: background-color 0.2s, border-color 0.2s;
}

.header-search .el-input :deep(.el-input__wrapper) {
  padding-right: 4px;
  box-shadow: none !important;
}

.search-btn {
  height: 28px;
  border-radius: 7px;
  font-size: 12px;
}

/* ---- Header Actions ---- */
.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
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
  color: var(--tint);
}

.theme-toggle {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border: none; border-radius: 8px;
  background: transparent; color: var(--text-secondary);
  cursor: pointer; transition: all 0.2s;
}
.theme-toggle:hover {
  background: var(--bg-hover);
  color: var(--tint);
}

/* ---- Notifications (iOS style) ---- */
.notif-badge {
  margin-right: 2px;
}

.notif-btn {
  border: none;
  background: transparent;
  color: var(--text-secondary);
  width: 32px;
  height: 32px;
}

.notif-btn:hover {
  background: var(--bg-hover);
  color: var(--tint);
}

.notif-popover__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 10px;
  border-bottom: 0.5px solid var(--border-light);
}

.notif-popover__title {
  font-weight: 700;
  font-size: 16px;
}

.notif-list {
  display: flex;
  flex-direction: column;
  max-height: 360px;
  overflow-y: auto;
}

.notif-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  cursor: pointer;
  border-bottom: 0.5px solid var(--border-light);
  transition: background 0.15s;
  border-radius: 8px;
  margin: 0 -12px;
}

.notif-item:last-child {
  border-bottom: none;
}

.notif-item:hover {
  background: var(--bg-hover);
}

.notif-item--unread {
  background: var(--tint-light);
}

.notif-item--unread:hover {
  background: var(--tint-mid);
}

.notif-item__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--tint);
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
  color: var(--text-primary);
}

.notif-item__content {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.notif-item__time {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.msg-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: var(--red);
  border-radius: 8px;
  margin-left: 2px;
}

/* ---- Hamburger (iOS) ---- */
.hamburger {
  display: none;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}

.hamburger:hover {
  background: var(--bg-hover);
  color: var(--tint);
}

/* ---- Mobile Drawer (iOS style) ---- */
.mobile-drawer__body {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
  padding: 8px 0;
}

.mobile-drawer__search {
  flex-shrink: 0;
  padding: 0 12px;
}

.mobile-drawer__search .el-input {
  --el-input-border-radius: 10px;
}

.mobile-drawer__nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 12px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  transition: background 0.15s;
}

.mobile-nav-item:hover {
  background: var(--bg-hover);
  color: var(--tint);
}

.mobile-nav-item--btn {
  background: none;
  border: none;
  width: 100%;
  text-align: left;
  font-family: inherit;
  cursor: pointer;
}

.mobile-drawer__footer {
  flex-shrink: 0;
  padding: 16px 12px 0;
  border-top: 0.5px solid var(--border-light);
}

.mobile-drawer__user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.mobile-drawer__logout {
  width: 100%;
  --el-button-border-radius: 12px;
}

.mobile-drawer__auth {
  display: flex;
  gap: 12px;
}

.mobile-drawer__auth .el-button {
  flex: 1;
  --el-button-border-radius: 12px;
}

/* ---- iOS 26 Header Actions (login/register pills) ---- */
.header-actions :deep(.el-button.link-login),
.header-actions :deep(.el-button.link-register) {
  font-size: 14px;
  font-weight: 500;
}

.header-actions :deep(.el-button.link-login) {
  color: var(--tint);
}

.header-actions :deep(.el-button.link-register) {
  color: var(--tint);
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .hamburger {
    display: flex;
  }

  .nav-links {
    display: none;
  }

  .header-search {
    display: none;
  }

  .header-inner {
    gap: 8px;
  }

  .header-actions .nickname {
    display: none;
  }

  .header-actions .el-button.link-logout {
    display: none;
  }

  .header-actions .el-button.link-login,
  .header-actions .el-button.link-register {
    font-size: 13px;
    padding: 4px 6px;
  }

  .header-actions {
    gap: 2px;
  }
}
</style>

