<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待办回收站</span>
          <el-button type="danger" plain @click="handleClear">
            <el-icon><Delete /></el-icon> 清空回收站
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="recycleList">
        <el-table-column label="序号" type="index" width="80" align="center" />
        <el-table-column label="待办标题" prop="todoTitle" :show-overflow-tooltip="true" min-width="200" />
        <el-table-column label="删除人" prop="deleteByName" width="120" />
        <el-table-column label="删除时间" prop="deleteTime" width="180">
          <template #default="{ row }">
            {{ row.deleteTime ? parseTime(row.deleteTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isRecover === 1 ? 'success' : 'info'">
              {{ row.isRecover === 1 ? '已恢复' : '未恢复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.isRecover === 0"
              type="success"
              link
              icon="RefreshLeft"
              @click="handleRecover(row)"
            >
              恢复
            </el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">
              永久删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup name="TodoRecycleBin">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, RefreshLeft } from '@element-plus/icons-vue'
import { listRecycleBin, recoverTodo, deletePermanently, clearRecycleBin } from '@/api/system/todo'
import { parseTime } from '@/utils/mp'

const { proxy } = getCurrentInstance()

const recycleList = ref([])
const loading = ref(true)
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10
})

// 获取回收站列表
function getList() {
  loading.value = true
  listRecycleBin(queryParams.value).then(res => {
    recycleList.value = res.data.records
    total.value = res.data.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 恢复待办
function handleRecover(row) {
  ElMessageBox.confirm('确认恢复待办 "' + row.todoTitle + '" 吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    recoverTodo(row.recycleId).then(() => {
      ElMessage.success('恢复成功')
      getList()
    })
  })
}

// 永久删除
function handleDelete(row) {
  ElMessageBox.confirm('确认永久删除 "' + row.todoTitle + '" 吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(() => {
    deletePermanently(row.recycleId).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  })
}

// 清空回收站
function handleClear() {
  ElMessageBox.confirm('确认清空回收站吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(() => {
    clearRecycleBin().then(() => {
      ElMessage.success('清空成功')
      getList()
    })
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.app-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>