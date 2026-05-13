import request from '@/utils/request'

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateUserAvatar(avatar) {
  return request.put('/user/avatar', { avatar })
}
