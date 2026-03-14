<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 用户信息卡片 -->
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-info">
            <div class="avatar">
              <el-upload
                action="#"
                :http-request="uploadAvatar"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
              >
                <el-avatar :size="100" :src="userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                <div class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                </div>
              </el-upload>
            </div>
            <div class="user-details">
              <h3>{{ userInfo.nickname || userInfo.username }}</h3>
              <p class="dept">{{ userInfo.deptName || '暂无部门' }}</p>
              <p class="role">
                <el-tag v-for="role in userInfo.roles" :key="role.roleId" size="small">
                  {{ role.roleName }}
                </el-tag>
              </p>
            </div>
          </div>
          <el-divider />
          <div class="user-stats">
            <div class="stat-item">
              <el-icon><User /></el-icon>
              <span>用户 ID</span>
              <p>{{ userInfo.userId }}</p>
            </div>
            <div class="stat-item">
              <el-icon><Phone /></el-icon>
              <span>手机号</span>
              <p>{{ userInfo.phone || '-' }}</p>
            </div>
            <div class="stat-item">
              <el-icon><Message /></el-icon>
              <span>邮箱</span>
              <p>{{ userInfo.email || '-' }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 编辑表单 -->
      <el-col :span="16">
        <el-card class="edit-card">
          <template #header>
            <span>个人信息</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <div class="input-with-button">
                <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
                <el-button
                  type="primary"
                  :disabled="phoneCountdown > 0"
                  :loading="sendingPhoneCode"
                  @click="sendPhoneCode"
                >
                  {{ phoneCountdown > 0 ? `${phoneCountdown}秒后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="验证码" prop="phoneCode">
              <el-input v-model="form.phoneCode" placeholder="请输入短信验证码" maxlength="6" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <div class="input-with-button">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
                <el-button
                  type="primary"
                  :disabled="emailCountdown > 0"
                  :loading="sendingEmailCode"
                  @click="sendEmailCode"
                >
                  {{ emailCountdown > 0 ? `${emailCountdown}秒后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="邮箱验证码" prop="emailCode">
              <el-input v-model="form.emailCode" placeholder="请输入邮件验证码" maxlength="6" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitForm">保存</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="password-card">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Phone, Message, Camera } from '@element-plus/icons-vue'
import request from '@/utils/request'

const formRef = ref(null)
const passwordFormRef = ref(null)

const userInfo = ref({
  userId: null,
  username: '',
  nickname: '',
  avatar: '',
  deptName: '',
  roles: [],
  phone: '',
  email: '',
  gender: 1
})

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  phoneCode: '',
  emailCode: '',
  gender: 1
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证码倒计时
const phoneCountdown = ref(0)
const emailCountdown = ref(0)
const sendingPhoneCode = ref(false)
const sendingEmailCode = ref(false)

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const getUserInfo = () => {
  request.get('/system/user/profile').then(res => {
    userInfo.value = res.data
    Object.assign(form, res.data)
  }).catch(() => {})
}

// 上传头像
const uploadAvatar = (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  
  request({
    url: '/system/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    ElMessage.success('头像上传成功')
    userInfo.value.avatar = res.data
    form.avatar = res.data
  }).catch(() => {})
}

// 上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 发送手机验证码
const sendPhoneCode = () => {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  
  sendingPhoneCode.value = true
  request.post('/system/user/sms/code', { phone: form.phone, type: 'change_phone' })
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
      sendingPhoneCode.value = false
    })
}

// 发送邮箱验证码
const sendEmailCode = () => {
  if (!form.email || !/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(form.email)) {
    ElMessage.error('请输入正确的邮箱')
    return
  }
  
  sendingEmailCode.value = true
  request.post('/system/user/email/code', { email: form.email, type: 'change_email' })
    .then(() => {
      ElMessage.success('验证码已发送')
      emailCountdown.value = 60
      const timer = setInterval(() => {
        emailCountdown.value--
        if (emailCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    })
    .catch(() => {})
    .finally(() => {
      sendingEmailCode.value = false
    })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const data = { ...form }
      
      // 如果手机号修改了，需要验证
      if (form.phone && form.phone !== userInfo.value.phone) {
        if (!form.phoneCode) {
          ElMessage.error('请输入手机验证码')
          return
        }
        data.phone = form.phoneCode
        // 调用修改手机号接口
        request.put('/system/user/profile/phone', { phone: form.phone, code: form.phoneCode })
          .then(() => {
            ElMessage.success('手机号修改成功')
          })
        return
      }
      
      // 如果邮箱修改了，需要验证
      if (form.email && form.email !== userInfo.value.email) {
        if (!form.emailCode) {
          ElMessage.error('请输入邮箱验证码')
          return
        }
        // 调用修改邮箱接口
        request.put('/system/user/profile/email', { email: form.email, code: form.emailCode })
          .then(() => {
            ElMessage.success('邮箱修改成功')
          })
        return
      }
      
      // 其他信息修改
      delete data.phoneCode
      delete data.emailCode
      delete data.phone
      delete data.email
      
      request.put('/system/user/profile', data).then(() => {
        ElMessage.success('保存成功')
        getUserInfo()
      })
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, userInfo.value)
}

// 修改密码
const changePassword = () => {
  passwordFormRef.value.validate(valid => {
    if (valid) {
      request.put('/system/user/profile/password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      }).then(() => {
        ElMessage.success('密码修改成功，请重新登录')
        ElMessageBox.confirm('密码修改成功，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('access_token')
          localStorage.removeItem('user_info')
          location.href = '/login'
        })
        passwordFormRef.value?.resetFields()
      })
    }
  })
}

onMounted(() => {
  getUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;

  .avatar {
    position: relative;
    display: inline-block;

    .avatar-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      cursor: pointer;

      .el-icon {
        color: #fff;
        font-size: 24px;
      }
    }

    &:hover .avatar-overlay {
      opacity: 1;
    }
  }

  .input-with-button {
    display: flex;
    align-items: center;
    gap: 10px;

    .el-input {
      flex: 1;
    }

    .el-button {
      flex-shrink: 0;
      width: 120px;
    }
  }
}

  .user-card {
    .user-info {
      text-align: center;
      padding: 20px 0;

      .avatar {
        margin-bottom: 20px;
      }

      .user-details {
        h3 {
          margin: 10px 0;
          font-size: 20px;
          color: #333;
        }

        .dept {
          color: #999;
          font-size: 14px;
          margin: 5px 0;
        }

        .role {
          margin-top: 10px;

          .el-tag {
            margin: 0 5px;
          }
        }
      }
    }

    .user-stats {
      display: flex;
      justify-content: space-around;
      padding: 20px 0;

      .stat-item {
        text-align: center;

        .el-icon {
          font-size: 24px;
          color: #409EFF;
          margin-bottom: 10px;
        }

        span {
          display: block;
          color: #999;
          font-size: 12px;
          margin-bottom: 5px;
        }

        p {
          margin: 0;
          color: #333;
          font-weight: 500;
        }
      }
    }
  }

  .edit-card,
  .password-card {
    margin-bottom: 20px;
  }
}
</style>
