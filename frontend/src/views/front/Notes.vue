<template>
  <div class="notes-page">
    <!-- Hero Header -->
    <section class="notes-hero">
      <div class="notes-hero__bg" />
      <div class="notes-hero__content">
        <div class="notes-hero__avatar">
          <el-avatar :size="72" style="background: linear-gradient(135deg, #e91e63, #9c27b0); color: #fff; font-size: 28px;">站</el-avatar>
        </div>
        <h1 class="notes-hero__title">站长随手记</h1>
        <p class="notes-hero__desc">一些琐碎日常和资源推荐 ✨</p>
      </div>
    </section>

    <!-- Admin create form -->
    <section v-if="userStore.isAdmin" class="note-form-card">
      <div class="note-form-card__header">
        <span class="note-form-card__icon">✏️</span>
        <span>写点什么呢…</span>
      </div>
      <el-input
        v-model="form.title"
        placeholder="标题（可选）"
        maxlength="200"
        class="note-form-input"
      />
      <el-input
        v-model="form.content"
        type="textarea"
        :rows="4"
        placeholder="分享你的想法…"
        maxlength="5000"
        show-word-limit
      />
      <div class="note-form-card__actions">
        <el-button type="primary" :loading="creating" round @click="handleCreate">发布</el-button>
      </div>
    </section>

    <!-- Notes Feed -->
    <div v-loading="loading" class="notes-feed">
      <TransitionGroup name="note-card">
        <article v-for="note in notes" :key="note.id" class="note-card">
          <!-- Card Header -->
          <div class="note-card__header">
            <el-avatar
              v-if="note.authorAvatar"
              :src="note.authorAvatar"
              :size="40"
              class="note-card__avatar"
              loading="lazy"
            />
            <el-avatar v-else :size="40" class="note-card__avatar">{{ note.authorNickname?.[0] || '?' }}</el-avatar>
            <div class="note-card__meta">
              <span class="note-card__author">{{ note.authorNickname }}</span>
              <span class="note-card__time">{{ formatDate(note.createTime) }}</span>
            </div>
            <el-button
              v-if="userStore.isAdmin"
              text
              type="danger"
              size="small"
              class="note-card__delete"
              @click="handleDelete(note.id)"
            >删除</el-button>
          </div>

          <!-- Card Title -->
          <h2 v-if="note.title" class="note-card__title">{{ note.title }}</h2>

          <!-- Card Content -->
          <div
            class="note-card__content"
            :class="{ expanded: expandedNote === note.id }"
            @click="toggleExpand(note.id)"
          >{{ note.content }}</div>
          <button
            v-if="note.content?.length > 150"
            class="note-card__expand"
            @click="toggleExpand(note.id)"
          >
            {{ expandedNote === note.id ? '▲ 收起' : '▼ 展开全文' }}
          </button>

          <!-- Card Actions -->
          <div class="note-card__actions">
            <button class="note-action-btn like-btn" :class="{ liked: note.liked }" @click="handleLike(note)">
              <span class="note-action-btn__icon">
                <svg v-if="note.liked" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
              </span>
              <span class="note-action-btn__count">{{ note.likeCount || 0 }}</span>
            </button>
            <button class="note-action-btn comment-btn" :class="{ active: activeNoteId === note.id }" @click="toggleComments(note)">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              <span class="note-action-btn__count">{{ note.commentCount || 0 }}</span>
            </button>
          </div>

          <!-- Comments Section -->
          <Transition name="comments-slide">
            <div v-if="activeNoteId === note.id" class="note-comments">
              <!-- Comment Form -->
              <div v-if="userStore.isLogin" class="comment-form">
                <el-avatar
                  v-if="userStore.userInfo?.avatar"
                  :src="userStore.userInfo.avatar"
                  :size="28"
                  loading="lazy"
                />
                <el-avatar v-else :size="28">{{ userStore.userInfo?.nickname?.[0] || '?' }}</el-avatar>
                <div class="comment-form__input-wrap">
                  <el-input
                    v-model="commentText"
                    placeholder="说点什么…"
                    size="small"
                    @keyup.enter="handleAddComment(note.id)"
                  />
                </div>
                <el-button type="primary" size="small" round :loading="commenting" @click="handleAddComment(note.id)">发送</el-button>
              </div>
              <div v-else class="comment-login-tip">
                <el-button link type="primary" size="small" @click="showAuth = true">登录</el-button> 后可以评论
              </div>

              <!-- Comment List -->
              <div v-if="noteComments.length" class="comment-list">
                <div v-for="c in noteComments" :key="c.id" class="comment-item">
                  <el-avatar v-if="c.userAvatar" :src="c.userAvatar" :size="28" loading="lazy" />
                  <el-avatar v-else :size="28">{{ c.userNickname?.[0] || '?' }}</el-avatar>
                  <div class="comment-item__body">
                    <div class="comment-item__header">
                      <span class="comment-item__user">{{ c.userNickname }}</span>
                      <span class="comment-item__time">{{ formatDate(c.createTime) }}</span>
                      <el-button
                        v-if="userStore.userInfo?.id === c.userId || userStore.isAdmin"
                        link
                        type="danger"
                        size="small"
                        class="comment-item__del"
                        @click="handleDeleteComment(c.id)"
                      >删除</el-button>
                    </div>
                    <div class="comment-item__content">{{ c.content }}</div>
                    <button class="comment-item__reply-btn" @click="startReply(c)">回复</button>

                    <!-- Replies -->
                    <div v-if="c.replies?.length" class="comment-replies">
                      <div v-for="r in c.replies" :key="r.id" class="reply-item">
                        <span class="reply-item__user">{{ r.userNickname }}</span>
                        <span class="reply-item__text">{{ r.content }}</span>
                        <el-button
                          v-if="userStore.userInfo?.id === r.userId || userStore.isAdmin"
                          link
                          type="danger"
                          size="small"
                          class="reply-item__del"
                          @click="handleDeleteComment(r.id)"
                        >删除</el-button>
                      </div>
                    </div>

                    <!-- Reply Form -->
                    <div v-if="replyTarget?.id === c.id" class="reply-form">
                      <el-input
                        v-model="replyText"
                        :placeholder="'回复 @' + c.userNickname"
                        size="small"
                        @keyup.enter="handleReply(note.id, c.id)"
                      />
                      <div class="reply-form__actions">
                        <el-button size="small" @click="cancelReply">取消</el-button>
                        <el-button size="small" type="primary" :loading="replying" @click="handleReply(note.id, c.id)">回复</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无评论，来抢沙发～" :image-size="40" />
            </div>
          </Transition>
        </article>
      </TransitionGroup>
    </div>

    <el-empty v-if="!loading && !notes.length" description="还没有随手记呢～" :image-size="60" />

    <AuthDialog v-model:visible="showAuth" :login-mode="true" @success="showAuth = false" />
  </div>
