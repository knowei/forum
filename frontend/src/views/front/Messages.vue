<template>
  <div class="messages-page">
    <div class="messages-container">
      <!-- Sidebar -->
      <div class="conv-sidebar" :class="{ 'conv-sidebar--hidden': mobileView === 'chat' }">
        <div class="conv-sidebar__header">
          <h2>私信</h2>
        </div>
        <div v-if="conversations.length" class="conv-list">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="conv-item"
            :class="{ 'conv-item--active': activeConvId === conv.id }"
            @click="selectConversation(conv)"
          >
            <el-avatar :size="40" :src="conv.otherAvatar">
              {{ conv.otherNickname?.[0] }}
            </el-avatar>
            <div class="conv-item__body">
              <div class="conv-item__top">
                <span class="conv-item__name">{{ conv.otherNickname }}</span>
                <span class="conv-item__time">{{ formatTime(conv.lastTime) }}</span>
              </div>
              <div class="conv-item__bottom">
                <span class="conv-item__preview">{{ conv.lastContent || '暂无消息' }}</span>
                <el-badge v-if="conv.unreadCount" :value="conv.unreadCount" class="conv-badge" />
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无会话" :image-size="60" />
      </div>

      <!-- Chat panel -->
      <div class="chat-panel" :class="{ 'chat-panel--hidden': mobileView === 'list' }">
        <template v-if="activeConv">
          <div class="chat-panel__header">
            <button v-if="mobileView === 'chat'" class="chat-back-btn" aria-label="返回" @click="mobileView = 'list'">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
            </button>
            <el-avatar :size="32" :src="activeConv.otherAvatar">
              {{ activeConv.otherNickname?.[0] }}
            </el-avatar>
            <span class="chat-panel__name">{{ activeConv.otherNickname }}</span>
          </div>

          <div class="chat-panel__messages" ref="msgListRef">
            <div v-if="loadingMore" class="chat-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="msg-item"
              :class="{ 'msg-item--mine': msg.senderId === currentUserId }"
            >
              <el-avatar v-if="msg.senderId !== currentUserId" :size="28" :src="msg.senderAvatar">
                {{ msg.senderNickname?.[0] }}
              </el-avatar>
              <div class="msg-item__content">
                <div class="msg-bubble">{{ msg.content }}</div>
                <div class="msg-meta">
                  <span class="msg-time">{{ formatMsgTime(msg.createTime) }}</span>
                  <span v-if="msg.senderId === currentUserId && msg.isRead" class="msg-read-badge">✓✓</span>
                </div>
              </div>
              <el-avatar v-if="msg.senderId === currentUserId" :size="28" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.[0] }}
              </el-avatar>
            </div>
            <div v-if="!messages.length && !loadingMore" class="chat-empty">
              <span>发送第一条消息吧</span>
            </div>
          </div>

          <div class="chat-panel__input">
            <div class="input-bar">
              <input
                v-model="inputContent"
                class="input-bar__field"
                placeholder="输入消息…"
                :disabled="sending"
                @keydown.enter.exact.prevent="handleSend"
              />
              <button
                class="input-bar__send"
                :class="{ 'input-bar__send--active': inputContent.trim() }"
                :disabled="sending || !inputContent.trim()"
                @click="handleSend"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="22" y1="2" x2="11" y2="13" />
                  <polygon points="22 2 15 22 11 13 2 9 22 2" />
                </svg>
              </button>
            </div>
          </div>
        </template>

        <div v-else class="chat-panel__empty">
          <el-icon :size="48" color="#d0d5dd"><ChatLineSquare /></el-icon>
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Loading, ChatLineSquare } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getConversations,
  getMessages,
  sendMessage,
  getMessageUnreadCount,
  markConversationRead,
  createOrGetConversation
} from '@/api/message'

const route = useRoute()
const userStore = useUserStore()

