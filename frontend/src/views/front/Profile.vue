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

        <el-tab-pane label="个人设置" name="settings">
          <div class="settings-form">
            <h3 class="settings-section-title">基本资料</h3>
            <el-form :model="settingsForm" label-width="100px">
              <el-form-item label="昵称">
                <el-input v-model="settingsForm.nickname" maxlength="50" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="settingsForm.email" maxlength="100" />
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="settingsForm.bio" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="介绍一下自己…" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="savingInfo" @click="handleSaveInfo">保存</el-button>
              </el-form-item>
            </el-form>

            <el-divider />

            <h3 class="settings-section-title">背景图片</h3>
            <div class="bg-setting">
              <div class="bg-preview" :style="{ backgroundImage: bgPreviewStyle }">
                <div v-if="!userStore.userInfo?.bgImage" class="bg-preview__empty">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                  <span>未设置</span>
                </div>
              </div>
              <div class="bg-actions">
                <el-upload
                  :show-file-list="false"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  :before-upload="handleBgUpload"
                >
                  <el-button size="small" type="primary">上传图片</el-button>
                </el-upload>
                <el-button v-if="userStore.userInfo?.bgImage" size="small" type="danger" plain @click="handleRemoveBg">移除背景</el-button>
                <span class="bg-hint">建议尺寸 1920×1080，仅支持 jpg/png/webp</span>
              </div>
            </div>

            <el-divider />

            <h3 class="settings-section-title">修改密码</h3>
            <el-form :model="passwordForm" label-width="100px">
              <el-form-item label="原密码">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="passwordForm.newPassword" type="password" show-password minlength="6" maxlength="20" />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="savingPassword" @click="handleSavePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
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
import { updateUserAvatar, updateUserInfo, updatePassword, updateBgImage } from '@/api/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const activeTab = ref('resources')
const resourceFilter = ref('all')

const collections = ref([])
const colLoading = ref(false)

const settingsForm = ref({ nickname: '', email: '' })
const savingInfo = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const savingPassword = ref(false)

const bgPreviewStyle = computed(() => {
  return userStore.userInfo?.bgImage ? `url(${userStore.userInfo.bgImage})` : 'none'
})

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

async function handleBgUpload(file) {
  try {
    const { url } = await uploadImage(file, 'bg')
    await updateBgImage(url)
    await userStore.fetchUserInfo()
    ElMessage.success('背景图片更新成功')
  } catch {
    ElMessage.error('背景图片上传失败')
  }
  return false
}

async function handleRemoveBg() {
  try {
    await updateBgImage('')
    await userStore.fetchUserInfo()
    ElMessage.success('背景图片已移除')
  } catch {
    ElMessage.error('移除失败')
  }
}

watch(activeTab, (tab) => {
  if (tab === 'settings') {
    settingsForm.value = {
      nickname: userStore.userInfo?.nickname || '',
      email: userStore.userInfo?.email || '',
      bio: userStore.userInfo?.bio || ''
    }
  }
  if (tab === 'collections' && !collections.value.length) {
    fetchCollections()
  }
})

async function handleSaveInfo() {
  if (!settingsForm.value.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  savingInfo.value = true
  try {
    await updateUserInfo({
      nickname: settingsForm.value.nickname.trim(),
      email: settingsForm.value.email.trim(),
      bio: settingsForm.value.bio?.trim() || ''
    })
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } catch {
    // interceptor handles message
  } finally {
    savingInfo.value = false
  }
}

async function handleSavePassword() {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码不能少于6位')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  savingPassword.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch {
    // interceptor handles message
  } finally {
    savingPassword.value = false
  }
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

onMounted(fetchMyResources)
</script>

<style scoped>
.settings-form {
  max-width: 480px;
}

.settings-section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
}

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

.bg-setting {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bg-preview {
  width: 100%;
  height: 140px;
  border-radius: 10px;
  background-size: cover;
  background-position: center;
  background-color: var(--bg-hover, #f3f4f6);
  border: 1px solid var(--border-light, #e5e7eb);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.bg-preview__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--text-muted, #9ca3af);
  font-size: 13px;
}

.bg-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.bg-hint {
  font-size: 12px;
  color: var(--text-muted, #9ca3af);
  margin-left: 4px;
}

@media (max-width: 480px) {
  .settings-form {
    max-width: 100%;
  }
  .bg-preview {
    height: 100px;
  }
}
</style>
