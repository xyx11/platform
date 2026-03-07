<template>
  <div class="upload-excel">
    <el-upload
      ref="uploadRef"
      drag
      :accept="accept"
      :before-upload="beforeUpload"
      :on-change="handleChange"
      :show-file-list="false"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        将文件拖到此处，或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 *.xlsx / *.xls / *.csv 文件，不超过 10MB
          <el-button link type="primary" @click.stop="downloadTemplate">
            下载模板
          </el-button>
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  modelValue: Boolean,
  type: {
    type: String,
    default: 'user'
  },
  accept: {
    type: String,
    default: '.xlsx,.xls,.csv'
  }
})

const emit = defineEmits(['success', 'error', 'update:modelValue'])

const uploadRef = ref(null)

const beforeUpload = (file) => {
  const isExcel = /\.(xlsx|xls|csv)$/.test(file.name)
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleChange = (uploadFile) => {
  const formData = new FormData()
  formData.append('file', uploadFile.raw)
  formData.append('type', props.type)

  request.post('/common/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => {
    ElMessage.success(`导入成功，共 ${res.data.count} 条数据`)
    emit('success', res.data)
    emit('update:modelValue', false)
  }).catch(err => {
    emit('error', err)
  })
}

const downloadTemplate = () => {
  request.get(`/common/template/${props.type}`, {
    responseType: 'blob'
  }).then(response => {
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${props.type}_template.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  })
}
</script>

<style lang="scss" scoped>
.upload-excel {
  width: 100%;

  .el-upload {
    width: 100%;
  }

  .el-icon--upload {
    font-size: 67px;
    color: #8c939d;
    margin: 40px 0 16px;
  }

  .el-upload__tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
  }
}
</style>
