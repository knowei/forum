<template>
  <div class="search-page">
    <div class="search-header">
      <h2>搜索结果</h2>
      <p v-if="keyword" class="search-summary">关键词 "<strong>{{ keyword }}</strong>"</p>
    </div>

    <el-tabs v-model="activeTab" class="search-tabs">
      <el-tab-pane label="资源" name="resources">
        <div class="resource-grid" v-loading="resourceLoading">
          <router-link
            v-for="item in resources"
            :key="item.id"
            class="resource-card"
            :to="`/resource/${item.id}`"
          >
            <div class="resource-card__cover-wrap">
              <img v-if="item.cover" :src="item.cover" class="resource-card__cover" alt="" loading="lazy" width="800" height="450" />
              <div v-else class="resource-card__cover-placeholder">
                <el-icon :size="28" aria-hidden="true"><Picture /></el-icon>
              </div>
            </div>
            <div class="resource-card__body">
              <div class="resource-card__title">{{ item.title }}</div>
              <div v-if="parseTags(item.tags).length" class="resource-card__tags">
                <el-tag v-for="tag in parseTags(item.tags).slice(0, 2)" :key="tag" size="small" effect="plain" round>{{ tag }}</el-tag>
              </div>
              <div class="resource-card__meta">
                <span class="resource-card__author">{{ item.authorNickname || '匿名' }}</span>
                <span class="resource-card__views">{{ item.viewCount || 0 }} 次浏览</span>
              </div>
            </div>
          </router-link>
        </div>
        <el-empty v-if="!resourceLoading && !resources.length" description="未找到相关资源" />
        <div v-if="total > query.size" class="pagination-wrap">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            background
            layout="prev, pager, next"
            :total="total"
            @current-change="fetchResources"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="用户" name="users">
        <div class="user-results" v-loading="userLoading">
          <div
            v-for="user in users"
            :key="user.id"
            class="user-card"
            @click="goUser(user.id)"
          >
            <el-avatar :size="48" :src="user.avatar">
              {{ user.nickname?.[0] || '?' }}
            </el-avatar>
            <div class="user-card__info">
              <div class="user-card__name">
                {{ user.nickname }}
                <span v-if="user.role === 1" class="user-card__badge">站长</span>
              </div>
              <div class="user-card__meta">
                @{{ user.username }} · {{ user.resourceCount || 0 }} 个资源 · {{ formatJoinDate(user.createTime) }} 加入
              </div>
            </div>
            <el-button
              v-if="userStore.isLogin && user.id !== userStore.userInfo?.id"
              size="small"
              class="user-card__msg"
              @click.stop="sendMessage(user.id)"
            >发私信</el-button>
          </div>
        </div>
        <el-empty v-if="!userLoading && !users.length" description="未找到相关用户" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getResourceList } from '@/api/resource'
import { searchUsers } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('resources')
const keyword = ref('')

// Resources
const resourceLoading = ref(false)
const resources = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 12, keyword: '' })

// Users
const userLoading = ref(false)
const users = ref([])

function parseTags(value) {
  return (value || '').split(',').map((item) => item.trim()).filter(Boolean)
}

function formatJoinDate(value) {
  return value ? new Date(value).getFullYear() : '-'
}

async function fetchResources() {
  const kw = route.query.keyword
  if (!kw) return
  keyword.value = kw
  query.keyword = kw
  resourceLoading.value = true
  try {
    const data = await getResourceList(query)
    resources.value = data.list || []
    total.value = data.total || 0
  } finally {
    resourceLoading.value = false
  }
}

async function fetchUsers() {
  const kw = route.query.keyword
  if (!kw) return
  keyword.value = kw
  userLoading.value = true
  try {
    users.value = await searchUsers(kw) || []
  } finally {
    userLoading.value = false
  }
}

watch(() => route.query.keyword, () => {
  query.page = 1
  fetchResources()
  fetchUsers()
})

watch(activeTab, (tab) => {
  if (tab === 'users' && !users.value.length && keyword.value) {
    fetchUsers()
  }
})

function goUser(id) {
  router.push(`/user/${id}`)
}

function sendMessage(id) {
  router.push({ path: '/messages', query: { user: id } })
}

onMounted(() => {
  fetchResources()
  fetchUsers()
})
</script>

<style scoped>
.search-page {
  display: grid;
  gap: 20px;
}

.search-header h2 {
  margin: 0;
  font-size: 22px;
}

.search-summary {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.search-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

/* ---- User Results ---- */
.user-results {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-light, #f0f0f0);
  cursor: pointer;
  transition: all 0.15s;
}

.user-card:hover {
  border-color: var(--tint);
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.1);
}

.user-card__info {
  flex: 1;
  min-width: 0;
}

.user-card__name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary, #1f2937);
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-card__badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: #fff;
}

.user-card__meta {
  font-size: 13px;
  color: var(--text-secondary, #6b7280);
  margin-top: 2px;
}

.user-card__msg {
  flex-shrink: 0;
}
</style>
