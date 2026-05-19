<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="resource">
      <section class="page-card detail-header">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
          <el-breadcrumb-item>资源详情</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="detail-header__top">
          <div>
            <h1 class="detail-title">{{ resource.title }}</h1>
            <p v-if="resource.description" class="resource-summary">{{ resource.description }}</p>
          </div>
          <el-tag :type="statusType(resource.status)" effect="light">{{ statusText(resource.status) }}</el-tag>
        </div>

        <div v-if="tags.length" class="detail-tags">
          <el-tag v-for="tag in tags" :key="tag" effect="plain">{{ tag }}</el-tag>
        </div>

        <div class="resource-meta">
          <el-tag size="small" effect="plain">{{ resource.type === 1 ? '文章' : '资源' }}</el-tag>
          <span>作者：{{ resource.authorNickname || '匿名用户' }}</span>
          <span>浏览：{{ resource.viewCount || 0 }}</span>
          <span>评论：{{ resource.commentCount || 0 }}</span>
          <span>创建时间：{{ formatDate(resource.createTime) }}</span>
          <span>更新时间：{{ formatDate(resource.updateTime) }}</span>
        </div>
      </section>

      <div class="detail-layout">
        <article class="page-card detail-article markdown-body" v-html="renderedContent"></article>

        <aside class="panel-card detail-side">
          <div class="detail-side__section">
            <div class="detail-side__label">作者</div>
            <div class="author-info" style="cursor:pointer" @click="goUserSpace(resource.authorId)">
              <el-avatar
                v-if="resource.authorAvatar"
                :src="resource.authorAvatar"
                :size="48"
              />
              <el-avatar v-else :size="48">{{ resource.authorNickname?.[0] || '?' }}</el-avatar>
              <div class="author-info__text">
                <span class="author-info__name">{{ resource.authorNickname }}</span>
              </div>
            </div>
          </div>

          <template v-if="resource.type === 0">
            <div class="detail-side__section">
              <div class="detail-side__label">下载链接</div>
              <div class="detail-side__actions">
                <el-button type="primary" @click="openLink" style="flex:1">立即前往</el-button>
                <el-button @click="copyText(resource.downloadLink, '下载链接已复制')" style="flex:1">复制链接</el-button>
              </div>
            </div>

            <div class="detail-side__section">
              <div class="detail-side__item">
                <span class="detail-side__label">提取码</span>
                <div class="detail-side__value-row">
                  <span>{{ resource.extractCode || '无' }}</span>
                  <el-button v-if="resource.extractCode" link type="primary" @click="copyText(resource.extractCode, '提取码已复制')">复制</el-button>
                </div>
              </div>

              <div class="detail-side__item">
                <span class="detail-side__label">解压码</span>
                <div class="detail-side__value-row">
                  <span>{{ resource.unzipPassword || '无' }}</span>
                  <el-button v-if="resource.unzipPassword" link type="primary" @click="copyText(resource.unzipPassword, '解压码已复制')">复制</el-button>
                </div>
              </div>
            </div>
          </template>

          <div class="detail-side__section">
            <el-button type="warning" plain size="small" @click="handleReport" style="width: 100%">
              报告资源过期
            </el-button>
          </div>
        </aside>
      </div>

      <!-- Comments Section -->
      <section class="page-card comment-section">
        <h3 class="comment-heading">评论（{{ resource.commentCount || 0 }}）</h3>

        <div v-if="userStore.isLogin" class="comment-form">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论…"
            maxlength="1000"
            show-word-limit
          />
          <div class="comment-form__actions">
            <el-button type="primary" :loading="commentLoading" @click="handleComment">发表评论</el-button>
          </div>
        </div>
        <div v-else class="comment-login-tip">
          <el-button link type="primary" @click="openAuth(true)">登录</el-button>后可以发表评论
        </div>

        <div v-if="commentTree.length" class="comment-list">
          <div v-for="top in commentTree" :key="top.id" class="comment-item">
            <el-avatar v-if="top.userAvatar" :src="top.userAvatar" :size="36" loading="lazy" />
            <el-avatar v-else :size="36">{{ top.userNickname?.[0] || '?' }}</el-avatar>
            <div class="comment-item__body">
              <div class="comment-item__header">
                <span class="comment-item__user">{{ top.userNickname }}</span>
                <span class="comment-item__time">{{ formatDate(top.createTime) }}</span>
              </div>
              <div class="comment-item__content">{{ top.content }}</div>
              <div class="comment-item__actions">
                <el-button v-if="userStore.isLogin" link type="primary" size="small" @click="startReply(top)">回复</el-button>
              </div>

              <!-- Replies -->
              <div v-if="top.replies?.length" class="comment-replies">
                <div v-for="reply in top.replies" :key="reply.id" class="comment-item comment-item--reply">
                  <el-avatar v-if="reply.userAvatar" :src="reply.userAvatar" :size="28" loading="lazy" />
                  <el-avatar v-else :size="28">{{ reply.userNickname?.[0] || '?' }}</el-avatar>
                  <div class="comment-item__body">
                    <div class="comment-item__header">
                      <span class="comment-item__user">{{ reply.userNickname }}</span>
                      <span class="comment-item__time">{{ formatDate(reply.createTime) }}</span>
                    </div>
                    <div class="comment-item__content">{{ reply.content }}</div>
                  </div>
                </div>
              </div>

              <!-- Reply form -->
              <div v-if="replyTarget?.id === top.id" class="comment-reply-form">
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  placeholder="@{{ top.userNickname }}"
                  maxlength="1000"
                  show-word-limit
                />
                <div class="comment-reply-form__actions">
                  <el-button size="small" @click="cancelReply">取消</el-button>
                  <el-button size="small" type="primary" :loading="replyLoading" @click="handleReply(top.id)">回复</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-else-if="!commentLoading" description="暂无评论" :image-size="60" />
      </section>
    </template>

    <el-empty v-else-if="!loading" description="资源不存在或当前无权限查看" />
  </div>

  <!-- Back to Top -->
  <Transition name="fade">
    <button v-if="showBackTop" class="back-top-btn" aria-label="返回顶部" @click="scrollToTop">
      <el-icon :size="20" aria-hidden="true"><ArrowUp /></el-icon>
    </button>
  </Transition>

  <!-- Like & Collect Float -->
    <div v-if="resource" class="action-float">
      <button class="action-float__btn" :class="{ active: resource?.liked }" aria-label="点赞" @click="handleLike">
        <el-icon :size="20" aria-hidden="true">
          <component :is="resource?.liked ? GoodsFilled : Goods" />
        </el-icon>
        <span>{{ resource?.likeCount ?? 0 }}</span>
      </button>
      <div class="action-float__divider" />
      <button class="action-float__btn" :class="{ active: resource?.collected }" aria-label="收藏" @click="handleCollect">
        <el-icon :size="20" aria-hidden="true">
          <component :is="resource?.collected ? StarFilled : Star" />
        </el-icon>
        <span>{{ resource?.collectCount ?? 0 }}</span>
      </button>
    </div>

  <AuthDialog v-if="showAuth" v-model:visible="showAuth" :login-mode="true" @success="showAuth = false" />
