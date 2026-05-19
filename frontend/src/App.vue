<template>
  <router-view v-slot="{ Component, route }">
    <Transition name="fade-slide" mode="out-in">
      <component :is="Component" :key="route.path" />
    </Transition>
  </router-view>
</template>

<script setup>
import { watch } from 'vue'
import { onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'

const themeStore = useThemeStore()
const userStore = useUserStore()

onMounted(() => themeStore.init())

watch(() => userStore.userInfo?.bgImage, (bg) => {
  const html = document.documentElement
  if (bg) {
    html.style.setProperty('--bg', 'transparent')
    document.body.style.backgroundImage = `url(${bg})`
    document.body.style.backgroundSize = 'cover'
    document.body.style.backgroundPosition = 'center'
    document.body.style.backgroundAttachment = 'fixed'
    document.body.style.backgroundRepeat = 'no-repeat'
  } else {
    html.style.setProperty('--bg', '')
    document.body.style.backgroundImage = ''
    document.body.style.backgroundSize = ''
    document.body.style.backgroundPosition = ''
    document.body.style.backgroundAttachment = ''
    document.body.style.backgroundRepeat = ''
  }
}, { immediate: true })
</script>
