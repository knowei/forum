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
