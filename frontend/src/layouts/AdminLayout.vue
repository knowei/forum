<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="admin-sidebar__header">
        <RouterLink to="/" class="admin-logo">柠檬网</RouterLink>
        <div class="admin-sidebar__title">管理后台</div>
      </div>
      <nav class="admin-sidebar__nav">
        <RouterLink to="/admin" class="admin-nav-item" :class="{ active: $route.path === '/admin' }">
          <el-icon aria-hidden="true"><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </RouterLink>
        <RouterLink to="/admin/resources" class="admin-nav-item" :class="{ active: $route.path === '/admin/resources' }">
          <el-icon aria-hidden="true"><List /></el-icon>
          <span>资源审核</span>
        </RouterLink>
        <RouterLink to="/admin/reports" class="admin-nav-item" :class="{ active: $route.path === '/admin/reports' }">
          <el-icon aria-hidden="true"><Warning /></el-icon>
          <span>资源举报</span>
        </RouterLink>
        <RouterLink to="/admin/users" class="admin-nav-item" :class="{ active: $route.path === '/admin/users' }">
          <el-icon aria-hidden="true"><User /></el-icon>
          <span>用户管理</span>
        </RouterLink>
        <RouterLink to="/admin/categories" class="admin-nav-item" :class="{ active: $route.path === '/admin/categories' }">
          <el-icon aria-hidden="true"><CollectionTag /></el-icon>
          <span>分类管理</span>
        </RouterLink>
      </nav>
      <div class="admin-sidebar__footer">
        <RouterLink to="/" class="admin-nav-item">
          <el-icon aria-hidden="true"><Switch /></el-icon>
          <span>返回前台</span>
        </RouterLink>
      </div>
    </aside>
    <div class="admin-main">
      <header class="admin-header">
        <div class="admin-header__left">
          <button class="admin-hamburger" aria-label="导航菜单" @click="drawerVisible = true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
          </button>
          <el-breadcrumb separator=">">
            <el-breadcrumb-item to="/admin">管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="admin-header__right">
          <el-avatar v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" :size="32" />
          <el-avatar v-else :size="32">{{ userStore.userInfo?.nickname?.[0] }}</el-avatar>
          <span class="admin-header__user">{{ userStore.userInfo?.nickname }}</span>
          <el-button text type="primary" @click="handleLogout">退出</el-button>
        </div>
      </header>

      <!-- Mobile Drawer -->
      <el-drawer v-model="drawerVisible" direction="ltr" size="260px" title="管理后台">
        <nav class="admin-drawer__nav">
          <RouterLink to="/admin" class="admin-nav-item" :class="{ active: $route.path === '/admin' }" @click="drawerVisible = false">
            <el-icon aria-hidden="true"><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </RouterLink>
          <RouterLink to="/admin/resources" class="admin-nav-item" :class="{ active: $route.path === '/admin/resources' }" @click="drawerVisible = false">
            <el-icon aria-hidden="true"><List /></el-icon>
            <span>资源审核</span>
          </RouterLink>
          <RouterLink to="/admin/reports" class="admin-nav-item" :class="{ active: $route.path === '/admin/reports' }" @click="drawerVisible = false">
            <el-icon aria-hidden="true"><Warning /></el-icon>
            <span>资源举报</span>
          </RouterLink>
          <RouterLink to="/admin/users" class="admin-nav-item" :class="{ active: $route.path === '/admin/users' }" @click="drawerVisible = false">
            <el-icon aria-hidden="true"><User /></el-icon>
            <span>用户管理</span>
          </RouterLink>
          <RouterLink to="/admin/categories" class="admin-nav-item" :class="{ active: $route.path === '/admin/categories' }" @click="drawerVisible = false">
            <el-icon aria-hidden="true"><CollectionTag /></el-icon>
            <span>分类管理</span>
          </RouterLink>
          <div class="admin-drawer__divider" />
          <RouterLink to="/" class="admin-nav-item" @click="drawerVisible = false">
            <el-icon aria-hidden="true"><Switch /></el-icon>
            <span>返回前台</span>
          </RouterLink>
        </nav>
      </el-drawer>
      <section class="admin-content">
        <router-view />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CollectionTag, DataAnalysis, List, Switch, User, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const drawerVisible = ref(false)

const breadcrumbTitle = computed(() => {
  if (route.path === '/admin') return '数据概览'
  if (route.path === '/admin/resources') return '资源审核'
  if (route.path === '/admin/reports') return '资源举报'
  if (route.path === '/admin/users') return '用户管理'
  if (route.path === '/admin/categories') return '分类管理'
  return ''
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-sidebar {
  display: flex;
  flex-direction: column;
  padding: 0;
  background: #1e293b;
  color: #fff;
}

.admin-sidebar__header {
  padding: 24px 20px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-logo {
  display: block;
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 4px;
}

.admin-sidebar__title {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

.admin-sidebar__nav {
  flex: 1;
  padding: 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.admin-sidebar__footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.65);
  font-size: 14px;
  transition: background-color 0.15s, color 0.15s;
}

.admin-nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.admin-nav-item.active {
  background: rgba(96, 165, 250, 0.15);
  color: #60a5fa;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: var(--bg-header);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid var(--border-light);
}

.admin-header__left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-header__right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-header__user {
  font-size: 14px;
  color: var(--text-primary);
}

/* ---- Admin Hamburger ---- */
.admin-hamburger {
  display: none;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}

.admin-hamburger:hover {
  background: var(--bg-hover);
  color: var(--tint);
}

/* ---- Admin Drawer Nav ---- */
.admin-drawer__nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 0;
}

.admin-drawer__divider {
  height: 0.5px;
  background: var(--border-light);
  margin: 8px 12px;
}

@media (max-width: 768px) {
  .admin-hamburger {
    display: flex;
  }

  .admin-sidebar {
    display: none;
  }

  .admin-header__user {
    display: none;
  }
}
</style>
