<template>
  <div class="home-page">
    <section class="home-hero">
      <div class="home-hero__inner">
        <div class="home-hero__eyebrow">资源分享社区</div>
        <h1 class="home-hero__title">发现、整理并分享你的优质资源</h1>
        <p class="home-hero__desc">支持软件工具、学习资料、开发资源等多种分类，一站式发现优质内容</p>
        <div class="home-hero__actions">
          <router-link to="/publish">
            <el-button type="primary" round>发布资源</el-button>
          </router-link>
        </div>
      </div>
    </section>

    <section class="category-bar">
      <el-tag
        :effect="activeCategory == null ? 'dark' : 'plain'"
        class="clickable-tag"
        @click="selectCategory()"
      >全部</el-tag>
      <el-tag
        v-for="item in categories"
        :key="item.id"
        :effect="activeCategory === item.id ? 'dark' : 'plain'"
        class="clickable-tag"
        @click="selectCategory(item.id)"
      >{{ item.name }}</el-tag>
    </section>

    <div class="toolbar-row">
      <span class="toolbar-summary">共 {{ total }} 个资源</span>
      <div class="toolbar-right">
        <el-radio-group v-model="query.orderBy" size="small" @change="handleSortChange">
          <el-radio-button value="">最新发布</el-radio-button>
          <el-radio-button value="views">最多浏览</el-radio-button>
          <el-radio-button value="likes">最多点赞</el-radio-button>
        </el-radio-group>
        <el-button text size="small" @click="resetFilters">重置</el-button>
      </div>
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
            <span class="resource-card__author" @click.stop="$router.push(`/user/${item.authorId}`)">{{ item.authorNickname || '匿名' }}</span>
            <span class="resource-card__views">{{ item.viewCount || 0 }} 次浏览</span>
          </div>
        </div>
      </router-link>
    </div>

    <el-empty v-if="!loading && !resources.length" description="暂无资源" />

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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import { getResourceList } from '@/api/resource'

const loading = ref(false)
const categories = ref([])
const resources = ref([])
const total = ref(0)
const activeCategory = ref(undefined)
const query = reactive({
  page: 1,
  size: 12,
  categoryId: undefined,
  keyword: '',
  orderBy: ''
})

function handleSortChange() {
  query.page = 1
  fetchResources()
}

function parseTags(value) {
  return (value || '').split(',').map((item) => item.trim()).filter(Boolean)
}

function selectCategory(categoryId) {
  activeCategory.value = categoryId
  query.categoryId = categoryId
  query.page = 1
  fetchResources()
}

function resetFilters() {
  activeCategory.value = undefined
  query.categoryId = undefined
  query.keyword = ''
  query.page = 1
  fetchResources()
}

async function fetchCategories() {
  categories.value = await getCategoryList()
}

async function fetchResources() {
  loading.value = true
  try {
    const data = await getResourceList(query)
    resources.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchResources()])
})
</script>
