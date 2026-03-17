# 开发快速参考

## 常用代码片段

### Controller 层

```java
@RestController
@RequestMapping("/system/user")
@Tag(name = "用户管理")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @GetMapping("/list")
    @Operation(summary = "查询用户列表")
    public Result<Page<UserVO>> list(UserParam param) {
        return Result.success(userService.pageUser(param));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<UserVO> get(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    public Result<Void> add(@RequestBody @Validated UserAddParam param) {
        userService.addUser(param);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改用户")
    public Result<Void> update(@RequestBody @Validated UserUpdateParam param) {
        userService.updateUser(param);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long[] ids) {
        userService.deleteByIds(ids);
        return Result.success();
    }
}
```

### Service 层

```java
@Service
public class SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户
     */
    public Page<UserVO> pageUser(UserParam param) {
        Page<User> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(param.getUserName()), User::getUserName, param.getUserName())
               .eq(param.getStatus() != null, User::getStatus, param.getStatus());
        return userMapper.selectPage(page, wrapper);
    }

    /**
     * 根据 ID 查询用户
     */
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    /**
     * 新增用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserAddParam param) {
        // 检查用户名是否存在
        if (existsUserName(param.getUserName())) {
            throw new BusinessException("用户名已存在");
        }

        // 构建实体
        User user = new User();
        BeanUtils.copyProperties(param, user);
        user.setPassword(passwordEncoder.encode(param.getPassword()));

        // 插入数据库
        userMapper.insert(user);
    }

    /**
     * 修改用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateParam param) {
        User user = userMapper.selectById(param.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        BeanUtils.copyProperties(param, user);
        userMapper.updateById(user);
    }

    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Long[] ids) {
        Arrays.asList(ids).forEach(id -> {
            if (existsUserName(id)) {
                throw new BusinessException("用户不存在");
            }
        });
        userMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 检查用户名是否存在
     */
    private boolean existsUserName(String userName) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
            .eq(User::getUserName, userName)) > 0;
    }

    /**
     * 转换 VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
```

### Entity 层

```java
@Data
@TableName("sys_user")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("nick_name")
    private String nickName;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("sex")
    private String sex;

    @TableField("avatar")
    private String avatar;

    @TableField("status")
    private String status;

    @TableField("dept_id")
    private Long deptId;

    @TableField(exist = false)
    private DeptVO dept;

    @TableField(exist = false)
    private List<RoleVO> roles;
}
```

### 参数校验

```java
@Data
public class UserAddParam {

    @NotBlank(message = "用户名不能为空", groups = AddGroup.class)
    @Size(min = 4, max = 20, message = "用户名长度必须在 4-20 之间")
    private String userName;

    @NotBlank(message = "密码不能为空", groups = AddGroup.class)
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotNull(message = "部门 ID 不能为空")
    private Long deptId;
}
```

### 异常处理

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统内部错误");
    }
}
```

### 缓存使用

```java
@Service
public class SysDictService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询字典数据（带缓存）
     */
    public List<DictData> getDictData(String dictType) {
        String key = "dict:" + dictType;

        // 1. 查询缓存
        List<DictData> cached = (List<DictData>) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        // 2. 查询数据库
        List<DictData> data = dictMapper.selectDictData(dictType);

        // 3. 写入缓存（30 分钟过期）
        redisTemplate.opsForValue().set(key, data, 30, TimeUnit.MINUTES);

        return data;
    }

    /**
     * 删除缓存
     */
    public void clearDictCache(String dictType) {
        String key = "dict:" + dictType;
        redisTemplate.delete(key);
    }
}
```

### 异步处理

```java
@Service
public class OperationLogService {

    @Autowired
    private OperationLogMapper logMapper;

    /**
     * 异步保存操作日志
     */
    @Async("taskExecutor")
    public void saveLogAsync(OperationLog log) {
        logMapper.insert(log);
    }

    /**
     * 异步发送邮件
     */
    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendEmailAsync(
            String to, String subject, String content) {
        boolean success = emailService.send(to, subject, content);
        return CompletableFuture.completedFuture(success);
    }
}
```

## 前端代码片段

### Vue 组件模板

```vue
<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.userName" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <el-row :gutter="10" class="mb8">
        <el-button type="primary" @click="handleAdd">新增</el-button>
        <el-button type="danger" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-row>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="nickName" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="UserManage">
import { listUser, getUser, addUser, updateUser, deleteUser } from '@/api/system/user'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const open = ref(false)
const title = ref('')
const multiple = ref(true)
const ids = ref([])
const dataList = ref([])
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userName: ''
})

const form = ref({})
const rules = {
  userName: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  nickName: [{ required: true, message: '昵称不能为空', trigger: 'blur' }]
}

