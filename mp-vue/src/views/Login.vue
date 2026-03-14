<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧装饰区 -->
      <div class="login-banner">
        <div class="banner-content">
          <div class="banner-logo">
            <el-icon :size="64"><Platform /></el-icon>
          </div>
          <h1 class="banner-title">Micro Platform</h1>
          <p class="banner-desc">企业级微服务架构平台</p>
          <div class="banner-features">
            <div class="feature-item">
              <el-icon><DataLine /></el-icon>
              <span>分布式架构</span>
            </div>
            <div class="feature-item">
              <el-icon><Shield /></el-icon>
              <span>安全可靠</span>
            </div>
            <div class="feature-item">
              <el-icon><Cpu /></el-icon>
              <span>高性能</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区 -->
      <div class="login-form-wrapper">
        <div class="login-box">
          <div class="login-header">
            <h2 class="login-title">欢迎登录</h2>
            <p class="login-subtitle">请使用您的账号登录系统</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                clearable
                prefix-icon="User"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                clearable
                prefix-icon="Lock"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="captchaCode">
              <div class="captcha-input">
                <el-input
                  v-model="loginForm.captchaCode"
                  placeholder="验证码"
                  size="large"
                  clearable
                  prefix-icon="Key"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
                <div class="captcha-img-wrapper" @click="getCaptcha">
                  <img :src="captchaImg" alt="验证码" class="captcha-img" />
                  <span class="captcha-refresh">点击刷新</span>
                </div>
              </div>
            </el-form-item>

            <el-form-item class="form-options">
              <el-checkbox v-model="loginForm.rememberMe" size="default">记住密码</el-checkbox>
              <el-link type="primary" :underline="false" class="forgot-link">忘记密码？</el-link>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                <span v-if="!loading">登 录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="login-footer">
          <p>© 2024 Micro Platform. All rights reserved.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Platform, DataLine, Cpu } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  captchaCode: '',
  captchaKey: '',
  rememberMe: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 获取验证码
const getCaptcha = () => {
  request.get('/auth/captcha').then(res => {
    loginForm.captchaKey = res.data.captchaKey
    captchaImg.value = res.data.captchaImg
  }).catch(err => {
    // 获取验证码失败
    captchaImg.value = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjQwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9IiNmNWY3ZmEiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iIzkwOTM5OSIgZm9udC1zaXplPSIxMyI+点击刷新</text></svg>'
  })
}

// 登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      const loginData = {
        '@class': 'com.micro.platform.auth.dto.LoginRequest',
        ...loginForm,
        userAgent: navigator.userAgent
      }
      try {
        const res = await request.post('/auth/login', loginData)
        const { accessToken, username, nickname, avatar, userId } = res.data
        localStorage.setItem('access_token', accessToken)
        localStorage.setItem('user_info', JSON.stringify({
          userId,
          username,
          nickname,
          avatar
        }))
        ElMessage.success('登录成功')
        // 延迟跳转，确保消息显示完成
        setTimeout(() => {
          router.replace({ path: '/dashboard' })
        }, 300)
      } catch (error) {
        // 登录失败
        // 静默刷新验证码，不显示错误提示
        request.get('/auth/captcha').then(res => {
          loginForm.captchaKey = res.data.captchaKey
          captchaImg.value = res.data.captchaImg
        }).catch(() => {
          // 静默失败，使用占位图
          captchaImg.value = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjQwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9IiNmNWY3ZmEiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iIzkwOTM5OSIgZm9udC1zaXplPSIxMyI+点击刷新</text></svg>'
        })
        loginForm.captchaCode = ''
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  getCaptcha()
})
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1e2832 0%, #2d3a47 100%);
  overflow: hidden;
}

.login-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  max-width: 1200px;
  min-height: 680px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: scale-in 0.5s ease;
}

@keyframes scale-in {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.login-banner {
  flex: 1;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 50%, #3d7ab5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    animation: rotate 30s linear infinite;
  }

  @keyframes rotate {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }

  .banner-content {
    position: relative;
    z-index: 1;
    text-align: center;
    color: #fff;

    .banner-logo {
      width: 120px;
      height: 120px;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10px);
      border-radius: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 30px;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    }

    .banner-title {
      font-size: 36px;
      font-weight: 700;
      margin: 0 0 12px 0;
      letter-spacing: 2px;
    }

    .banner-desc {
      font-size: 16px;
      opacity: 0.8;
      margin: 0 0 50px 0;
    }

    .banner-features {
      display: flex;
      flex-direction: column;
      gap: 20px;
      align-items: center;

      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 24px;
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(10px);
        border-radius: 12px;
        font-size: 15px;
        transition: all 0.3s ease;

        &:hover {
          background: rgba(255, 255, 255, 0.25);
          transform: translateX(10px);
        }

        .el-icon {
          font-size: 20px;
        }
      }
    }
  }
}

.login-form-wrapper {
  width: 480px;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.login-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 50px;

  .login-header {
    margin-bottom: 40px;

    .login-title {
      font-size: 28px;
      font-weight: 600;
      color: #1a1a1a;
      margin: 0 0 10px 0;
    }

    .login-subtitle {
      font-size: 14px;
      color: #888;
      margin: 0;
    }
  }

  .login-form {
    .el-form-item {
      margin-bottom: 24px;
    }

    .el-input {
      :deep(.el-input__wrapper) {
        background-color: #f7f8fa;
        border: 1px solid #e8eaed;
        border-radius: 8px;
        padding: 12px 16px;
        transition: all 0.3s ease;
        box-shadow: none;

        &:hover {
          border-color: #dcdfe6;
        }

        &.is-focus {
          border-color: #1e80ff;
          box-shadow: 0 0 0 2px rgba(30, 128, 255, 0.15);
        }
      }

      .el-input__inner {
        font-size: 14px;
        color: #333;
      }
    }

    .captcha-input {
      display: flex;
      gap: 12px;
      align-items: center;

      .el-input {
        flex: 1;
      }

      .captcha-img-wrapper {
        width: 110px;
        height: 44px;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;
        position: relative;
        flex-shrink: 0;
        transition: all 0.3s ease;
        border: 1px solid #e8eaed;

        &:hover {
          transform: scale(1.02);
          border-color: #1e80ff;
        }

        .captcha-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .captcha-refresh {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(30, 128, 255, 0.9);
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover .captcha-refresh {
          opacity: 1;
        }
      }
    }

    .form-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 32px;

      .forgot-link {
        font-size: 14px;
      }
    }

    .login-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 500;
      background: linear-gradient(180deg, #1e80ff 0%, #1967d9 100%);
      border: none;
      border-radius: 8px;
      transition: all 0.3s ease;
      box-shadow: 0 4px 12px rgba(30, 128, 255, 0.3);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(30, 128, 255, 0.4);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}

.login-footer {
  padding: 24px;
  text-align: center;
  border-top: 1px solid #f0f0f0;

  p {
    font-size: 12px;
    color: #999;
    margin: 0;
  }
}

/* 响应式 */
@media (max-width: 900px) {
  .login-wrapper {
    flex-direction: column;
    max-width: 480px;
    min-height: auto;
  }

  .login-banner {
    display: none;
  }

  .login-form-wrapper {
    width: 100%;
  }
}
</style>