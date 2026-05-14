<template>
  <div class="notes-page">
    <section class="page-card">
      <h1 class="notes-page__title">站长随手记</h1>
      <p class="notes-page__subtitle">站长的日常碎碎念</p>
    </section>

    <!-- Admin create form -->
    <section v-if="userStore.isAdmin" class="page-card note-form-card">
      <h3 class="note-form-card__title">写随手记</h3>
      <el-input v-model="form.title" placeholder="标题（可选）" maxlength="200" />
      <el-input
        v-model="form.content"
        type="textarea"
        :rows="5"
        placeholder="写点什么吧…"
        maxlength="5000"
        show-word-limit
        style="margin-top:12px"
      />
      <div class="note-form-card__actions">
        <el-button type="primary" :loading="creating" @click="handleCreate">发布</el-button>
      </div>
    </section>

    <!-- Notes feed -->
    <div v-loading="loading" class="notes-feed">
      <div v-for="note in notes" :key="note.id" class="page-card note-card">
        <div class="note-card__header">
          <el-avatar v-if="note.authorAvatar" :src="note.authorAvatar" :size="36" loading="lazy" />
          <el-avatar v-else :size="36">{{ note.authorNickname?.[0] || '?' }}</el-avatar>
          <div class="note-card__author-info">
            <span class="note-card__author-name">{{ note.authorNickname }}</span>
            <span class="note-card__time">{{ formatDate(note.createTime) }}</span>
          </div>
          <el-button v-if="userStore.isAdmin" text type="danger" size="small" @click="handleDelete(note.id)">删除</el-button>
        </div>

        <h2 v-if="note.title" class="note-card__title">{{ note.title }}</h2>
        <div class="note-card__content" :class="{ expanded: expandedNote === note.id }">
          {{ note.content }}
        </div>
        <el-button
          v-if="note.content?.length > 200"
          link
          type="primary"
          size="small"
          @click="toggleExpand(note.id)"
        >
          {{ expandedNote === note.id ? '收起' : '展开全文' }}
        </el-button>

        <div class="note-card__stats">
          <button class="note-stat-btn" :class="{ liked: note.liked }" @click="handleLike(note)">
            <el-icon :size="16" aria-hidden="true"><component :is="note.liked ? GoodsFilled : Goods" /></el-icon>
            <span>{{ note.likeCount || 0 }}</span>
          </button>
          <button class="note-stat-btn" @click="toggleComments(note)">
            <el-icon :size="16" aria-hidden="true"><ChatDotSquare /></el-icon>
            <span>{{ note.commentCount || 0 }}</span>
          </button>
        </div>

        <!-- Comments section -->
        <Transition name="fade">
          <div v-if="activeNoteId === note.id" class="note-comments">
            <div class="note-comments__divider" />
            <div v-if="userStore.isLogin" class="note-comments__form">
              <el-input
                v-model="commentText"
                placeholder="写下你的评论…"
                maxlength="500"
                show-word-limit
                @keyup.enter="handleAddComment(note.id)"
              />
              <el-button type="primary" size="small" :loading="commenting" @click="handleAddComment(note.id)">发送</el-button>
            </div>
            <div v-else class="note-comments__login-tip">
              <el-button link type="primary" size="small" @click="showAuth = true">登录</el-button>后可以评论
            </div>

            <div v-if="noteComments.length" class="note-comments__list">
              <div v-for="c in noteComments" :key="c.id" class="note-comment-item">
                <el-avatar v-if="c.userAvatar" :src="c.userAvatar" :size="28" loading="lazy" />
                <el-avatar v-else :size="28">{{ c.userNickname?.[0] || '?' }}</el-avatar>
                <div class="note-comment-item__body">
                  <div class="note-comment-item__header">
                    <span class="note-comment-item__user">{{ c.userNickname }}</span>
                    <span class="note-comment-item__time">{{ formatDate(c.createTime) }}</span>
                  </div>
                  <div class="note-comment-item__content">{{ c.content }}</div>
                  <div class="note-comment-item__actions">
                    <el-button v-if="userStore.isLogin" link type="primary" size="small" @click="startReply(c)">回复</el-button>
                    <el-button
                      v-if="userStore.userInfo?.id === c.userId || userStore.isAdmin"
                      link
                      type="danger"
                      size="small"
                      @click="handleDeleteComment(c.id)"
                    >删除</el-button>
                  </div>
                  <!-- Replies -->
                  <div v-if="c.replies?.length" class="note-comment-replies">
                    <div v-for="r in c.replies" :key="r.id" class="note-comment-item note-comment-item--reply">
                      <el-avatar v-if="r.userAvatar" :src="r.userAvatar" :size="24" loading="lazy" />
                      <el-avatar v-else :size="24">{{ r.userNickname?.[0] || '?' }}</el-avatar>
                      <div class="note-comment-item__body">
                        <div class="note-comment-item__header">
                          <span class="note-comment-item__user">{{ r.userNickname }}</span>
                          <span class="note-comment-item__time">{{ formatDate(r.createTime) }}</span>
                        </div>
                        <div class="note-comment-item__content">{{ r.content }}</div>
                        <el-button
                          v-if="userStore.userInfo?.id === r.userId || userStore.isAdmin"
                          link
                          type="danger"
                          size="small"
                          @click="handleDeleteComment(r.id)"
                        >删除</el-button>
                      </div>
                    </div>
                  </div>
                  <!-- Reply form -->
                  <div v-if="replyTarget?.id === c.id" class="note-comment-reply-form">
                    <el-input
                      v-model="replyText"
                      placeholder="@{{ c.userNickname }}"
                      size="small"
                      @keyup.enter="handleReply(note.id, c.id)"
                    />
                    <div class="note-comment-reply-form__actions">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button size="small" type="primary" :loading="replying" @click="handleReply(note.id, c.id)">回复</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无评论" :image-size="40" />
          </div>
        </Transition>
      </div>
    </div>

    <el-empty v-if="!loading && !notes.length" description="暂无随手记" />

    <AuthDialog v-model:visible="showAuth" :login-mode="true" @success="showAuth = false" />
  </div>