const conversations = ref([])
const activeConvId = ref(null)
const activeConv = ref(null)
const messages = ref([])
const inputContent = ref('')
const sending = ref(false)
const loadingMore = ref(false)
const msgListRef = ref(null)
const currentUserId = ref(null)
const hasMore = ref(true)
const messageOffset = ref(0)
const unreadCount = ref(0)
const mobileView = ref('list')

function isMobile() {
  return window.innerWidth < 768
}

let pollTimer = null

onMounted(() => {
  currentUserId.value = userStore.userInfo?.id
  loadConversations()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

watch(() => route.query.user, async (userId) => {
  if (userId) {
    await startConversation(Number(userId))
  }
}, { immediate: true })

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}/${pad(d.getDate())}`
}

function formatMsgTime(value) {
  if (!value) return ''
  const d = new Date(value)
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const time = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  if (d.toDateString() === now.toDateString()) return time
  return `${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${time}`
}

async function loadConversations() {
  try {
    const list = await getConversations()
    conversations.value = list || []
    // Update unread badge on nav
    updateTotalUnread()
  } catch { /* ignore */ }
}

async function updateTotalUnread() {
  try {
    const { count } = await getMessageUnreadCount()
    unreadCount.value = count
    updateNavUnread(count)
  } catch { /* ignore */ }
}

function updateNavUnread(count) {
  window.dispatchEvent(new CustomEvent('msg-unread', { detail: count }))
}

async function selectConversation(conv) {
  activeConvId.value = conv.id
  activeConv.value = conv
  messages.value = []
  messageOffset.value = 0
  hasMore.value = true
  if (isMobile()) mobileView.value = 'chat'

  try {
    const list = await getMessages(conv.id, 0, 50)
    messages.value = (list || []).reverse()
    hasMore.value = list && list.length >= 50
    messageOffset.value = list ? list.length : 0
    await markConversationRead(conv.id)
    conv.unreadCount = 0
    updateTotalUnread()
    scrollToBottom()
  } catch { /* ignore */ }
}

async function startConversation(otherUserId) {
  try {
    const { conversationId } = await createOrGetConversation(otherUserId)
    await loadConversations()
    const conv = conversations.value.find(c => c.id === conversationId)
    if (conv) {
      selectConversation(conv)
    } else {
      activeConvId.value = conversationId
      activeConv.value = { id: conversationId, otherNickname: '用户' }
      messages.value = []
    }
  } catch { /* ignore */ }
}

async function handleSend() {
  const content = inputContent.value.trim()
  if (!content || !activeConvId.value) return

  sending.value = true
  try {
    const msg = await sendMessage(activeConvId.value, content)
    msg.senderId = currentUserId.value
    messages.value.push(msg)
    inputContent.value = ''
    nextTick(() => scrollToBottom())
    // Refresh conversation list to update last message
    loadConversations()
  } catch { /* ignore */ }
  sending.value = false
}

function scrollToBottom() {
  if (msgListRef.value) {
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  }
}

function startPolling() {
  pollTimer = setInterval(() => {
    if (activeConvId.value) {
      loadNewMessages()
    }
    updateTotalUnread()
  }, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function loadNewMessages() {
  if (!activeConvId.value) return
  try {
    const list = await getMessages(activeConvId.value, 0, 50)
    if (list && list.length) {
      const existingIds = new Set(messages.value.map(m => m.id))
      const newMsgs = list.reverse().filter(m => !existingIds.has(m.id))
      if (newMsgs.length) {
        messages.value.push(...newMsgs)
        await markConversationRead(activeConvId.value)
        if (activeConv.value) activeConv.value.unreadCount = 0
        nextTick(() => scrollToBottom())
      }
    }
  } catch { /* ignore */ }
}
</script>

<style scoped>
.messages-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 0;
  height: calc(100vh - 140px);
}

.messages-container {
  display: flex;
  height: 100%;
  background: var(--bg-card, #fff);
  border-radius: 12px;
  box-shadow: var(--shadow, 0 1px 3px rgba(0,0,0,.1));
  overflow: hidden;
}

/* ---- Sidebar ---- */
.conv-sidebar {
  width: 320px;
  border-right: 1px solid var(--border-light, #f0f0f0);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.conv-sidebar__header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light, #f0f0f0);
}

.conv-sidebar__header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
}

.conv-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.15s;
}

.conv-item:hover {
  background: var(--bg-hover, #f9fafb);
}

.conv-item--active {
  background: #eef4ff;
}

.conv-item--active:hover {
  background: #e6eefe;
}

.conv-item__body {
  flex: 1;
  min-width: 0;
}

.conv-item__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conv-item__name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #1f2937);
}

.conv-item__time {
  font-size: 11px;
  color: var(--text-muted, #9ca3af);
}

.conv-item__bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}

.conv-item__preview {
  font-size: 13px;
  color: var(--text-secondary, #6b7280);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 160px;
}

.conv-badge {
  flex-shrink: 0;
}

/* ---- Chat Panel ---- */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-panel__header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-light, #f0f0f0);
}

.chat-panel__name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary, #1f2937);
}

.chat-panel__messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-loading {
  text-align: center;
  padding: 8px;
  color: var(--text-muted, #9ca3af);
}

.chat-empty {
  text-align: center;
  color: var(--text-muted, #9ca3af);
  padding-top: 40px;
  font-size: 13px;
}

.chat-panel__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-muted, #9ca3af);
}

.chat-panel__empty p {
  margin: 0;
  font-size: 14px;
}

.chat-panel__input {
  padding: 12px 16px;
  border-top: 1px solid var(--border-light, #f0f0f0);
  background: var(--bg-card, #fff);
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-hover, #f3f4f6);
  border: 1px solid var(--border-light, #e5e7eb);
  border-radius: 10px;
  padding: 4px 4px 4px 14px;
  transition: border-color 0.2s, background 0.2s;
}

.input-bar:focus-within {
  border-color: #409eff;
  background: var(--bg-card, #fff);
}

.input-bar__field {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--text-primary, #1f2937);
  line-height: 1.5;
  padding: 6px 0;
  min-width: 0;
}

.input-bar__field::placeholder {
  color: var(--text-muted, #9ca3af);
}

.input-bar__field:disabled {
  opacity: 0.5;
}

.input-bar__send {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: #d0d5dd;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.input-bar__send:hover:not(:disabled) {
  background: #b0b5bd;
}

.input-bar__send--active {
  background: #409eff;
}

.input-bar__send--active:hover:not(:disabled) {
  background: #3a8ce6;
}

.input-bar__send:disabled {
  cursor: not-allowed;
}

/* ---- Message Bubbles ---- */
.msg-item {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  max-width: 72%;
  animation: msg-in 0.2s ease-out;
}

@keyframes msg-in {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-item--mine {
  align-self: flex-end;
}

.msg-item__content {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.msg-item--mine .msg-item__content {
  align-items: flex-end;
}

.msg-bubble {
  padding: 10px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  background: #f0f0f0;
  color: #1f2937;
}

.msg-item--mine .msg-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-item:not(.msg-item--mine) .msg-bubble {
  border-bottom-left-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: var(--text-muted, #9ca3af);
  padding: 0 6px;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 4px;
}

.msg-read-badge {
  font-size: 11px;
  color: #409eff;
  line-height: 1;
}

/* ---- Back Button ---- */
.chat-back-btn {
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
}

.chat-back-btn:hover {
  background: var(--bg-hover);
  color: #409eff;
}

/* ---- Mobile ---- */
@media (max-width: 768px) {
  .messages-page {
    padding: 0;
    height: calc(100vh - 80px);
  }

  .messages-container {
    border-radius: 0;
  }

  .conv-sidebar {
    width: 100%;
  }

  .conv-sidebar--hidden {
    display: none;
  }

  .chat-panel--hidden {
    display: none;
  }

  .chat-back-btn {
    display: flex;
  }
}
</style>
