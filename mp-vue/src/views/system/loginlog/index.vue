<template>
  <div class="login-log-container">
    <el-card>
      <el-form :model="queryParams" inline>
        <el-form-item label="用户账号">
          <el-input v-model="queryParams.username" placeholder="请输入用户账号" clearable />
        </el-form-item>
        <el-form-item label="登录 IP">
          <el-input v-model="queryParams.ip" placeholder="请输入登录 IP" clearable />
        </el-form-item>
        <el-form-item label="登录状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="header-actions">
          <el-button type="danger" icon="Delete" @click="handleClear">清空日志</el-button>
        </div>
      </template>

      <el-table :data="logList" border stripe v-loading="loading">
        <el-table-column prop="logId" label="访问 ID" width="180" />
        <el-table-column prop="username" label="用户账号" width="120" />
        <el-table-column prop="ip" label="登录地址" width="140" />
        <el-table-column prop="location" label="登录地点" width="150" />
        <el-table-column prop="browser" label="浏览器" width="120" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="描述信息" min-width="150" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getLogList"
          @current-change="getLogList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const logList = ref([])
const loading = ref(false)

const queryParams = reactive({
  username: '',
  ip: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取登录日志列表
const getLogList = () => {
  loading.value = true
  request.get('/system/loginlog/list', { params: queryParams }).then(res => {
    logList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getLogList()
}

// 重置
const resetQuery = () => {
  queryParams.username = ''
  queryParams.ip = ''
  queryParams.status = null
  handleQuery()
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除登录日志 ID 为"${row.logId}"的记录吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/loginlog/' + row.logId).then(() => {
      ElMessage.success('删除成功')
      getLogList()
    })
  })
}

// 清空日志
const handleClear = () => {
  ElMessageBox.confirm('确认清空所有登录日志吗？此操作不可恢复！', '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/loginlog/clear').then(() => {
      ElMessage.success('清空成功')
      getLogList()
    })
  })
}

onMounted(() => {
  getLogList()
})
</script>

<style lang="scss" scoped>
.login-log-container {
  .table-card {
    margin-top: 20px;

    .header-actions {
      display: flex;
      justify-content: flex-end;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>