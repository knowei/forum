import request from '@/utils/request'

export function getCategoryList() {
  return request.get('/category/list')
}

export function getAdminCategoryList() {
  return request.get('/admin/category/list')
}

export function createCategory(data) {
  return request.post('/admin/category', data)
}

export function updateCategory(id, data) {
  return request.put(`/admin/category/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/admin/category/${id}`)
}