</template>

<script setup>
import { ChatDotSquare, Goods, GoodsFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ref } from 'vue'
import {
  getNoteList, createNote, deleteNote,
  likeNote, unlikeNote,
  getNoteComments, createNoteComment, deleteNoteComment
} from '@/api/note'
import { useUserStore } from '@/stores/user'
import AuthDialog from '@/components/common/AuthDialog.vue'

const userStore = useUserStore()
const loading = ref(false)
const creating = ref(false)
const notes = ref([])
const form = ref({ title: '', content: '' })
const expandedNote = ref(null)
const activeNoteId = ref(null)
const noteComments = ref([])
const commentText = ref('')
const commenting = ref(false)
const replyTarget = ref(null)
const replyText = ref('')
const replying = ref(false)
const showAuth = ref(false)

async function fetchNotes() {
  loading.value = true
  try {
    notes.value = await getNoteList()
  } catch {
    notes.value = []
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!form.value.content.trim()) return
  creating.value = true
  try {
    await createNote({
      title: form.value.title.trim(),
      content: form.value.content.trim()
    })
    ElMessage.success('发布成功')
    form.value = { title: '', content: '' }
    await fetchNotes()
  } finally {
    creating.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除这条随手记吗？', '删除', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
    await deleteNote(id)
    ElMessage.success('已删除')
    await fetchNotes()
  } catch { /* cancelled */ }
}

async function handleLike(note) {
  if (!userStore.isLogin) { showAuth.value = true; return }
  try {
    if (note.liked) {
      await unlikeNote(note.id)
      note.liked = false
      note.likeCount = Math.max(0, (note.likeCount || 0) - 1)
    } else {
      await likeNote(note.id)
      note.liked = true
      note.likeCount = (note.likeCount || 0) + 1
    }
  } catch { /* handled */ }
}

function toggleExpand(id) {
  expandedNote.value = expandedNote.value === id ? null : id
}

async function toggleComments(note) {
  if (activeNoteId.value === note.id) {
    activeNoteId.value = null
    return
  }
  activeNoteId.value = note.id
  await fetchComments(note.id)
}

