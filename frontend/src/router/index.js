import { createRouter, createWebHistory } from 'vue-router'
import FrontLayout from '@/layouts/FrontLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: FrontLayout,
      children: [
        { path: '', name: 'home', component: () => import('@/views/front/Home.vue') },
        { path: 'notes', name: 'notes', component: () => import('@/views/front/Notes.vue') },
        { path: 'user/:id', name: 'user-space', component: () => import('@/views/front/UserSpace.vue') },
        { path: 'resource/:id', name: 'resource-detail', component: () => import('@/views/front/ResourceDetail.vue') },
        { path: 'publish', name: 'publish', meta: { requiresAuth: true }, component: () => import('@/views/front/Publish.vue') },
        { path: 'search', name: 'search', component: () => import('@/views/front/Search.vue') },
        { path: 'profile', name: 'profile', meta: { requiresAuth: true }, component: () => import('@/views/front/Profile.vue') },
        { path: 'messages', name: 'messages', meta: { requiresAuth: true }, component: () => import('@/views/front/Messages.vue') }
      ]
    },
    { path: '/login', name: 'login', component: () => import('@/views/front/Login.vue') },
{ path: '/forgot-password', name: 'forgot-password', component: () => import('@/views/front/ForgotPassword.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/front/Register.vue') },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', name: 'admin-dashboard', component: () => import('@/views/admin/Dashboard.vue') },
        { path: 'resources', name: 'admin-resources', component: () => import('@/views/admin/ResourceManage.vue') },
        { path: 'reports', name: 'admin-reports', component: () => import('@/views/admin/ReportManage.vue') },
        { path: 'users', name: 'admin-users', component: () => import('@/views/admin/UserManage.vue') },
        { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/CategoryManage.vue') }
      ]
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      userStore.logout()
    }
  }

  if (to.meta.requiresAuth && !userStore.isLogin) {
    return { path: '/', query: { showAuth: 'login' } }
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return '/'
  }

  if ((to.path === '/login' || to.path === '/register') && userStore.isLogin) {
    return '/'
  }

  return true
})

export default router
