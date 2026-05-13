import request from '@/utils/request'

export function getCommentList(resourceId) {
  return request.get('/comment/list', { params: { resourceId } })
}

export function createComment(resourceId, content, parentId) {
  return request.post('/comment', null, { params: { resourceId, content, parentId } })
}

export function deleteComment(id) {
  return request.delete(`/comment/${id}`)
}