</template>

<script setup>
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import { ArrowUp, Goods, GoodsFilled, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getResourceDetail, likeResource, unlikeResource, collectResource, uncollectResource } from '@/api/resource'
import { getCommentList, createComment } from '@/api/comment'
import { reportResource } from '@/api/report'
import { useUserStore } from '@/stores/user'
import AuthDialog from '@/components/common/AuthDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const resource = ref(null)
const comments = ref([])
const commentContent = ref('')
const commentLoading = ref(false)
const replyTarget = ref(null)
const replyContent = ref('')
const replyLoading = ref(false)
const showAuth = ref(false)
const showBackTop = ref(false)
let scrollHandler = null

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

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

const renderedContent = computed(() => DOMPurify.sanitize(md.render(resource.value?.content || '')))
const tags = computed(() => (resource.value?.tags || '').split(',').map((item) => item.trim()).filter(Boolean))

const commentTree = computed(() => {
  const topLevel = comments.value.filter((c) => !c.parentId)
  const replies = comments.value.filter((c) => c.parentId)
  return topLevel.map((c) => ({
    ...c,
    replies: replies.filter((r) => r.parentId === c.id)
  }))
})

function formatDate(value) {
  return value ? value.replace('T', ' ') : '-'
}

function statusText(status) {
  if (status === 1) return '已发布'
  if (status === 0) return '待审核'
  if (status === 2) return '已驳回'
  return '未知状态'
}

function statusType(status) {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  if (status === 2) return 'danger'
  return 'info'
}

function openAuth(loginMode) {
  showAuth.value = true
}

async function handleLike() {
  if (!userStore.isLogin) { showAuth.value = true; return }
  try {
    if (resource.value.liked) {
      await unlikeResource(resource.value.id)
      resource.value.liked = false
      resource.value.likeCount = Math.max(0, (resource.value.likeCount || 0) - 1)
    } else {
      await likeResource(resource.value.id)
      resource.value.liked = true
      resource.value.likeCount = (resource.value.likeCount || 0) + 1
    }
  } catch { /* handled by interceptor */ }
}