async function fetchComments(noteId) {
  try {
    noteComments.value = await getNoteComments(noteId)
  } catch {
    noteComments.value = []
  }
}

async function handleAddComment(noteId) {
  if (!commentText.value.trim()) return
  commenting.value = true
  try {
    await createNoteComment(noteId, commentText.value.trim(), null)
    ElMessage.success('评论成功')
    commentText.value = ''
    await fetchComments(noteId)
    const note = notes.value.find((n) => n.id === noteId)
    if (note) note.commentCount = (note.commentCount || 0) + 1
  } finally {
    commenting.value = false
  }
}

function startReply(comment) {
  replyTarget.value = comment
  replyText.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyText.value = ''
}

async function handleReply(noteId, parentId) {
  if (!replyText.value.trim()) return
  replying.value = true
  try {
    await createNoteComment(noteId, replyText.value.trim(), parentId)
    ElMessage.success('回复成功')
    replyText.value = ''
    replyTarget.value = null
    await fetchComments(noteId)
    const note = notes.value.find((n) => n.id === noteId)
    if (note) note.commentCount = (note.commentCount || 0) + 1
  } finally {
    replying.value = false
  }
}

async function handleDeleteComment(id) {
  try {
    await deleteNoteComment(id)
    ElMessage.success('已删除')
    if (activeNoteId.value) await fetchComments(activeNoteId.value)
  } catch { /* handled */ }
}

function formatDate(value) {
  if (!value) return ''
  const d = new Date(value)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return value.replace('T', ' ').slice(0, 16)
}

fetchNotes()
</script>

<style scoped>
.notes-page__title {
  margin: 0;
  font-size: 22px;
}
.notes-page__subtitle {
  margin: 6px 0 0;
  color: #9ca3af;
  font-size: 14px;
}

.note-form-card {
  margin-top: 16px;
}
.note-form-card__title {
  margin: 0 0 12px;
  font-size: 16px;
}
.note-form-card__actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.notes-feed {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.note-card {
  padding: 20px;
}
.note-card__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.note-card__author-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.note-card__author-name {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}
.note-card__time {
  font-size: 12px;
  color: #9ca3af;
}
.note-card__title {
  margin: 0 0 8px;
  font-size: 18px;
  color: #1f2937;
}
.note-card__content {
  font-size: 14px;
  line-height: 1.7;
  color: #4b5563;
  white-space: pre-wrap;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.note-card__content.expanded {
  display: block;
}
.note-card__stats {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}
.note-stat-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 13px;
  color: #6b7280;
  border-radius: 6px;
  transition: all 0.15s;
}
.note-stat-btn:hover {
  background: #f3f4f6;
  color: #409eff;
}
.note-stat-btn.liked {
  color: #ef4444;
}
.note-stat-btn.liked:hover {
  background: #fef2f2;
}

/* Comments */
.note-comments {
  margin-top: 0;
}
.note-comments__divider {
  height: 1px;
  background: #f3f4f6;
  margin: 12px 0;
}
.note-comments__form {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.note-comments__login-tip {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 12px;
  margin-bottom: 12px;
  background: #f9fafb;
  border-radius: 8px;
}
.note-comments__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.note-comment-item {
  display: flex;
  gap: 10px;
}
.note-comment-item__body {
  flex: 1;
  min-width: 0;
}
.note-comment-item__header {
  display: flex;
  align-items: center;
  gap: 6px;
}
.note-comment-item__user {
  font-weight: 600;
  font-size: 13px;
  color: #374151;
}
.note-comment-item__time {
  font-size: 11px;
  color: #9ca3af;
}
.note-comment-item__content {
  font-size: 13px;
  line-height: 1.5;
  color: #4b5563;
  margin-top: 2px;
}
.note-comment-item__actions {
  margin-top: 2px;
}
.note-comment-replies {
  margin-top: 8px;
  padding-left: 10px;
  border-left: 2px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.note-comment-item--reply {
  padding: 0;
}
.note-comment-reply-form {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}
.note-comment-reply-form__actions {
  display: flex;
  gap: 4px;
}
</style>
