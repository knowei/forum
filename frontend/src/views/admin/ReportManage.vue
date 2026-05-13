<template>
  <div class="panel-card">
    <div class="table-header">
      <h2>资源举报</h2>
      <el-button @click="fetchList">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" empty-text="暂无举报">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="reason" label="举报原因" min-width="240" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
            {{ row.status === 0 ? '待处理' : '已处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleNote" label="处理备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="举报时间" width="170">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button type="primary" link @click="handleResolve(row)">标记已处理</el-button>
          </template>
          <el-tag v-else size="small" type="success">已处理</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportList, handleReport } from '@/api/report'

const loading = ref(false)
const list = ref([])

function formatDate(value) {
  return value ? value.replace('T', ' ') : '-'
}

async function fetchList() {
  loading.value = true
  try {
    list.value = await getReportList()
  } finally {
    loading.value = false
  }
}

async function handleResolve(row) {
  try {
    const { value } = await ElMessageBox.prompt('处理备注（可选）', '标记已处理', {
      inputType: 'textarea',
      inputPlaceholder: '输入处理说明'
    })
    await handleReport(row.id, { status: 1, handleNote: value || '' })
    ElMessage.success('已标记处理')
    await fetchList()
  } catch {
    // cancelled
  }
}

onMounted(fetchList)
</script>