async function handleCollect() {
  if (!userStore.isLogin) { showAuth.value = true; return }
  try {
    if (resource.value.collected) {
      await uncollectResource(resource.value.id)
      resource.value.collected = false
      resource.value.collectCount = Math.max(0, (resource.value.collectCount || 0) - 1)
    } else {
      await collectResource(resource.value.id)
      resource.value.collected = true
      resource.value.collectCount = (resource.value.collectCount || 0) + 1
    }
  } catch { /* handled by interceptor */ }
}

function goUserSpace(id) {
  router.push(`/user/${id}`)
}

async function copyText(value, message) {
  await navigator.clipboard.writeText(value)
  ElMessage.success(message)
}

function openLink() {
  window.open(resource.value.downloadLink, '_blank', 'noopener,noreferrer')
}

async function handleReport() {
  if (!userStore.isLogin) {
    showAuth.value = true
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请描述资源过期原因', '报告资源过期', {
      inputPlaceholder: '例如：下载链接已失效',
      inputType: 'textarea',
      inputValidator: (v) => (v ? true : '请输入原因')
    })
    await reportResource(resource.value.id, value)
    ElMessage.success('已提交报告，我们会尽快处理')
  } catch {
    // cancelled
  }
}

async function handleComment() {
  if (!commentContent.value.trim()) return
  commentLoading.value = true
  try {
    await createComment(resource.value.id, commentContent.value.trim(), null)
    ElMessage.success('评论发表成功')
    commentContent.value = ''
    await fetchComments()
    if (resource.value) resource.value.commentCount = (resource.value.commentCount || 0) + 1
  } finally {
    commentLoading.value = false
  }
}

function startReply(comment) {
  replyTarget.value = comment
  replyContent.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyContent.value = ''
}

async function handleReply(parentId) {
  if (!replyContent.value.trim()) return
  replyLoading.value = true
  try {
    await createComment(resource.value.id, replyContent.value.trim(), parentId)
    ElMessage.success('回复成功')
    replyContent.value = ''
    replyTarget.value = null
    await fetchComments()
    if (resource.value) resource.value.commentCount = (resource.value.commentCount || 0) + 1
  } finally {
    replyLoading.value = false
  }
}

async function fetchComments() {
  try {
    comments.value = await getCommentList(route.params.id)
  } catch {
    comments.value = []
  }
}

async function fetchDetail() {
  loading.value = true
  try {
    resource.value = await getResourceDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

async function loadAll() {
  await Promise.all([fetchDetail(), fetchComments()])
}

watch(() => route.params.id, loadAll)

onMounted(() => {
  loadAll()
  scrollHandler = () => {
    showBackTop.value = window.scrollY > 400
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })
})

onUnmounted(() => {
  if (scrollHandler) {
    window.removeEventListener('scroll', scrollHandler)
  }
})
</script>

<style scoped>
.comment-section {
  margin-top: 20px;
}

.comment-heading {
  margin: 0 0 20px;
  font-size: 18px;
}

.comment-form {
  margin-bottom: 24px;
}

.comment-form__actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.comment-login-tip {
  margin-bottom: 24px;
  color: #9ca3af;
  font-size: 14px;
  text-align: center;
  padding: 24px;
  background: #f9fafb;
  border-radius: 8px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item__body {
  flex: 1;
  min-width: 0;
}

.comment-item__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-item__user {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.comment-item__time {
  font-size: 12px;
  color: #9ca3af;
}

.comment-item__content {
  font-size: 14px;
  line-height: 1.6;
  color: #4b5563;
  word-break: break-word;
}

.comment-item__actions {
  margin-top: 4px;
}

.comment-replies {
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item--reply {
  padding: 0;
  border-bottom: none;
}

.comment-reply-form {
  margin-top: 12px;
}

.comment-reply-form__actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* ---- Like & Collect Float ---- */
.action-float {
  position: fixed;
  right: 32px;
  bottom: 110px;
  z-index: 200;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 6px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.1);
}

.action-float__btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  width: 52px;
  padding: 6px 0;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  font-size: 11px;
  line-height: 1.2;
  transition: background 0.2s, color 0.2s;
}

.action-float__btn:hover {
  background: var(--bg-hover);
  color: var(--tint);
}

.action-float__btn.active {
  color: #e74c3c;
}

.action-float__btn.active:last-child {
  color: #e6a23c;
}

.action-float__divider {
  width: 32px;
  height: 1px;
  background: #e5e7eb;
}

@media (prefers-reduced-motion: reduce) {
  .action-float {
    transition: none;
  }
  .action-float__btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .action-float {
    right: 16px;
    bottom: 90px;
  }
  .action-float__btn {
    width: 44px;
    padding: 4px 0;
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .action-float {
    right: 12px;
    bottom: 80px;
    padding: 4px;
    border-radius: 10px;
  }
  .action-float__btn {
    width: 40px;
    padding: 3px 0;
    font-size: 10px;
  }
}
</style>
