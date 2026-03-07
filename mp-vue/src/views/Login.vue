<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>Micro Platform</h2>
        <p>微服务架构平台</p>
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
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="captchaCode">
          <div class="captcha-input">
            <el-input
              v-model="loginForm.captchaCode"
              placeholder="请输入验证码"
              prefix-icon="Key"
              size="large"
              clearable
              style="width: 60%"
            />
            <img
              :src="captchaImg"
              alt="验证码"
              class="captcha-img"
              @click="getCaptcha"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
  })
}

// 登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      const loginData = {
        ...loginForm,
        userAgent: navigator.userAgent
      }
      request
        .post('/auth/login', loginData)
        .then(res => {
          const { accessToken, tokenType } = res.data
          localStorage.setItem('access_token', `${tokenType} ${accessToken}`)
          ElMessage.success('登录成功')
          router.push('/')
        })
        .catch(() => {
          getCaptcha()
          loginForm.captchaCode = ''
        })
        .finally(() => {
          loading.value = false
        })
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
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.1);

  .login-header {
    text-align: center;
    margin-bottom: 30px;

    h2 {
      font-size: 28px;
      color: #333;
      margin-bottom: 10px;
    }

    p {
      font-size: 14px;
      color: #999;
    }
  }

  .login-form {
    .captcha-input {
      display: flex;
      align-items: center;
      gap: 10px;

      .captcha-img {
        width: 130px;
        height: 40px;
        cursor: pointer;
        border-radius: 4px;
      }
    }

    .login-btn {
      width: 100%;
    }
  }
}
</style>
