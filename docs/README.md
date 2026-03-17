# Micro Platform Docs

基于 VitePress 的文档站点。

## 快速开始

### 安装依赖

```bash
cd docs
npm install
```

### 启动开发服务器

```bash
npm run docs:dev
```

访问 http://localhost:5173

### 构建生产版本

```bash
npm run docs:build
```

### 预览构建结果

```bash
npm run docs:preview
```

## 目录结构

```
docs/
├── .vitepress/         # VitePress 配置
│   └── config.ts       # 站点配置
├── index.md            # 首页
├── guide/              # 指南文档
│   ├── introduction.md
│   ├── quickstart.md
│   ├── architecture.md
│   ├── modules.md
│   └── workflow/
│       ├── overview.md
│       ├── designer.md
│       ├── form-config.md
│       └── task.md
└── api/                # API 文档
    ├── overview.md
    ├── workflow-form.md
    ├── workflow.md
    └── form-definition.md
```

## 添加新文档

1. 在 `guide/` 或 `api/` 目录下创建 `.md` 文件
2. 在 `.vitepress/config.ts` 中添加 sidebar 配置
3. 重启开发服务器

## 部署

构建后的静态文件位于 `docs/.vitepress/dist/`，可以部署到任何静态托管服务：

- GitHub Pages
- Vercel
- Netlify
- 公司内部服务器
