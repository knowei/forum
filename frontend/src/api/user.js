import request from '@/utils/request'

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateUserAvatar(avatar) {
  return request.put('/user/avatar', { avatar })
}

export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

export function updatePassword(data) {
  return request.put('/user/password', data)
}

export function getNotifications() {
  return request.get('/user/notifications')
}

export function getUnreadCount() {
  return request.get('/user/notifications/unread-count')
}

export function markNotificationRead(id) {
  return request.put(`/user/notifications/${id}/read`)
}

export function markAllNotificationsRead() {
  return request.put('/user/notifications/read-all')
}

export function getUserProfile(id) {
  return request.get(`/user/${id}/profile`)
}
