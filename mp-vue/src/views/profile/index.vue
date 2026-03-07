<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 用户信息卡片 -->
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-info">
            <div class="avatar">
              <el-avatar :size="100" :src="userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
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
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
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
import { User, Phone, Message } from '@element-plus/icons-vue'
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
  gender: 1
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

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

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      request.put('/system/user/profile', form).then(() => {
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
