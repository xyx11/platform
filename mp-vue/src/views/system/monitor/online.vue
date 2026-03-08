<template>
  <div class="online-user-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>在线用户列表</span>
          <el-input
            v-model="searchText"
            placeholder="搜索用户名"
            style="width: 200px"
            clearable
            prefix-icon="Search"
            @input="handleSearch"
          />
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        :default-sort="{ prop: 'loginTime', order: 'descending' }"
      >
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="ipaddr" label="登录 IP" min-width="140" />
        <el-table-column prop="loginLocation" label="登录地点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="browser" label="浏览器" min-width="150" show-overflow-tooltip />
        <el-table-column prop="os" label="操作系统" min-width="120" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" min-width="180" sortable>
          <template #default="{ row }">
            {{ formatTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              link
              @click="handleKickout(row)"
            >
              强退
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const onlineUsers = ref([])

const tableData = computed(() => {
  if (!searchText.value) return onlineUsers.value
  return onlineUsers.value.filter(user =>
    user.username.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const loadOnlineUsers = () => {
  loading.value = true
  request.get('/monitor/online/list').then(res => {
    onlineUsers.value = res.data || []
    total.value = onlineUsers.value.length
  }).catch(err => {
    console.error('加载在线用户失败:', err)
  }).finally(() => {
    loading.value = false
  })
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = () => {
  loadOnlineUsers()
}

const handleCurrentChange = () => {
  loadOnlineUsers()
}

const handleKickout = (row) => {
  ElMessageBox.confirm(
    `确定要强制退出用户 "${row.username}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    request.delete(`/monitor/online/${row.tokenId}`).then(() => {
      ElMessage.success('强退成功')
      loadOnlineUsers()
    }).catch(err => {
      console.error('强退失败:', err)
    })
  }).catch(() => {})
}

onMounted(() => {
  loadOnlineUsers()
})
</script>

<style lang="scss" scoped>
.online-user-container {
  padding: 0;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 500;
  }

  .pagination-wrapper {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>