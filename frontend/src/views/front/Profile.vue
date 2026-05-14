<template>
  <div class="profile-page">
    <section class="page-card profile-header">
      <div class="profile-avatar-wrap">
        <el-upload
          :show-file-list="false"
          accept="image/jpeg,image/png,image/gif,image/webp"
          :before-upload="handleAvatarUpload"
        >
          <el-avatar
            v-if="userStore.userInfo?.avatar"
            :src="userStore.userInfo.avatar"
            :size="80"
            class="profile-avatar"
          />
          <el-avatar v-else :size="80" class="profile-avatar">
            {{ userStore.userInfo?.nickname?.[0] || '?' }}
          </el-avatar>
          <div class="avatar-overlay">更换头像</div>
        </el-upload>
      </div>
      <div class="profile-info">
        <h2>{{ userStore.userInfo?.nickname }}</h2>
        <span class="profile-role">{{ userStore.userInfo?.role === 1 ? '管理员' : '普通用户' }}</span>
      </div>
    </section>

    <section class="page-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的资源" name="resources">
          <el-tabs v-model="resourceFilter" @tab-change="filterResources">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="草稿" name="3" />
            <el-tab-pane label="待审核" name="0" />
            <el-tab-pane label="已发布" name="1" />
            <el-tab-pane label="已驳回" name="2" />
          </el-tabs>

          <el-table :data="filteredList" v-loading="loading" empty-text="暂无资源">
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column label="类型" width="60">
              <template #default="{ row }">{{ row.type === 1 ? '文章' : '资源' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="举报" width="70">
              <template #default="{ row }">
                <el-tag v-if="row.reportCount > 0" type="danger" size="small">被举报</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="150">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" link @click="$router.push(`/resource/${row.id}`)">查看</el-button>
                <el-button type="warning" link @click="$router.push(`/publish?id=${row.id}`)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="collections">
          <el-table :data="collections" v-loading="colLoading" empty-text="暂无收藏">
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column label="类型" width="60">
              <template #default="{ row }">{{ row.type === 1 ? '文章' : '资源' }}</template>
            </el-table-column>
            <el-table-column label="作者" width="120">
              <template #default="{ row }">{{ row.authorNickname || '匿名' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="150">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link @click="$router.push(`/resource/${row.id}`)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyResources, getMyCollections, deleteResource } from '@/api/resource'
import { uploadImage } from '@/api/resource'
import { updateUserAvatar } from '@/api/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const activeTab = ref('resources')
const resourceFilter = ref('all')

const collections = ref([])
const colLoading = ref(false)

const filteredList = computed(() => {
  if (resourceFilter.value === 'all') return list.value
  return list.value.filter((item) => item.status === Number(resourceFilter.value))
})

function statusText(status) {
  if (status === 1) return '已发布'
  if (status === 0) return '待审核'
  if (status === 2) return '已驳回'
  if (status === 3) return '草稿'
  return '未知'
}

function statusType(status) {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  if (status === 2) return 'danger'
  if (status === 3) return 'info'
  return 'info'
}

function formatDate(value) {
  return value ? value.replace('T', ' ') : '-'
}

function filterResources() {
  // computed handles filtering
}

async function handleAvatarUpload(file) {
  try {
    const { url } = await uploadImage(file, 'avatar')
    await updateUserAvatar(url)
    await userStore.fetchUserInfo()
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('头像上传失败')
  }
  return false
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除「${row.title}」吗？删除后不可恢复。`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteResource(row.id)
    ElMessage.success('删除成功')
    await fetchMyResources()
  } catch {
    // cancelled
  }
}

async function fetchMyResources() {
  loading.value = true
  try {
    list.value = await getMyResources()
  } finally {
    loading.value = false
  }
}

async function fetchCollections() {
  colLoading.value = true
  try {
    collections.value = await getMyCollections()
  } finally {
    colLoading.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'collections' && !collections.value.length) {
    fetchCollections()
  }
})

onMounted(fetchMyResources)
</script>

<style scoped>
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
}

.profile-avatar-wrap {
  position: relative;
  border-radius: 50%;
  cursor: pointer;
}

.profile-avatar {
  display: block;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.2s;
}

.profile-avatar-wrap:hover .avatar-overlay {
  opacity: 1;
}

.profile-info h2 {
  margin: 0 0 4px;
  font-size: 20px;
}

.profile-role {
  color: #9ca3af;
  font-size: 14px;
}
</style>
