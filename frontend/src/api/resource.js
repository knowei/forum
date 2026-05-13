import request from '@/utils/request'

export function getResourceList(params) {
  return request.get('/resource/list', { params })
}

export function getResourceDetail(id) {
  return request.get(`/resource/${id}`)
}

export function createResource(data) {
  return request.post('/resource', data)
}

export function updateResource(id, data) {
  return request.put(`/resource/${id}`, data)
}

export function getMyResources() {
  return request.get('/resource/my')
}

export function getPendingResources() {
  return request.get('/admin/resource/pending')
}

export function auditResource(id, data) {
  return request.put(`/admin/resource/${id}/status`, data)
}

export function uploadImage(file, scene = 'cover') {
  const formData = new FormData()
  formData.append('file', file)
  if (scene) formData.append('scene', scene)
  return request.post('/upload/image', formData)
}
