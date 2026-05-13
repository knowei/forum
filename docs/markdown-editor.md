# 前端 Markdown 编辑器方案

## 推荐方案

### 方案一：Vditor (推荐)
**优点：**
- 国产开源，中文文档完善
- 支持所见即所得、即时渲染、分屏预览三种模式
- 内置图片上传、表情、代码高亮
- 支持数学公式、流程图、甘特图
- 移动端适配良好

**安装：**
```bash
npm install vditor
```

**使用示例：**
```vue
<template>
  <div id="vditor"></div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'

const props = defineProps({
  modelValue: String
})

const emit = defineEmits(['update:modelValue'])

let vditor = null

onMounted(() => {
  vditor = new Vditor('vditor', {
    height: 500,
    placeholder: '请输入资源详细内容...',
    theme: 'classic',
    mode: 'wysiwyg', // wysiwyg(所见即所得) / ir(即时渲染) / sv(分屏预览)
    value: props.modelValue,
    cache: {
      enable: false
    },
    toolbar: [
      'emoji', 'headings', 'bold', 'italic', 'strike', '|',
      'quote', 'line', 'list', 'ordered-list', 'check', '|',
      'code', 'inline-code', 'table', 'link', '|',
      'undo', 'redo', '|', 'edit-mode', 'outline', 'preview', 'fullscreen'
    ],
    after: () => {
      vditor.setValue(props.modelValue || '')
    },
    input: (value) => {
      emit('update:modelValue', value)
    }
  })
})
</script>
```

### 方案二：Mavon Editor
**优点：**
- Vue 专用 Markdown 编辑器
- 轻量级，易于集成
- 支持工具栏自定义

**安装：**
```bash
npm install mavon-editor
```

**使用示例：**
```vue
<template>
  <mavon-editor
    v-model="content"
    :toolbars="toolbars"
    @imgAdd="handleImageAdd"
  />
</template>

<script setup>
import { ref } from 'vue'
import { mavonEditor } from 'mavon-editor'
import 'mavon-editor/dist/css/index.css'

const content = ref('')

const toolbars = {
  bold: true,
  italic: true,
  header: true,
  underline: true,
  strikethrough: true,
  mark: true,
  superscript: true,
  subscript: true,
  quote: true,
  ol: true,
  ul: true,
  link: true,
  imagelink: true,
  code: true,
  table: true,
  fullscreen: true,
  readmodel: true,
  htmlcode: true,
  help: true,
  undo: true,
  redo: true,
  trash: true,
  save: false,
  navigation: true,
  alignleft: true,
  aligncenter: true,
  alignright: true,
  subfield: true,
  preview: true
}

const handleImageAdd = (pos, file) => {
  // 上传图片逻辑
}
</script>
```

### 方案三：Markdown-it + Textarea
**优点：**
- 最轻量，完全自定义
- 仅用于渲染，编辑器自己实现

**安装：**
```bash
npm install markdown-it highlight.js
```

**使用示例：**
```vue
<template>
  <div class="markdown-editor">
    <el-input
      v-model="content"
      type="textarea"
      :rows="20"
      placeholder="支持 Markdown 语法"
    />
    <div class="preview">
      <div v-html="renderedContent"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const md = new MarkdownIt({
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch (__) {}
    }
    return ''
  }
})

const content = ref('')

const renderedContent = computed(() => {
  return md.render(content.value)
})
</script>

<style scoped>
.markdown-editor {
  display: flex;
  gap: 20px;
}
.preview {
  flex: 1;
  border: 1px solid #dcdfe6;
  padding: 10px;
  border-radius: 4px;
}
</style>
```

## 渲染 Markdown 内容