</template>

<script setup>
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
    ElMessage.success('发布成功 ✨')
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
  noteComments.value = []
  try {
    noteComments.value = await getNoteComments(note.id)
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
    const [comments] = await Promise.all([
      getNoteComments(noteId)
    ])
    noteComments.value = comments
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
    noteComments.value = await getNoteComments(noteId)
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
    if (activeNoteId.value) {
      noteComments.value = await getNoteComments(activeNoteId.value)
    }
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
  if (diff < 172800000) return '昨天'
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  if (y === now.getFullYear()) return `${m}-${day} ${h}:${min}`
  return `${y}-${m}-${day} ${h}:${min}`
}

fetchNotes()
</script>

<style scoped>
/* ── Page ── */
.notes-page {
  max-width: 640px;
  margin: 0 auto;
  padding-bottom: 40px;
}

/* ── Hero ── */
.notes-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  padding: 40px 24px 32px;
  margin-bottom: 20px;
  text-align: center;
  background: linear-gradient(135deg, #fce4ec 0%, #f3e5f5 40%, #e8eaf6 100%);
}
.notes-hero__bg {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 20% 50%, rgba(255,182,193,0.3) 0%, transparent 50%),
                    radial-gradient(circle at 80% 20%, rgba(206,147,216,0.2) 0%, transparent 50%),
                    radial-gradient(circle at 50% 80%, rgba(179,229,252,0.2) 0%, transparent 50%);
  pointer-events: none;
}
.notes-hero__content {
  position: relative;
  z-index: 1;
}
.notes-hero__avatar {
  display: inline-block;
  margin-bottom: 12px;
  border: 3px solid rgba(255,255,255,0.8);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.notes-hero__title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #e91e63, #9c27b0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.notes-hero__desc {
  margin: 6px 0 0;
  font-size: 14px;
  color: #78909c;
}

/* ── Admin Form ── */
.note-form-card {
  background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
  border: 1px solid #f0e6f6;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.note-form-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #78909c;
  margin-bottom: 12px;
}
.note-form-card__icon {
  font-size: 18px;
}
.note-form-input {
  margin-bottom: 12px;
}
.note-form-card__actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

