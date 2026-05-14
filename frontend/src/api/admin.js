import request from '@/utils/request'

export function getAdminStatistics() {
  return request.get('/admin/statistics')
}

export function getAdminResourceList(status) {
  return request.get('/admin/resource/list', { params: { status } })
}

export function getOnlineUsers() {
  return request.get('/admin/online-users')
}

export function getUserList() {
  return request.get('/admin/user/list')
}

export function updateUserStatus(id, status) {
  return request.put(`/admin/user/${id}/status`, { status })
}
