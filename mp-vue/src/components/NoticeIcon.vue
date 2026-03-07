<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notice-icon">
      <el-icon :size="20"><Bell /></el-icon>
    </el-badge>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="notice">通知公告 ({{ unreadCount }})</el-dropdown-item>
        <el-dropdown-item command="message">站内消息</el-dropdown-item>
        <el-dropdown-item command="todo">待办事项</el-dropdown-item>
        <el-dropdown-item divided command="clear">清空未读</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'

const router = useRouter()
const unreadCount = ref(3)

const handleCommand = (command) => {
  if (command === 'notice') {
    router.push('/system/notice')
  } else if (command === 'clear') {
    unreadCount.value = 0
  } else {
    // TODO: 实现其他功能
  }
}
</script>

<style scoped>
.notice-icon {
  cursor: pointer;
  padding: 0 10px;
}

.notice-icon:hover {
  background-color: #f5f5f5;
  border-radius: 4px;
}
</style>