### 在详情页渲染
```vue
<template>
  <div class="resource-detail">
    <h1>{{ resource.title }}</h1>
    <div class="markdown-body" v-html="renderedContent"></div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import 'github-markdown-css/github-markdown.css'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>'
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

const resource = ref({
  title: '',
  content: ''
})

const renderedContent = computed(() => {
  return md.render(resource.value.content || '')
})
</script>

<style>
.markdown-body {
  box-sizing: border-box;
  min-width: 200px;
  max-width: 980px;
  margin: 0 auto;
  padding: 45px;
}
</style>
```

## 推荐配置

### package.json 依赖
```json
{
  "dependencies": {
    "vditor": "^3.9.9",
    "markdown-it": "^13.0.2",
    "highlight.js": "^11.9.0",
    "github-markdown-css": "^5.5.0"
  }
}
```

### 完整的编辑器组件
```vue
<!-- components/MarkdownEditor.vue -->
<template>
  <div class="markdown-editor-wrapper">
    <div id="vditor"></div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, watch } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { uploadImage } from '@/api/upload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  height: {
    type: Number,
    default: 500
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  }
})

const emit = defineEmits(['update:modelValue'])

let vditor = null

onMounted(() => {
  vditor = new Vditor('vditor', {
    height: props.height,
    placeholder: props.placeholder,
    theme: 'classic',
    mode: 'wysiwyg',
    toolbar: [
      'emoji',
      'headings',
      'bold',
      'italic',
      'strike',
      '|',
      'line',
      'quote',
      'list',
      'ordered-list',
      'check',
      '|',
      'code',
      'inline-code',
      'insert-before',
      'insert-after',
      '|',
      'upload',
      'link',
      'table',
      '|',
      'undo',
      'redo',
      '|',
      'fullscreen',
      'edit-mode',
      'outline',
      'preview'
    ],
    upload: {
      url: '/api/upload/image',
      max: 5 * 1024 * 1024,
      accept: 'image/png, image/jpeg, image/jpg, image/gif',
      fieldName: 'file',
      format(files, responseText) {
        const response = JSON.parse(responseText)
        if (response.code === 200) {
          return JSON.stringify({
            msg: '',
            code: 0,
            data: {
              errFiles: [],
              succMap: {
                [files[0].name]: response.data.url
              }
            }
          })
        }
        return JSON.stringify({
          msg: response.message,
          code: 1,
          data: {
            errFiles: [files[0].name],
            succMap: {}
          }
        })
      }
    },
    cache: {
      enable: false
    },
    after: () => {
      vditor.setValue(props.modelValue || '')
    },
    input: (value) => {
      emit('update:modelValue', value)
    }
  })
})

watch(() => props.modelValue, (newVal) => {
  if (vditor && newVal !== vditor.getValue()) {
    vditor.setValue(newVal || '')
  }
})

onBeforeUnmount(() => {
  if (vditor) {
    vditor.destroy()
    vditor = null
  }
})
</script>

<style scoped>
.markdown-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}
</style>
```

### 使用编辑器组件
```vue
<!-- views/Publish.vue -->
<template>
  <el-form :model="form" label-width="100px">
    <el-form-item label="资源标题">
      <el-input v-model="form.title" />
    </el-form-item>
    
    <el-form-item label="简短描述">
      <el-input
        v-model="form.description"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
        placeholder="用于列表展示的简短描述"
      />
    </el-form-item>
    
    <el-form-item label="详细内容">
      <MarkdownEditor v-model="form.content" :height="600" />
    </el-form-item>
    
    <el-form-item label="下载链接">
      <el-input v-model="form.downloadLink" />
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" @click="handleSubmit">发布资源</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const form = ref({
  title: '',
  description: '',
  content: '',
  downloadLink: ''
})

const handleSubmit = () => {
  console.log(form.value)
}
</script>
```

## 总结

**推荐使用 Vditor**，理由：
1. 功能强大，开箱即用
2. 三种编辑模式适应不同用户习惯
3. 内置图片上传，无需额外处理
4. 中文文档完善，社区活跃
5. 渲染效果好，支持各种扩展语法

**前端依赖清单：**
```bash
npm install vditor markdown-it highlight.js github-markdown-css
```
