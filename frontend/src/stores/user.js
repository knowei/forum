import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { getUserInfo } from '@/api/user'
import { clearAuth, getToken, getUser, setToken, setUser } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(getUser())

  const isLogin = computed(() => Boolean(token.value))
  const isAdmin = computed(() => userInfo.value?.role === 1)

  async function login(payload) {
    const data = await loginApi(payload)
    token.value = data.token
    userInfo.value = data.userInfo
    setToken(data.token)
    setUser(data.userInfo)
    return data
  }

  async function register(payload) {
    await registerApi(payload)
  }

  async function fetchUserInfo() {
    if (!token.value) {
      return null
    }
    const data = await getUserInfo()
    userInfo.value = data
    setUser(data)
    return data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    clearAuth()
  }

  return {
    token,
    userInfo,
    isLogin,
    isAdmin,
    login,
    register,
    fetchUserInfo,
    logout
  }
})
