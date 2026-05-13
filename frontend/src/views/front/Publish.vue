<template>
  <div class="page-card">
    <h2>{{ editId ? '编辑内容' : '发布内容' }}</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="类型">
        <el-radio-group v-model="form.type">
          <el-radio :value="0">资源（含下载链接）</el-radio>
          <el-radio :value="1">文章（纯内容）</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="封面图片">
        <div class="cover-upload">
          <el-upload
            :show-file-list="false"
            accept="image/jpeg,image/png,image/gif,image/webp"
            :before-upload="handleCoverUpload"
          >
            <template #default>
              <div v-if="!form.cover" class="cover-upload__placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传封面</span>
              </div>
              <img v-else :src="form.cover" class="cover-upload__preview" />
            </template>
          </el-upload>
          <el-button v-if="form.cover" type="danger" link size="small" @click="removeCover">移除封面</el-button>
        </div>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="200" placeholder="请输入标题" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 240px">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="简短描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="正文内容" prop="content">
        <MarkdownEditor v-model="form.content" :height="560" />
      </el-form-item>

      <template v-if="form.type === 0">
        <el-form-item label="下载链接" prop="downloadLink">
          <el-input v-model="form.downloadLink" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="提取码">
          <el-input v-model="form.extractCode" />
        </el-form-item>
        <el-form-item label="解压码">
          <el-input v-model="form.unzipPassword" />
        </el-form-item>
      </template>

      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="tag1,tag2" />
      </el-form-item>
      <el-form-item>
        <el-button v-if="editId" type="primary" :loading="loading" @click="handleUpdate">保存修改</el-button>
        <template v-else>
          <el-button type="primary" :loading="loading" @click="handleSubmit(0)">提交审核</el-button>
          <el-button :loading="loading" @click="handleSubmit(3)">保存草稿</el-button>
        </template>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MarkdownEditor from '@/components/editor/MarkdownEditor.vue'
import { Plus } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import { createResource, updateResource, uploadImage, getResourceDetail } from '@/api/resource'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const categories = ref([])
const editId = ref(null)
const formRef = ref(null)
const form = reactive({
  type: 0,
  categoryId: undefined,
  title: '',
  description: '',
  content: '',
  cover: '',
  downloadLink: '',
  extractCode: '',
  unzipPassword: '',
  tags: ''
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 200, message: '标题不能超过200个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  downloadLink: [
    { pattern: /^https?:\/\/.+/, message: '请输入正确的http链接', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
  ]
}

async function handleCoverUpload(file) {
  try {
    const { url } = await uploadImage(file, 'cover')
    form.cover = url
    ElMessage.success('封面上传成功')
  } catch {
    ElMessage.error('封面上传失败')
  }
  return false
}

function removeCover() {
  form.cover = ''
}

async function fetchCategories() {
  categories.value = await getCategoryList()
}

async function validateBeforeSubmit(status) {
  if (!formRef.value) return false

  // Basic field validation (title, categoryId)
  try {
    await formRef.value.validate()
  } catch {
    return false
  }

  // Draft can skip content & download validation
  if (status === 3) return true

  // Non-draft: validate content
  if (!form.content.trim()) {
    ElMessage.warning('请输入正文内容')
    return false
  }

  // Resource type: validate download link
  if (form.type === 0) {
    if (!form.downloadLink.trim()) {
      ElMessage.warning('请输入下载链接')
      return false
    }
    if (!/^https?:\/\//.test(form.downloadLink.trim())) {
      ElMessage.warning('下载链接需以 http:// 或 https:// 开头')
      return false
    }
  }

  return true
}

async function handleSubmit(status) {
  if (!(await validateBeforeSubmit(status))) return
  loading.value = true
  try {
    await createResource({ ...form, status })
    if (status === 3) {
      ElMessage.success('草稿保存成功')
      router.push('/profile')
    } else {
      ElMessage.success(form.type === 1 ? '文章已提交审核' : '资源已提交审核')
      router.push('/')
    }
  } finally {
    loading.value = false
  }
}

async function handleUpdate() {
  if (!(await validateBeforeSubmit(0))) return
  loading.value = true
  try {
    await updateResource(editId.value, { ...form })
    ElMessage.success('修改已保存，重新提交审核')
    router.push('/profile')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchCategories()
  const id = route.query.id
  if (id) {
    editId.value = Number(id)
    const data = await getResourceDetail(id)
    form.type = data.type ?? 0
    form.categoryId = data.categoryId
    form.title = data.title || ''
    form.description = data.description || ''
    form.content = data.content || ''
    form.cover = data.cover || ''
    form.downloadLink = data.downloadLink || ''
    form.extractCode = data.extractCode || ''
    form.unzipPassword = data.unzipPassword || ''
    form.tags = data.tags || ''
  }
})
</script>

<style scoped>
.cover-upload {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.cover-upload__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 240px;
  height: 160px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.cover-upload__placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.cover-upload__preview {
  width: 240px;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.el-form-item :deep(.editor-shell) {
  width: 100%;
}
</style>
