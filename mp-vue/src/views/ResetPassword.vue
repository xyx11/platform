<template>
  <div class="reset-password-container">
    <div class="reset-password-wrapper">
      <!-- 左侧装饰区 -->
      <div class="banner">
        <div class="banner-content">
          <div class="banner-logo">
            <el-icon :size="64"><Platform /></el-icon>
          </div>
          <h1 class="banner-title">Micro Platform</h1>
          <p class="banner-desc">企业级微服务架构平台</p>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-wrapper">
        <div class="form-box">
          <div class="form-header">
            <h2 class="form-title">忘记密码</h2>
            <p class="form-subtitle">通过手机号重置您的密码</p>
          </div>

          <el-form
            ref="resetFormRef"
            :model="resetForm"
            :rules="resetRules"
            class="reset-form"
          >
            <!-- 步骤 1：验证手机号 -->
            <el-form-item prop="phone">
              <div class="input-with-button">
                <el-input
                  v-model="resetForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  clearable
                  maxlength="11"
                >
                  <template #prefix>
                    <el-icon><Phone /></el-icon>
                  </template>
                </el-input>
                <el-button
                  type="primary"
                  :disabled="phoneCountdown > 0"
                  :loading="sendingCode"
                  @click="sendCode"
                >
                  {{ phoneCountdown > 0 ? `${phoneCountdown}秒后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item prop="code">
              <el-input
                v-model="resetForm.code"
                placeholder="请输入短信验证码"
                size="large"
                clearable
                maxlength="6"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="newPassword">
              <el-input
                v-model="resetForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                size="large"
                show-password
                clearable
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="resetForm.confirmPassword"
                type="password"
                placeholder="请确认新密码"
                size="large"
                show-password
                clearable
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="reset-btn"
                @click="handleReset"
              >
                重置密码
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <el-link type="primary" @click="goToLogin">返回登录</el-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Platform, Phone, Lock, Key } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const resetFormRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const phoneCountdown = ref(0)

const resetForm = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const resetRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 发送验证码
const sendCode = () => {
  if (!resetForm.phone || !/^1[3-9]\d{9}$/.test(resetForm.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }

  sendingCode.value = true
  request.post('/auth/reset-password/code', { phone: resetForm.phone })
    .then(() => {
      ElMessage.success('验证码已发送')
      phoneCountdown.value = 60
      const timer = setInterval(() => {
        phoneCountdown.value--
        if (phoneCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    })
    .catch(() => {})
    .finally(() => {
      sendingCode.value = false
    })
}

// 重置密码
const handleReset = () => {
  resetFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      request.post('/auth/reset-password', {
        phone: resetForm.phone,
        code: resetForm.code,
        newPassword: resetForm.newPassword
      })
        .then(() => {
          ElMessage.success('密码重置成功')
          setTimeout(() => {
            router.replace({ path: '/login' })
          }, 1500)
        })
        .catch(() => {})
        .finally(() => {
          loading.value = false
        })
    }
  })
}

// 返回登录
const goToLogin = () => {
  router.push({ path: '/login' })
}
</script>

<style lang="scss" scoped>
.reset-password-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1e2832 0%, #2d3a47 100%);
  overflow: hidden;
}

.reset-password-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  max-width: 1200px;
  min-height: 500px;
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

.banner {
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
  }
}

.form-wrapper {
  width: 480px;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.form-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 50px;

  .form-header {
    margin-bottom: 40px;

    .form-title {
      font-size: 28px;
      font-weight: 600;
      color: #1a1a1a;
      margin: 0 0 10px 0;
    }

    .form-subtitle {
      font-size: 14px;
      color: #888;
      margin: 0;
    }
  }

  .reset-form {
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

    .input-with-button {
      display: flex;
      gap: 10px;
      align-items: center;

      .el-input {
        flex: 1;
      }

      .el-button {
        flex-shrink: 0;
        width: 130px;
      }
    }

    .reset-btn {
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

  .form-footer {
    margin-top: 20px;
    text-align: center;
  }
}

/* 响应式 */
@media (max-width: 900px) {
  .reset-password-wrapper {
    flex-direction: column;
    max-width: 480px;
    min-height: auto;
  }

  .banner {
    display: none;
  }

  .form-wrapper {
    width: 100%;
  }
}
</style>