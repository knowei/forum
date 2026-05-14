<template>
  <div class="search-page">
    <div class="search-header">
      <h2>搜索结果</h2>
      <p v-if="keyword" class="search-summary">关键词 "<strong>{{ keyword }}</strong>" 共找到 {{ total }} 个资源</p>
    </div>

    <div class="resource-grid" v-loading="loading">
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

    <el-empty v-if="!loading && !resources.length" description="未找到相关资源" />

    <div v-if="total > query.size" class="pagination-wrap">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        background
        layout="prev, pager, next"
        :total="total"
        @current-change="fetchResults"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { getResourceList } from '@/api/resource'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const resources = ref([])
const total = ref(0)
const keyword = ref('')
const query = reactive({
  page: 1,
  size: 12,
  keyword: ''
})

function parseTags(value) {
  return (value || '').split(',').map((item) => item.trim()).filter(Boolean)
}

async function fetchResults() {
  const kw = route.query.keyword
  if (!kw) return
  keyword.value = kw
  query.keyword = kw
  loading.value = true
  try {
    const data = await getResourceList(query)
    resources.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

watch(() => route.query.keyword, () => {
  query.page = 1
  fetchResults()
})

onMounted(fetchResults)
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
</style>
