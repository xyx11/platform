<template>
  <div class="online-user-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>在线用户管理</span>
          <div>
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
              style="width: 200px; margin-right: 10px;"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="onlineUserList" v-loading="loading" style="width: 100%">
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column prop="token" label="Token" show-overflow-tooltip />
        <el-table-column label="是否当前用户" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isCurrent ? 'success' : 'info'">
              {{ row.isCurrent ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="剩余时间 (秒)" width="120">
          <template #default="{ row }">
            {{ row.timeout > 0 ? row.timeout : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="180">
          <template #default="{ row }">
            {{ row.expireTime ? new Date(row.expireTime).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isCurrent"
              type="danger"
              size="small"
              @click="handleKickout(row)"
            >
              强制下线
            </el-button>
            <el-tag v-else type="info" size="small">当前用户</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-statistic title="在线用户数" :value="totalCount" />
      </div>
    </el-card>
  </div>
</template>

<script setup name="OnlineUser">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'

const loading = ref(false)
const totalCount = ref(0)
const onlineUserList = ref([])
const searchForm = reactive({
  username: ''
})

// 获取在线用户列表
const getOnlineUserList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/system/online-user/list',
      method: 'get',
      params: { username: searchForm.username.value }
    })
    onlineUserList.value = res.data || []
    totalCount.value = onlineUserList.value.length
  } catch (error) {
    ElMessage.error('获取在线用户列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  getOnlineUserList()
}

// 刷新
const handleRefresh = () => {
  searchForm.username = ''
  getOnlineUserList()
}

// 强制下线
const handleKickout = (row) => {
  ElMessageBox.confirm(
    `确定要强制用户（ID: ${row.userId}）下线吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/system/online-user/kickout/${row.userId}`,
        method: 'post'
      })
      ElMessage.success('强制下线成功')
      getOnlineUserList()
    } catch (error) {
      ElMessage.error('强制下线失败：' + error.message)
    }
  }).catch(() => {})
}

onMounted(() => {
  getOnlineUserList()
})
</script>

<style scoped>
.online-user-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>