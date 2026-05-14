import request from '@/utils/request'

export function getNoteList() {
  return request.get('/note/list')
}

export function getNoteDetail(id) {
  return request.get(`/note/${id}`)
}

export function createNote(data) {
  return request.post('/note', data)
}

export function deleteNote(id) {
  return request.delete(`/note/${id}`)
}

export function likeNote(id) {
  return request.post(`/note/${id}/like`)
}

export function unlikeNote(id) {
  return request.delete(`/note/${id}/like`)
}

export function getNoteComments(id) {
  return request.get(`/note/${id}/comments`)
}

export function createNoteComment(id, content, parentId) {
  return request.post(`/note/${id}/comment`, null, { params: { content, parentId } })
}

export function deleteNoteComment(id) {
  return request.delete(`/note/comment/${id}`)
}
