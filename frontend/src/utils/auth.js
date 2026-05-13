const TOKEN_KEY = 'forum_token'
const USER_KEY = 'forum_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUser() {
  localStorage.removeItem(USER_KEY)
}

export function clearAuth() {
  removeToken()
  removeUser()
}

const REMEMBER_KEY = 'forum_remembered'

export function setRememberedUser(data) {
  localStorage.setItem(REMEMBER_KEY, JSON.stringify(data))
}

export function getRememberedUser() {
  const raw = localStorage.getItem(REMEMBER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function clearRememberedUser() {
  localStorage.removeItem(REMEMBER_KEY)
}
