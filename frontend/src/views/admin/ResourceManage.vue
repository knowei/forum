<template>
  <div class="panel-card">
    <div class="table-header">
      <h2>资源管理</h2>
      <el-button @click="fetchList">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="fetchList">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="已发布" name="1" />
      <el-tab-pane label="已驳回" name="2" />
      <el-tab-pane label="全部" name="all" />
    </el-tabs>

    <el-table :data="list" v-loading="loading" empty-text="暂无数据">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="authorNickname" label="作者" width="100" />
      <el-table-column label="类型" width="60">
        <template #default="{ row }">{{ row.type === 1 ? '文章' : '资源' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="简介" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" min-width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handlePreview(row)">预览</el-button>
          <template v-if="row.status === 0">
            <el-button type="success" link @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button type="danger" link @click="handleAudit(row.id, 2)">驳回</el-button>
          </template>
          <el-tag v-else-if="row.status === 1" size="small" type="success">已通过</el-tag>
          <el-tag v-else size="small" type="danger">已驳回</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="previewVisible" title="资源预览" width="800px" top="5vh" :close-on-click-modal="false">
      <template v-if="previewData">
        <img v-if="previewData.cover" :src="previewData.cover" class="preview-cover" />
        <h1 class="preview-title">{{ previewData.title }}</h1>
        <p v-if="previewData.description" class="preview-desc">{{ previewData.description }}</p>
        <div v-if="previewTags.length" class="preview-tags">
          <el-tag v-for="tag in previewTags" :key="tag" size="small">{{ tag }}</el-tag>
        </div>
        <div class="preview-meta">
          <span>作者：{{ previewData.authorNickname }}</span>
          <span>链接：{{ previewData.downloadLink }}</span>
          <span v-if="previewData.extractCode">提取码：{{ previewData.extractCode }}</span>
          <span v-if="previewData.unzipPassword">解压码：{{ previewData.unzipPassword }}</span>
        </div>
        <el-divider />
        <div class="markdown-body" v-html="previewRendered"></div>
      </template>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button v-if="previewData && previewData.status === 0" type="success" @click="handleAudit(previewData.id, 1)">通过</el-button>
        <el-button v-if="previewData && previewData.status === 0" type="danger" @click="handleAudit(previewData.id, 2)">驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { auditResource, getPendingResources, getResourceDetail } from '@/api/resource'
import { getAdminResourceList } from '@/api/admin'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight(str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

const loading = ref(false)
const list = ref([])
const activeTab = ref('pending')
const previewVisible = ref(false)
const previewData = ref(null)

const previewTags = computed(() => (previewData.value?.tags || '').split(',').map(t => t.trim()).filter(Boolean))
const previewRendered = computed(() => DOMPurify.sanitize(md.render(previewData.value?.content || '')))

function statusText(status) {
  if (status === 1) return '已发布'
  if (status === 0) return '待审核'
  if (status === 2) return '已驳回'
  if (status === 3) return '草稿'
  return '未知'
}

function statusType(status) {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  if (status === 2) return 'danger'
  if (status === 3) return 'info'
  return 'info'
}

async function fetchList() {
  loading.value = true
  try {
    if (activeTab.value === 'pending') {
      list.value = await getPendingResources()
    } else {
      const status = activeTab.value === 'all' ? undefined : Number(activeTab.value)
      list.value = await getAdminResourceList(status)
    }
  } finally {
    loading.value = false
  }
}

async function handlePreview(row) {
  previewData.value = null
  previewVisible.value = true
  previewData.value = await getResourceDetail(row.id)
}

async function handleAudit(id, status) {
  await auditResource(id, { status })
  ElMessage.success(status === 1 ? '审核通过' : '已驳回')
  previewVisible.value = false
  await fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.preview-cover {
  max-width: 100%;
  max-height: 300px;
  border-radius: 12px;
  margin-bottom: 16px;
}
.preview-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 12px;
  letter-spacing: -0.3px;
}
.preview-desc {
  color: var(--text-secondary);
  margin: 0 0 12px;
  line-height: 1.6;
}
.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: var(--text-muted);
  font-size: 14px;
}
</style>
