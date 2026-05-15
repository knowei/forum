import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

const STORAGE_KEY = 'forum_theme'

function getInitialTheme() {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'dark' || stored === 'light') return stored
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(getInitialTheme())

  function applyTheme(val) {
    document.documentElement.setAttribute('data-theme', val)
    if (val === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  function toggle() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    localStorage.setItem(STORAGE_KEY, theme.value)
    applyTheme(theme.value)
  }

  function init() {
    applyTheme(theme.value)
  }

  const isDark = computed(() => theme.value === 'dark')

  return { theme, isDark, toggle, init }
})
