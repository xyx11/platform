<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

#app {
  overflow: hidden;
}

/* 确保布局组件正确显示 */
.el-container {
  height: 100%;
}

.el-aside {
  overflow-x: hidden;
}

.el-main {
  overflow-y: auto;
}
</style>