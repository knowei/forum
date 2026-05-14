<template>
  <div class="category-manage">
    <div class="category-manage__header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="showDialog(null)">添加分类</el-button>
    </div>

    <el-table :data="list" v-loading="loading" empty-text="暂无分类">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" width="180" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDialog(row)">编辑</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '添加分类'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" maxlength="200" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = ref({ name: '', description: '', sort: 0 })

async function fetchList() {
  loading.value = true
  try {
    list.value = await getAdminCategoryList()
  } finally {
    loading.value = false
  }
}

function showDialog(row) {
  if (row) {
    editingId.value = row.id
    form.value = { name: row.name, description: row.description || '', sort: row.sort ?? 0 }
  } else {
    editingId.value = null
    form.value = { name: '', description: '', sort: 0 }
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) {
    ElMessage.warning('名称不能为空')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, form.value)
      ElMessage.success('分类已更新')
    } else {
      await createCategory(form.value)
      ElMessage.success('分类已创建')
    }
    dialogVisible.value = false
    await fetchList()
  } catch {
    // interceptor handles message
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateCategory(row.id, { ...row, status: newStatus })
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    await fetchList()
  } catch {
    // interceptor handles message
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除「${row.name}」吗？`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCategory(row.id)
    ElMessage.success('分类已删除')
    await fetchList()
  } catch {
    // cancelled
  }
}

onMounted(fetchList)
</script>

<style scoped>
.category-manage__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.category-manage__header h2 {
  margin: 0;
  font-size: 18px;
}
</style>