/* ── Feed ── */
.notes-feed {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ── Card ── */
.note-card {
  background: #fff;
  border: 1px solid #f0e6f6;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s, transform 0.2s;
}
.note-card:hover {
  box-shadow: 0 4px 16px rgba(156,39,176,0.08);
}

.note-card__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.note-card__avatar {
  border: 2px solid #f3e5f5;
  flex-shrink: 0;
}
.note-card__meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.note-card__author {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}
.note-card__time {
  font-size: 12px;
  color: #b0bec5;
}
.note-card__delete {
  flex-shrink: 0;
}

.note-card__title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 700;
  color: #2d2d3a;
  line-height: 1.4;
}

.note-card__content {
  font-size: 14px;
  line-height: 1.75;
  color: #455a64;
  white-space: pre-wrap;
  word-break: break-word;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: all 0.3s;
}
.note-card__content.expanded {
  display: block;
  -webkit-line-clamp: unset;
}

.note-card__expand {
  background: none;
  border: none;
  padding: 4px 0 0;
  font-size: 12px;
  color: #b39ddb;
  cursor: pointer;
  transition: color 0.2s;
}
.note-card__expand:hover {
  color: #7e57c2;
}

/* ── Actions ── */
.note-card__actions {
  display: flex;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f3eef8;
}

.note-action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  padding: 6px 14px;
  cursor: pointer;
  border-radius: 20px;
  font-size: 13px;
  color: #90a4ae;
  transition: all 0.2s;
}
.note-action-btn:hover {
  background: #faf0fc;
  color: #ab47bc;
}
.like-btn.liked {
  color: #e91e63;
  background: #fce4ec;
}
.like-btn.liked:hover {
  background: #f8bbd0;
}
.comment-btn.active {
  color: #7e57c2;
  background: #ede7f6;
}
.note-action-btn__icon {
  display: flex;
  align-items: center;
}
.note-action-btn__count {
  font-weight: 500;
  min-width: 8px;
}

/* ── Comments ── */
.note-comments {
  margin-top: 12px;
  padding-top: 4px;
}

.comment-form {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  padding: 10px 14px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #f0e6f6;
}
.comment-form__input-wrap {
  flex: 1;
}

.comment-login-tip {
  text-align: center;
  color: #b0bec5;
  font-size: 13px;
  padding: 14px;
  margin-bottom: 12px;
  background: #fafafa;
  border-radius: 12px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 10px;
}
.comment-item__body {
  flex: 1;
  min-width: 0;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 12px;
}
.comment-item__header {
  display: flex;
  align-items: center;
  gap: 6px;
}
.comment-item__user {
  font-weight: 600;
  font-size: 13px;
  color: #374151;
}
.comment-item__time {
  font-size: 11px;
  color: #b0bec5;
}
.comment-item__del {
  margin-left: auto;
  flex-shrink: 0;
}
.comment-item__content {
  font-size: 13px;
  line-height: 1.5;
  color: #546e7a;
  margin-top: 2px;
  word-break: break-word;
}
.comment-item__reply-btn {
  background: none;
  border: none;
  padding: 2px 0 0;
  font-size: 12px;
  color: #b39ddb;
  cursor: pointer;
}
.comment-item__reply-btn:hover {
  color: #7e57c2;
}

.comment-replies {
  margin-top: 8px;
  padding-left: 8px;
  border-left: 2px solid #ede7f6;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.reply-item {
  font-size: 13px;
  line-height: 1.5;
  display: flex;
  align-items: baseline;
  gap: 4px;
  flex-wrap: wrap;
}
.reply-item__user {
  font-weight: 600;
  color: #374151;
  white-space: nowrap;
}
.reply-item__text {
  color: #546e7a;
  word-break: break-word;
}
.reply-item__del {
  margin-left: auto;
  flex-shrink: 0;
}

.reply-form {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.reply-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
}

/* ── Transitions ── */
.note-card-enter-active {
  transition: all 0.4s ease;
}
.note-card-leave-active {
  transition: all 0.3s ease;
}
.note-card-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.note-card-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.comments-slide-enter-active,
.comments-slide-leave-active {
  transition: all 0.25s ease;
}
.comments-slide-enter-from,
.comments-slide-leave-to {
  opacity: 0;
  max-height: 0;
  overflow: hidden;
}
.comments-slide-enter-to,
.comments-slide-leave-from {
  max-height: 2000px;
}

/* ── Override dark filter for empty state ── */
:deep(.el-empty__description p) {
  color: #b0bec5;
}
</style>