/** 查询列表 */
function getList() {
  loading.value = true
  listUser(queryParams.value).then(res => {
    dataList.value = res.data.rows
    total.value = res.data.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryParamsForm')
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '新增用户'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const userId = row.userId || ids.value[0]
  getUser(userId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改用户'
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.userId) {
        updateUser(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addUser(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const userIds = row.userId || ids.value
  proxy.$modal.confirm('确认删除？').then(() => {
    deleteUser(userIds).then(() => {
      proxy.$modal.msgSuccess('删除成功')
      getList()
    })
  })
}

/** 重置表单 */
function reset() {
  form.value = {}
  proxy.resetForm('formRef')
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.userId)
  multiple.value = !selection.length
}
</script>
```

### API 调用封装

```javascript
// api/system/user.js
import request from '@/utils/request'

// 查询用户列表
export function listUser(query) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params: query
  })
}

// 查询用户详细
export function getUser(userId) {
  return request({
    url: '/system/user/' + userId,
    method: 'get'
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data: data
  })
}

// 修改用户
export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data: data
  })
}

// 删除用户
export function deleteUser(userId) {
  return request({
    url: '/system/user/' + userId,
    method: 'delete'
  })
}
```

## 工具类速查

### StringUtils

```java
StringUtils.isBlank("  ")         // true
StringUtils.isNotEmpty("hello")   // true
StringUtils.camelToUnderline("userName")  // user_name
StringUtils.underlineToCamel("user_name") // userName
StringUtils.toInt("123", 0)       // 123
StringUtils.join(list, ",")       // a,b,c
```

### Arith

```java
Arith.add(0.1, 0.2)      // 0.3
Arith.div(1.0, 3, 4)     // 0.3333
Arith.round(3.14159, 2)  // 3.14
Arith.percentage(50, 200) // 25.0
```

### JsonUtils

```java
JsonUtils.toJson(obj)              // 对象转 JSON
JsonUtils.fromJson(json, User.class)  // JSON 转对象
JsonUtils.toPrettyJson(obj)        // 格式化 JSON
```

### DateUtils

```java
DateUtils.now()                    // 当前时间
DateUtils.format(LocalDateTime.now())  // 格式化
DateUtils.addDays(now, 7)          // 7 天后
DateUtils.isToday(date)            // 是否今天
DateUtils.getRelativeTime(past)    // 相对时间
```

### ValidationUtils

```java
ValidationUtils.isValidPhone("13800138000")  // true
ValidationUtils.isValidEmail("test@example.com")  // true
ValidationUtils.isValidIdCard("...")  // 身份证验证
ValidationUtils.isValidPassword("Abc123456")  // 密码强度
```

## SQL 速查

### 基础查询

```sql
-- 分页查询
SELECT * FROM sys_user
ORDER BY create_time DESC
LIMIT 0, 10;

-- 条件查询
SELECT * FROM sys_user
WHERE status = 0
  AND dept_id = 100
  AND user_name LIKE '%test%';

-- 连表查询
SELECT u.*, d.dept_name
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE u.status = 0;
```

### 索引创建

```sql
-- 单列索引
CREATE INDEX idx_user_name ON sys_user(user_name);

-- 复合索引
CREATE INDEX idx_dept_status ON sys_user(dept_id, status);

-- 唯一索引
CREATE UNIQUE INDEX uk_user_name ON sys_user(user_name);
```

### 数据操作

```sql
-- 批量插入
INSERT INTO sys_user (user_name, status) VALUES
('user1', 0),
('user2', 0),
('user3', 1);

-- 批量更新
UPDATE sys_user SET status = 1 WHERE user_id IN (1, 2, 3);

-- 批量删除
DELETE FROM sys_user WHERE user_id IN (1, 2, 3);
```

## Git 命令速查

```bash
# 新建分支
git checkout -b feature/new-feature

# 切换分支
git checkout dev

# 查看状态
git status

# 添加文件
git add .

# 提交
git commit -m "feat: 新增功能"

# 推送
git push origin feature/new-feature

# 合并分支
git checkout dev
git merge feature/new-feature

# 查看日志
git log --oneline

# 撤销提交
git reset --soft HEAD~1

# 暂存修改
git stash

# 恢复暂存
git stash pop
```

## 常用命令

```bash
# 查看端口占用
lsof -i :8080

# 查看日志
tail -f logs/mp-system.log

# 查看进程
ps aux | grep java

# 重启服务
./restart.sh

# 查看 Docker 容器
docker ps

# 查看 Docker 日志
docker logs -f container-name

# Maven 清理打包
mvn clean package -DskipTests

# Maven 安装依赖
mvn install -DskipTests

# 查看 Node 版本
node -v

# 查看 npm 版本
npm -v
```