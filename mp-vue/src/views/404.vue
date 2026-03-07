<template>
  <div class="not-found-container">
    <h1>404</h1>
    <p>页面未找到</p>
    <el-button @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}
.not-found-container h1 {
  font-size: 72px;
  margin-bottom: 20px;
}
</style>