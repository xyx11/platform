# Maven 配置说明

## 环境信息

- **Maven 版本**: 3.9.13
- **安装路径**: /opt/homebrew/Cellar/maven/3.9.13
- **Java 版本**: 25.0.2
- **配置文件**: ~/.m2/settings.xml

## 配置内容

### 1. 镜像配置

已配置阿里云镜像加速 Maven 依赖下载：

| 镜像 ID | 作用 | URL |
|--------|------|-----|
| aliyun | 中央仓库 | https://maven.aliyun.com/repository/public |
| aliyun-google | Google 仓库 | https://maven.aliyun.com/repository/google |
| aliyun-gradle | Gradle 插件 | https://maven.aliyun.com/repository/gradle-plugin |

### 2. 项目配置

| 配置项 | 值 |
|--------|-----|
| JDK | 17 |
| 编码 | UTF-8 |
| 项目打包类型 | pom (多模块父工程) |

### 3. 模块列表

- mp-common - 公共模块
- mp-gateway - 网关模块
- mp-auth - 认证模块
- mp-system - 系统模块
- mp-generator - 代码生成器
- mp-job - 定时任务

## 常用命令

```bash
# 编译项目
mvn clean compile

# 编译并跳过测试
mvn clean compile -DskipTests

# 运行测试
mvn test

# 打包项目
mvn clean package

# 安装到本地仓库
mvn clean install

# 查看依赖树
mvn dependency:tree

# 查看有效配置
mvn help:effective-settings

# 查看项目有效 POM
mvn help:effective-pom
```

## 验证配置

```bash
# 验证 Maven 版本
/opt/homebrew/bin/mvn --version

# 验证配置生效
/opt/homebrew/bin/mvn help:effective-settings
```