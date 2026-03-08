<template>
  <div class="file-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <el-upload
            ref="uploadRef"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="false"
          >
            <el-button type="primary" icon="Upload">上传文件</el-button>
          </el-upload>
          <el-button type="danger" icon="Delete" :disabled="selectedFiles.length === 0" @click="handleBatchDelete">
            批量删除
          </el-button>
        </div>
      </template>

      <el-table :data="fileList" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="文件名" min-width="200">
          <template #default="{ row }">
            <el-icon :size="20" style="vertical-align: middle; margin-right: 8px;">
              <component :is="getFileIcon(row.fileName)" />
            </el-icon>
            {{ row.fileName }}
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="类型" width="100" />
        <el-table-column prop="createBy" label="上传人" width="120" />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Download" @click="handleDownload(row)">下载</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-show="pagination.total > 0"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getFileList"
          @current-change="getFileList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Picture, VideoCamera, Headset, Grid, Folder } from '@element-plus/icons-vue'
import request from '@/utils/request'

const uploadRef = ref(null)
const fileList = ref([])
const loading = ref(false)
const selectedFiles = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const uploadUrl = computed(() => {
  return request.defaults.baseURL + '/common/upload'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('access_token')
  return { 'Authorization': token }
})

// 获取文件列表
const getFileList = () => {
  loading.value = true
  request.get('/system/file/list', { params: {
    pageNum: pagination.current,
    pageSize: pagination.size
  }}).then(res => {
    fileList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 获取文件图标
const getFileIcon = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase()
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext)) return Picture
  if (['mp4', 'avi', 'mov'].includes(ext)) return VideoCamera
  if (['mp3', 'wav', 'flac'].includes(ext)) return Headset
  if (['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt'].includes(ext)) return Document
  return Folder
}

// 上传成功
const handleUploadSuccess = (response) => {
  ElMessage.success('上传成功')
  getFileList()
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  selectedFiles.value = selection.map(item => item.fileId)
}

// 下载文件
const handleDownload = (row) => {
  request.get(`/common/download/${row.fileName}`, { responseType: 'blob' }).then(response => {
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', row.fileName)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  })
}

// 删除文件
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除文件 "${row.fileName}" 吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/file/${row.fileId}`).then(() => {
      ElMessage.success('删除成功')
      getFileList()
    })
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${selectedFiles.value.length} 个文件吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/file/batch/' + selectedFiles.value.join(',')).then(() => {
      ElMessage.success('删除成功')
      getFileList()
    })
  })
}

onMounted(() => {
  getFileList()
})
</script>

<style lang="scss" scoped>
.file-container {
  padding: 20px;

  .header-actions {
    display: flex;
    gap: 10px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>