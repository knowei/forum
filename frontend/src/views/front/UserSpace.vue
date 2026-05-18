<template>
  <div class="user-page" v-loading="loading">
    <!-- Profile Header -->
    <section class="page-card user-profile">
      <div class="user-profile__bg" />
      <div class="user-profile__inner">
        <div class="user-profile__avatar-wrap">
          <el-avatar v-if="profile?.avatar" :src="profile.avatar" :size="80" class="user-profile__avatar" loading="lazy" />
          <el-avatar v-else :size="80" class="user-profile__avatar">{{ profile?.nickname?.[0] || '?' }}</el-avatar>
          <span v-if="profile?.role === 1" class="user-profile__badge">站长</span>
        </div>
        <h1 class="user-profile__name">{{ profile?.nickname || '未知用户' }}</h1>
        <p class="user-profile__username">@{{ profile?.username }}</p>
        <div class="user-profile__stats">
          <div class="user-stat">
            <span class="user-stat__value">{{ profile?.resourceCount || 0 }}</span>
            <span class="user-stat__label">资源</span>
          </div>
          <div class="user-stat">
            <span class="user-stat__value">{{ profile?.totalViews || 0 }}</span>
            <span class="user-stat__label">总浏览</span>
          </div>
          <div class="user-stat">
            <span class="user-stat__value">{{ formatJoinDate(profile?.createTime) }}</span>
            <span class="user-stat__label">加入</span>
          </div>
        </div>
        <el-button v-if="canSendMessage" size="small" class="msg-btn" @click="sendMessage">发私信</el-button>
      </div>
    </section>

    <!-- User's Resources -->
    <section class="page-card user-resources" style="margin-top: 20px;">
      <h2 class="user-resources__title">发布的资源</h2>

      <div v-if="resources.length" class="resource-grid">
        <router-link
          v-for="item in resources"
          :key="item.id"
          class="resource-card"
          :to="`/resource/${item.id}`"
        >
          <div class="resource-card__cover-wrap">
            <img v-if="item.cover" :src="item.cover" class="resource-card__cover" alt="" loading="lazy" width="800" height="450" />
            <div v-else class="resource-card__cover-placeholder">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
            </div>
          </div>
          <div class="resource-card__body">
            <div class="resource-card__title">{{ item.title }}</div>
            <div class="resource-card__meta">
              <span class="resource-card__views">{{ item.viewCount || 0 }} 次浏览</span>
              <span>{{ item.likeCount || 0 }} 赞</span>
            </div>
          </div>
        </router-link>
      </div>

      <el-empty v-else-if="!loading" description="还没有发布资源" :image-size="50" />

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          background
          layout="prev, pager, next"
          :total="total"
          @current-change="fetchResources"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserProfile } from '@/api/user'
import { getResourceList } from '@/api/resource'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const profile = ref(null)
const resources = ref([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

const canSendMessage = computed(() => {
  return userStore.isLogin && profile.value && profile.value.id !== userStore.userInfo?.id
})

function sendMessage() {
  router.push({ path: '/messages', query: { user: profile.value.id } })
}

async function loadAll() {
  const userId = route.params.id
  if (!userId) return
  loading.value = true
  try {
    const [profileData, resourceData] = await Promise.all([
      getUserProfile(userId),
      getResourceList({ userId, page: page.value, size: pageSize.value })
    ])
    profile.value = profileData
    resources.value = resourceData.list
    total.value = resourceData.total
  } catch {
    profile.value = null
    resources.value = []
  } finally {
    loading.value = false
  }
}

async function fetchResources() {
  try {
    const data = await getResourceList({ userId: route.params.id, page: page.value, size: pageSize.value })
    resources.value = data.list
    total.value = data.total
  } catch {
    resources.value = []
  }
}

function formatJoinDate(value) {
  if (!value) return '-'
  return new Date(value).getFullYear()
}

watch(() => route.params.id, loadAll)
onMounted(loadAll)
</script>

<style scoped>
.user-page {
  max-width: 900px;
  margin: 0 auto;
}

.user-profile {
  position: relative;
  overflow: hidden;
  padding: 0;
}
.user-profile__bg {
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.user-profile__inner {
  padding: 0 24px 24px;
  margin-top: -40px;
  text-align: center;
}
.user-profile__avatar-wrap {
  position: relative;
  display: inline-block;
}
.user-profile__avatar {
  border: 4px solid var(--bg-card);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.user-profile__badge {
  position: absolute;
  bottom: 0;
  right: -4px;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
  border: 2px solid var(--bg-card);
}
.user-profile__name {
  margin: 12px 0 2px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}
.user-profile__username {
  margin: 0 0 16px;
  font-size: 14px;
  color: var(--text-muted);
}
.user-profile__stats {
  display: flex;
  justify-content: center;
  gap: 32px;
}

.msg-btn {
  margin-top: 12px;
}
.user-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.user-stat__value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}
.user-stat__label {
  font-size: 12px;
  color: var(--text-muted);
}

.user-resources__title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
