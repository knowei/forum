<template>
  <div class="admin-grid">
    <div class="stat-card">
      <div class="stat-card__icon stat-card__icon--blue">
        <el-icon :size="28" aria-hidden="true"><User /></el-icon>
      </div>
      <div class="stat-card__body">
        <div class="stat-card__value">{{ stats.userCount }}</div>
        <div class="stat-card__label">用户总数</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-card__icon stat-card__icon--green">
        <el-icon :size="28" aria-hidden="true"><Document /></el-icon>
      </div>
      <div class="stat-card__body">
        <div class="stat-card__value">{{ stats.resourceCount }}</div>
        <div class="stat-card__label">资源总数</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-card__icon stat-card__icon--orange">
        <el-icon :size="28" aria-hidden="true"><Clock /></el-icon>
      </div>
      <div class="stat-card__body">
        <div class="stat-card__value">{{ stats.pendingResourceCount }}</div>
        <div class="stat-card__label">待审核资源</div>
      </div>
    </div>
    <div class="stat-card stat-card--clickable" @click="showOnlineUsers = true">
      <div class="stat-card__icon stat-card__icon--purple">
        <el-icon :size="28" aria-hidden="true"><Avatar /></el-icon>
      </div>
      <div class="stat-card__body">
        <div class="stat-card__value">{{ stats.onlineCount }}</div>
        <div class="stat-card__label">当前在线</div>
      </div>
    </div>

    <el-dialog v-model="showOnlineUsers" title="当前在线用户" width="420px" @open="fetchOnlineUsers">
      <div v-if="onlineUsers.length" class="online-user-list">
        <div v-for="u in onlineUsers" :key="u.id" class="online-user-item">
          <el-avatar v-if="u.avatar" :src="u.avatar" :size="36" />
          <el-avatar v-else :size="36">{{ u.nickname?.[0] || '?' }}</el-avatar>
          <span class="online-user-name">{{ u.nickname }}</span>
          <span class="online-user-tag">在线</span>
        </div>
      </div>
      <el-empty v-else description="暂无用户在线" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { User, Document, Clock, Avatar } from '@element-plus/icons-vue'
import { getAdminStatistics, getOnlineUsers } from '@/api/admin'

const stats = reactive({ userCount: 0, resourceCount: 0, pendingResourceCount: 0, onlineCount: 0 })
const showOnlineUsers = ref(false)
const onlineUsers = ref([])

async function fetchOnlineUsers() {
  onlineUsers.value = await getOnlineUsers()
}

onMounted(async () => {
  Object.assign(stats, await getAdminStatistics())
})
</script>

<style scoped>
.admin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: transform 0.15s, box-shadow 0.15s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-card--clickable {
  cursor: pointer;
}

.stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  flex-shrink: 0;
}

.stat-card__icon--blue {
  background: #eff6ff;
  color: #3b82f6;
}

.stat-card__icon--green {
  background: #f0fdf4;
  color: #22c55e;
}

.stat-card__icon--orange {
  background: #fff7ed;
  color: #f97316;
}

.stat-card__icon--purple {
  background: #f5f3ff;
  color: #8b5cf6;
}

.stat-card__body {
  display: flex;
  flex-direction: column;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-card__label {
  font-size: 14px;
  color: #9ca3af;
  margin-top: 4px;
}

.online-user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.online-user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.online-user-item:last-child {
  border-bottom: none;
}

.online-user-name {
  flex: 1;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.online-user-tag {
  font-size: 12px;
  color: #22c55e;
  background: #f0fdf4;
  padding: 2px 10px;
  border-radius: 10px;
}
</style>
