<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="not-found-image">
        <svg viewBox="0 0 500 300" width="400" height="240">
          <!-- 背景 -->
          <rect fill="#f0f2f5" width="500" height="300" rx="20" />
          
          <!-- 404 文字 -->
          <text x="250" y="120" font-size="80" font-weight="bold" fill="#409EFF" text-anchor="middle">404</text>
          
          <!-- 小人 -->
          <circle cx="180" cy="200" r="30" fill="#667eea" />
          <line x1="180" y1="230" x2="180" y2="270" stroke="#667eea" stroke-width="8" stroke-linecap="round" />
          <line x1="180" y1="250" x2="150" y2="280" stroke="#667eea" stroke-width="8" stroke-linecap="round" />
          <line x1="180" y1="250" x2="210" y2="280" stroke="#667eea" stroke-width="8" stroke-linecap="round" />
          <line x1="180" y1="230" x2="150" y2="250" stroke="#667eea" stroke-width="8" stroke-linecap="round" />
          <line x1="180" y1="230" x2="210" y2="250" stroke="#667eea" stroke-width="8" stroke-linecap="round" />
          
          <!-- 放大镜 -->
          <circle cx="320" cy="200" r="40" fill="none" stroke="#764ba2" stroke-width="8" />
          <line x1="348" y1="228" x2="380" y2="260" stroke="#764ba2" stroke-width="8" stroke-linecap="round" />
          
          <!-- 装饰元素 -->
          <circle cx="50" cy="50" r="20" fill="#e6a23c" opacity="0.5" />
          <circle cx="450" cy="80" r="30" fill="#f56c6c" opacity="0.3" />
          <circle cx="420" cy="250" r="25" fill="#67c23a" opacity="0.4" />
        </svg>
      </div>
      
      <h1 class="not-found-title">页面未找到</h1>
      <p class="not-found-desc">抱歉，您访问的页面不存在或已被移除</p>
      
      <div class="not-found-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon> 返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style lang="scss" scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.not-found-content {
  text-align: center;
  background: #fff;
  padding: 60px 80px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);

  .not-found-image {
    margin-bottom: 30px;
  }

  .not-found-title {
    font-size: 32px;
    color: #333;
    margin-bottom: 15px;
  }

  .not-found-desc {
    font-size: 16px;
    color: #999;
    margin-bottom: 30px;
  }

  .not-found-actions {
    display: flex;
    gap: 15px;
    justify-content: center;
  }
}
</style>
