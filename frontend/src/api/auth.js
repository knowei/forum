import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function sendVerifyCode(email) {
  return request.post('/auth/send-code', { email })
}

export function forgotPassword(email) {
  return request.post('/auth/forgot-password', { email })
}

export function resetPassword(email, code, newPassword) {
  return request.post('/auth/reset-password', { email, code, newPassword })
}
