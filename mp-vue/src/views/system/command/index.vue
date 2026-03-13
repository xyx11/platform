<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :model="queryParams" :inline="true" label-width="80px">
        <el-form-item label="命令类型">
          <el-input
            v-model="queryParams.commandType"
            placeholder="请输入命令类型"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="执行状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">
            搜索
          </el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="Delete"
            @click="handleClear"
          >
            清空记录
          </el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table
        v-loading="loading"
        :data="commandList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="命令 ID" align="center" prop="commandId" width="180" />
        <el-table-column label="命令类型" align="center" prop="commandType" width="150" />
        <el-table-column label="命令内容" align="center" prop="commandContent" :show-overflow-tooltip="true" />
        <el-table-column label="执行结果" align="center" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行时长 (ms)" align="center" prop="executeTime" width="120" />
        <el-table-column label="执行时间" align="center" prop="executeTimeStr" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.executeTimeStr) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              type="primary"
              link
              icon="View"
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <!-- 执行命令对话框 -->
    <el-dialog
      v-model="executeVisible"
      title="执行命令"
      width="800px"
      append-to-body
      @close="resetExecuteForm"
    >
      <el-form ref="executeFormRef" :model="executeForm" label-width="100px">
        <el-form-item label="命令类型" required>
          <el-select v-model="executeForm.commandType" style="width: 100%">
            <el-option label="Shell 命令" value="shell" />
            <el-option label="Python 脚本" value="python" />
            <el-option label="SQL 查询" value="sql" />
          </el-select>
        </el-form-item>
        <el-form-item label="命令内容" required>
          <el-input
            v-model="executeForm.command"
            type="textarea"
            :rows="8"
            placeholder="请输入命令内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExecuteSubmit">执行</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="命令执行详情"
      width="900px"
      append-to-body
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="命令 ID">
          {{ currentCommand.commandId }}
        </el-descriptions-item>
        <el-descriptions-item label="命令类型">
          {{ currentCommand.commandType }}
        </el-descriptions-item>
        <el-descriptions-item label="命令内容" :span="2">
          {{ currentCommand.commandContent }}
        </el-descriptions-item>
        <el-descriptions-item label="执行结果" :span="2">
          <el-tag :type="currentCommand.status === 1 ? 'success' : 'danger'">
            {{ currentCommand.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行时长">
          {{ currentCommand.executeTime }} ms
        </el-descriptions-item>
        <el-descriptions-item label="执行时间">
          {{ parseTime(currentCommand.executeTimeStr) }}
        </el-descriptions-item>
        <el-descriptions-item label="执行者" :span="2">
          {{ currentCommand.executeName }}
        </el-descriptions-item>
        <el-descriptions-item label="命令结果" :span="2">
          <el-input
            v-model="currentCommand.commandResult"
            type="textarea"
            :rows="6"
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item v-if="currentCommand.errorMsg" label="错误信息" :span="2">
          <el-input
            v-model="currentCommand.errorMsg"
            type="textarea"
            :rows="4"
            readonly
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="Command">
import { ref, reactive, getCurrentInstance } from 'vue'
import { listCommand, getCommand, executeCommand, delCommand, batchDelCommand, clearCommand } from '@/api/system/command'
import { parseTime } from '@/utils/mp'

const { proxy } = getCurrentInstance()

const commandList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const executeVisible = ref(false)
const detailVisible = ref(false)
const currentCommand = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  commandType: undefined,
  status: undefined
})

const executeForm = reactive({
  commandType: 'shell',
  command: ''
})

// 查询命令列表
function getList() {
  loading.value = true
  listCommand(queryParams).then(response => {
    commandList.value = response.data.records
    total.value = response.data.total
    loading.value = false
  })
}

// 搜索按钮操作
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

// 重置按钮操作
function resetQuery() {
  proxy.resetForm('queryParams')
  resetExecuteForm()
  getList()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.commandId)
  multiple.value = !selection.length
}

// 执行命令按钮操作
function handleExecute() {
  executeVisible.value = true
}

// 执行命令提交
function handleExecuteSubmit() {
  executeCommand(executeForm).then(response => {
    proxy.$modal.msgSuccess('命令执行成功')
    executeVisible.value = false
    getList()
  }).catch(() => {
    executeVisible.value = false
  })
}

// 重置执行表单
function resetExecuteForm() {
  executeForm.commandType = 'shell'
  executeForm.command = ''
}

// 详情按钮操作
function handleDetail(row) {
  getCommand(row.commandId).then(response => {
    currentCommand.value = response.data
    detailVisible.value = true
  })
}

// 删除按钮操作
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除命令记录 "' + row.commandId + '"？').then(() => {
    return delCommand(row.commandId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// 批量删除
function handleBatchDelete() {
  proxy.$modal.confirm('是否确认删除选中的 ' + ids.value.length + ' 条记录？').then(() => {
    return batchDelCommand(ids.value)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// 清空记录
function handleClear() {
  proxy.$modal.confirm('是否确认清空所有命令执行记录？').then(() => {
    return clearCommand()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('清空成功')
  }).catch(() => {})
}

getList()
</script>

<style scoped lang="scss">
.app-container {
  :deep(.el-card__body) {
    padding: 20px;
  }
}
</style>