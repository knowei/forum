<template>
  <div class="editor-shell">
    <div :id="editorId"></div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, watch } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  height: {
    type: Number,
    default: 520
  },
  placeholder: {
    type: String,
    default: '请输入资源正文内容'
  }
})

const emit = defineEmits(['update:modelValue'])
const editorId = `vditor-${Math.random().toString(36).slice(2)}`
let editor = null
let syncing = false

onMounted(() => {
  editor = new Vditor(editorId, {
    height: props.height,
    mode: 'sv',
    theme: 'classic',
    placeholder: props.placeholder,
    cache: { enable: false },
    toolbar: [
      'emoji', 'headings', 'bold', 'italic', 'strike', '|',
      'quote', 'line', 'list', 'ordered-list', 'check', '|',
      'code', 'inline-code', 'table', 'link', '|',
      'undo', 'redo', '|', 'edit-mode', 'outline', 'preview', 'fullscreen'
    ],
    after() {
      editor.setValue(props.modelValue || '')
    },
    input(value) {
      syncing = true
      emit('update:modelValue', value)
      queueMicrotask(() => {
        syncing = false
      })
    }
  })
})

watch(() => props.modelValue, (value) => {
  if (editor && !syncing && value !== editor.getValue()) {
    editor.setValue(value || '')
  }
})

onBeforeUnmount(() => {
  editor?.destroy()
  editor = null
})
</script>
