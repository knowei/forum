import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'github-markdown-css/github-markdown.css'
import '@/assets/styles/index.scss'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.mount('#app')
