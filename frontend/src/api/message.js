import request from '@/utils/request'

export function getConversations() {
  return request.get('/message/conversations')
}

export function createOrGetConversation(userId) {
  return request.post(`/message/conversation/${userId}`)
}

export function getMessages(conversationId, offset = 0, limit = 20) {
  return request.get(`/message/conversation/${conversationId}/messages`, { params: { offset, limit } })
}

export function sendMessage(conversationId, content) {
  return request.post('/message/send', { conversationId, content })
}

export function getMessageUnreadCount() {
  return request.get('/message/unread-count')
}

export function markConversationRead(conversationId) {
  return request.put(`/message/conversation/${conversationId}/read`)
}
