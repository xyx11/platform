<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <span>在线用户</span>
      </template>

      <el-table :data="onlineUsers" style="width: 100%">
        <el-table-column prop="token" label="Token" />
        <el-table-column prop="deptName" label="部门" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="nickName" label="昵称" />
        <el-table-column prop="ipaddr" label="登录 IP" />
        <el-table-column prop="loginTime" label="登录时间" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              @click="handleForceLogout(row)"
            >
              强制下线
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOnlineList, forceLogout } from '@/api/monitor'

const onlineUsers = ref([])

const getList = async () => {
  try {
    const response = await getOnlineList()
    onlineUsers.value = response.data || []
  } catch (error) {
    ElMessage.error('获取在线用户列表失败')
  }
}

const handleForceLogout = (row) => {
  ElMessageBox.confirm(`确定要强制用户 "${row.nickName}" 下线吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await forceLogout(row.token)
      ElMessage.success('强制下线成功')
      getList()
    } catch (error) {
      ElMessage.error('强制下线失败')
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}
</style>