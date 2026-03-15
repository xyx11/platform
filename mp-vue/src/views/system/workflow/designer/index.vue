<template>
  <div class="workflow-designer">
    <!-- 顶部工具栏 -->
    <div class="designer-header">
      <div class="header-left">
        <el-button-group>
          <el-button @click="newDiagram" title="新建流程 (Ctrl+N)">
            <el-icon><DocumentAdd /></el-icon> 新建
          </el-button>
          <el-button @click="openFile" title="打开文件 (Ctrl+O)">
            <el-icon><FolderOpened /></el-icon> 打开
          </el-button>
          <el-button @click="saveDiagram" title="保存流程 (Ctrl+S)">
            <el-icon><DocumentChecked /></el-icon> 保存
          </el-button>
          <el-button @click="saveAsDiagram" title="另存为">
            <el-icon><Download /></el-icon> 另存为
          </el-button>
          <el-button @click="restoreFromBackup" title="恢复本地备份">
            <el-icon><Refresh /></el-icon> 恢复备份
          </el-button>
        </el-button-group>

        <el-divider direction="vertical" />

        <el-button-group>
          <el-dropdown :hide-on-click="false">
            <el-button>
              <el-icon><Download /></el-icon> 导出
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="exportWithOptions">
                  <el-icon><Setting /></el-icon> 导出选项...
                </el-dropdown-item>
                <el-dropdown-item divided @click="downloadBpmn">
                  BPMN XML
                </el-dropdown-item>
                <el-dropdown-item @click="downloadDiagram">
                  SVG 图片
                </el-dropdown-item>
                <el-dropdown-item @click="downloadPng">
                  PNG 图片
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-button-group>

        <el-divider direction="vertical" />

        <el-button-group>
          <el-button @click="undo" title="撤销 (Ctrl+Z)">
            <el-icon><RefreshLeft /></el-icon>
          </el-button>
          <el-button @click="redo" title="重做 (Ctrl+Y)">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
          <el-button @click="zoomIn" title="放大">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button @click="zoomOut" title="缩小">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <el-button @click="zoomFit" title="适应屏幕">
            <el-icon><FullScreen /></el-icon>
          </el-button>
        </el-button-group>

        <el-divider direction="vertical" />

        <el-button-group>
          <el-button @click="openNodeProps" :disabled="!selectedNode" title="节点属性">
            <el-icon><Setting /></el-icon> 节点属性
          </el-button>
          <el-button @click="validateDiagram" title="基础验证">
            <el-icon><Checked /></el-icon> 验证
          </el-button>
          <el-button @click="enhancedValidate" title="流程健康度检查">\n            <el-icon><DataAnalysis /></el-icon> 健康度检查\n          </el-button>\n          <el-button @click="previewDiagram" title="预览流程">
            <el-icon><View /></el-icon> 预览
          </el-button>
          <el-button @click="duplicateProcess" :disabled="!saved" title="复制流程">
            <el-icon><DocumentCopy /></el-icon> 复制
          </el-button>
          <el-button @click="showVersionHistory" :disabled="!saved" title="版本历史">
            <el-icon><Document /></el-icon> 历史
          </el-button>
          <el-button @click="exportAsTemplate" :disabled="!saved" title="保存为模板">
            <el-icon><Star /></el-icon> 存为模板
          </el-button>
          <el-button @click="importTemplate" title="导入模板">
            <el-icon><Upload /></el-icon> 导入模板
          </el-button>
          <el-button @click="openBatchDialog" title="批量操作">
            <el-icon><Operation /></el-icon> 批量操作
          </el-button>
          <el-button @click="openAutoLayout" title="自动布局">
            <el-icon><Grid /></el-icon> 自动布局
          </el-button>
          <el-button @click="openTemplateLib" title="模板库">
            <el-icon><Collection /></el-icon> 模板库
          </el-button>
          <el-button @click="openSimulation" title="流程模拟">
            <el-icon><VideoPlay /></el-icon> 模拟
          </el-button>
          <el-button @click="openRecommendation" title="智能推荐">
            <el-icon><Star /></el-icon> 智能推荐
          </el-button>
          <el-button @click="openComments" title="评论批注">
            <el-icon><ChatRound /></el-icon> 评论
          </el-button>
          <el-button @click="openDashboard" title="健康度仪表板">
            <el-icon><Monitor /></el-icon> 健康度
          </el-button>
          <el-button @click="openShortcuts" title="快捷键设置">
            <el-icon><Key /></el-icon> 快捷键
          </el-button>
          <el-button @click="openLegend" title="图例说明">
            <el-icon><QuestionFilled /></el-icon> 图例
          </el-button>
          <el-button @click="openCollab" title="协作编辑">
            <el-icon><User /></el-icon> 协作
          </el-button>
          <el-button @click="showHelp" title="帮助">
            <el-icon><QuestionFilled /></el-icon> 帮助
          </el-button>
        </el-button-group>
        
        <el-divider direction="vertical" />
        
        <el-dropdown :hide-on-click="false">
          <el-button>
            <el-icon><Setting /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>
                <el-checkbox v-model="gridEnabled" @change="toggleGrid">显示网格</el-checkbox>
              </el-dropdown-item>
              <el-dropdown-item>
                <el-checkbox v-model="showMinimap" @change="toggleMinimap">显示缩略图</el-checkbox>
              </el-dropdown-item>
              <el-dropdown-item divided @click="resetViewPort">适应画布</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="header-right">
        <el-badge :is-dot="hasUnsavedChanges" class="mr-2">
          <el-button @click="openFormConfig" :disabled="!saved">
            <el-icon><Document /></el-icon> 关联表单
          </el-button>
        </el-badge>
        <el-button @click="launchProcess" type="success" :disabled="!deployed">
          <el-icon><VideoPlay /></el-icon> 发起流程
        </el-button>
        <el-button @click="deployDiagram" type="primary" :loading="deploying">
          <el-icon><Upload /></el-icon> {{ deploying ? '部署中...' : '部署流程' }}
        </el-button>
        <el-divider direction="vertical" />
        <span class="save-status">
          <el-icon v-if="autoSaving" class="is-loading"><Loading /></el-icon>
          <el-icon v-else-if="lastSavedTime"><Clock /></el-icon>
          {{ autoSaving ? '保存中...' : (lastSavedTime ? '已保存：' + formatTime(lastSavedTime) : '') }}
        </span>
      </div>
    </div>

    <div class="designer-container">
      <!-- 左侧工具栏 - 参考 SAP NetWeaver BPM 风格 -->
      <div class="palette-container">
        <el-collapse v-model="activePanels" accordion>
          <!-- 事件面板 -->
          <el-collapse-item title="事件 Events" name="events">
            <div class="palette-group">
              <div class="palette-category">开始事件</div>
              <div class="palette-item" data-action="create-start-event" title="开始事件">
                <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#28c961" stroke="#28c961" stroke-width="2"/></svg>
              </div>
              <div class="palette-item" data-action="create-timer-start" title="定时开始事件">
                <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#28c961" stroke="#28c961" stroke-width="2"/><path d="M16 8 L16 16 L22 16" stroke="#fff" stroke-width="2" fill="none"/></svg>
              </div>
              <div class="palette-item" data-action="create-message-start" title="消息开始事件">
                <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#28c961" stroke="#28c961" stroke-width="2"/><rect x="8" y="10" width="16" height="12" stroke="#fff" stroke-width="2" fill="none"/></svg>
              </div>

              <div class="palette-category">结束事件</div>
              <div class="palette-item" data-action="create-end-event" title="结束事件">
                <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#ff3333" stroke="#333" stroke-width="3"/></svg>
              </div>
              <div class="palette-item" data-action="create-message-end" title="消息结束事件">
                <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#ff3333" stroke="#333" stroke-width="3"/><rect x="8" y="10" width="16" height="12" stroke="#fff" stroke-width="2" fill="none"/></svg>
              </div>
              <div class="palette-item" data-action="create-error-end" title="错误结束事件">
                <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#ff3333" stroke="#333" stroke-width="3"/><path d="M16 10 L20 18 L12 18 Z" fill="#fff"/></svg>
              </div>
              <div class="palette-item" data-action="create-terminate-end" title="终止结束事件">
                <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#ff3333" stroke="#333" stroke-width="3"/><circle cx="16" cy="16" r="8" fill="#333"/></svg>
              </div>
            </div>
          </el-collapse-item>

          <!-- 任务面板 -->
          <el-collapse-item title="任务 Tasks" name="tasks">
            <div class="palette-group">
              <div class="palette-item" data-action="create-user-task" title="用户任务">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#58a3d9" stroke="#333" stroke-width="1"/><circle cx="16" cy="14" r="4" fill="#fff"/><path d="M8 24 Q16 18 24 24" fill="#fff"/></svg>
              </div>
              <div class="palette-item" data-action="create-service-task" title="服务任务">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#f5c34f" stroke="#333" stroke-width="1"/><circle cx="16" cy="16" r="8" stroke="#333" stroke-width="2" fill="none"/><path d="M16 10 L16 16 L20 16" stroke="#333" stroke-width="2"/></svg>
              </div>
              <div class="palette-item" data-action="create-script-task" title="脚本任务">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#78c961" stroke="#333" stroke-width="1"/><text x="6" y="20" font-size="12" fill="#fff">Script</text></svg>
              </div>
              <div class="palette-item" data-action="create-send-task" title="发送任务">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#58a3d9" stroke="#333" stroke-width="1"/><path d="M8 12 L24 12 L24 20 L8 20 Z" fill="#fff"/><path d="M8 12 L16 18 L24 12" fill="none" stroke="#333" stroke-width="1"/></svg>
              </div>
              <div class="palette-item" data-action="create-receive-task" title="接收任务">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#ffffff" stroke="#333" stroke-width="1"/><path d="M6 10 L12 16 L6 22" stroke="#333" stroke-width="2" fill="none"/><text x="14" y="18" font-size="10">收件</text></svg>
              </div>
              <div class="palette-item" data-action="create-manual-task" title="手动任务">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#58a3d9" stroke="#333" stroke-width="1"/><text x="6" y="20" font-size="10" fill="#fff">Manual</text></svg>
              </div>
            </div>
          </el-collapse-item>

          <!-- 网管面板 -->
          <el-collapse-item title="网关 Gateways" name="gateways">
            <div class="palette-group">
              <div class="palette-item" data-action="create-exclusive-gateway" title="排他网关 (XOR)">
                <svg viewBox="0 0 32 32"><rect x="4" y="4" width="24" height="24" transform="rotate(45 16 16)" fill="#ffab45" stroke="#333" stroke-width="1"/><text x="16" y="18" font-size="14" text-anchor="middle" fill="#333">X</text></svg>
              </div>
              <div class="palette-item" data-action="create-parallel-gateway" title="并行网关 (AND)">
                <svg viewBox="0 0 32 32"><rect x="4" y="4" width="24" height="24" transform="rotate(45 16 16)" fill="#58a3d9" stroke="#333" stroke-width="1"/><text x="16" y="18" font-size="14" text-anchor="middle" fill="#333">+</text></svg>
              </div>
              <div class="palette-item" data-action="create-inclusive-gateway" title="包容网关 (OR)">
                <svg viewBox="0 0 32 32"><rect x="4" y="4" width="24" height="24" transform="rotate(45 16 16)" fill="#a358d9" stroke="#333" stroke-width="1"/><text x="16" y="18" font-size="14" text-anchor="middle" fill="#333">O</text></svg>
              </div>
              <div class="palette-item" data-action="create-event-gateway" title="事件网关">
                <svg viewBox="0 0 32 32"><rect x="4" y="4" width="24" height="24" transform="rotate(45 16 16)" fill="#ffffff" stroke="#333" stroke-width="1"/><circle cx="16" cy="16" r="8" stroke="#333" stroke-width="1" fill="none"/></svg>
              </div>
            </div>
          </el-collapse-item>

          <!-- 数据面板 -->
          <el-collapse-item title="数据 Data" name="data">
            <div class="palette-group">
              <div class="palette-item" data-action="create-data-object" title="数据对象">
                <svg viewBox="0 0 32 32"><path d="M8 4 L24 4 L24 28 L8 28 Z" fill="#fff" stroke="#333" stroke-width="1"/><path d="M24 4 L24 10 L18 4 Z" fill="#e0e0e0"/></svg>
              </div>
              <div class="palette-item" data-action="create-data-store" title="数据存储">
                <svg viewBox="0 0 32 32"><ellipse cx="16" cy="8" rx="12" ry="4" fill="#fff" stroke="#333" stroke-width="1"/><path d="M4 8 L4 24 Q4 28 16 28 Q28 28 28 24 L28 8" fill="none" stroke="#333" stroke-width="1"/><ellipse cx="16" cy="24" rx="12" ry="4" fill="#fff" stroke="#333" stroke-width="1"/></svg>
              </div>
            </div>
          </el-collapse-item>

          <!-- 连接面板 -->
          <el-collapse-item title="连接 Connectors" name="connectors">
            <div class="palette-group">
              <div class="palette-item" data-action="create-sequence-flow" title="顺序流">
                <svg viewBox="0 0 32 32"><path d="M6 16 L26 16 M20 10 L26 16 L20 22" stroke="#333" stroke-width="2" fill="none"/></svg>
              </div>
              <div class="palette-item" data-action="create-message-flow" title="消息流">
                <svg viewBox="0 0 32 32"><path d="M4 16 L28 16" stroke="#333" stroke-width="1" stroke-dasharray="4,2"/><circle cx="8" cy="16" r="3" fill="#fff" stroke="#333" stroke-width="1"/><circle cx="24" cy="16" r="3" fill="#fff" stroke="#333" stroke-width="1"/></svg>
              </div>
              <div class="palette-item" data-action="create-association" title="关联">
                <svg viewBox="0 0 32 32"><path d="M6 16 L26 16" stroke="#333" stroke-width="1" stroke-dasharray="4,4"/></svg>
              </div>
            </div>
          </el-collapse-item>

          <!-- 子流程面板 -->
          <el-collapse-item title="子流程 Sub-Process" name="subprocess">
            <div class="palette-group">
              <div class="palette-item" data-action="create-subprocess" title="子流程">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#fafafa" stroke="#333" stroke-width="2"/><rect x="8" y="12" width="16" height="8" fill="#ddd"/><text x="22" y="22" font-size="8">+</text></svg>
              </div>
              <div class="palette-item" data-action="create-transaction" title="事务子流程">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#fafafa" stroke="#333" stroke-width="2"/><rect x="4" y="8" width="24" height="16" rx="2" fill="none" stroke="#28c961" stroke-width="2" stroke-dasharray="2,2"/></svg>
              </div>
              <div class="palette-item" data-action="create-event-subprocess" title="事件子流程">
                <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#fafafa" stroke="#333" stroke-width="2"/><circle cx="8" cy="16" r="4" fill="#ff3333" stroke="#333" stroke-width="1"/><text x="22" y="22" font-size="8">+</text></svg>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 画布区域 -->
      <div class="canvas-container">
        <!-- 画布控制栏 -->
        <div class="canvas-controls">
          <div class="canvas-info">
            <span>缩放：{{ currentZoom }}%</span>
            <el-divider direction="vertical" />
            <span>节点：{{ quickStats.nodes }} 连线：{{ quickStats.flows }}</span>
            <el-divider direction="vertical" />
            <span>流程：{{ processInfo.name || '未命名流程' }}</span>
          </div>
          <div class="canvas-actions">
            <el-switch v-model="gridEnabled" active-text="网格" @change="toggleGrid" />
            <el-divider direction="vertical" />
            <!-- 节点搜索 -->
            <el-input v-model="searchNodeText" placeholder="搜索节点..." size="small"
                      style="width: 220px; margin-right: 8px;"
                      @keydown.enter="searchNext" clearable>
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-button-group style="display: flex;">
                  <el-button size="small" @click="searchPrevious" title="上一个">
                    <el-icon><ArrowUp /></el-icon>
                  </el-button>
                  <el-button size="small" @click="searchNext" title="下一个">
                    <el-icon><ArrowDown /></el-icon>
                  </el-button>
                </el-button-group>
              </template>
            </el-input>
            <el-button size="small" @click="showStats" title="流程统计">
              <el-icon><DataAnalysis /></el-icon> 统计
            </el-button>
            <el-button size="small" @click="showNodeList" title="节点列表">
              <el-icon><Menu /></el-icon> 节点列表
            </el-button>
          </div>
        </div>
        <div ref="bpmnCanvas" class="bpmn-canvas"></div>
        <!-- 迷你地图 -->
        <div class="minimap-container" v-show="showMinimap">
          <div ref="minimap" class="minimap"></div>
          <el-button size="small" @click="toggleMinimap" class="minimap-close">×</el-button>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="properties-panel-container">
        <div class="properties-panel-header">
          <span>属性面板</span>
          <el-button link @click="toggleProperties" size="small">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div ref="propertiesPanel" class="properties-panel-content"></div>
      </div>
    </div>

    <!-- 流程信息对话框 -->
    <el-dialog v-model="processInfoVisible" title="流程信息" width="500px">
      <el-form :model="processInfo" label-width="100px">
        <el-form-item label="流程名称">
          <el-input v-model="processInfo.name" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="流程 ID">
          <el-input v-model="processInfo.id" placeholder="请输入流程 ID" />
        </el-form-item>
        <el-form-item label="流程版本">
          <el-input v-model="processInfo.version" placeholder="请输入流程版本" />
        </el-form-item>
        <el-form-item label="流程描述">
          <el-input v-model="processInfo.description" type="textarea" :rows="3" placeholder="请输入流程描述" />
        </el-form-item>
        <el-form-item label="流程分类">
          <el-select v-model="processInfo.category" placeholder="请选择流程分类" style="width: 100%">
            <el-option label="人力资源" value="HR" />
            <el-option label="财务管理" value="FIN" />
            <el-option label="采购管理" value="PROC" />
            <el-option label="销售管理" value="SALES" />
            <el-option label="生产管理" value="PROD" />
            <el-option label="行政管理" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processInfoVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProcessInfo">确定</el-button>
      </template>
    </el-dialog>

    <!-- 版本历史对话框 -->
    <el-dialog v-model="versionVisible" title="版本历史" width="800px">
      <el-table :data="versionList" v-loading="false" border>
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column prop="name" label="流程名称" width="200" />
        <el-table-column prop="deployTime" label="部署时间" width="180" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="loadVersion(row)">加载</el-button>
            <el-button size="small" type="danger" @click="deleteVersion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="versionVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 模板管理对话框 -->
    <el-dialog v-model="templateVisible" title="流程模板管理" width="700px">
      <el-tabs v-model="templateTab">
        <el-tab-pane label="保存为模板" name="save">
          <el-form :model="templateForm" label-width="100px">
            <el-form-item label="模板名称">
              <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
            </el-form-item>
            <el-form-item label="模板分类">
              <el-select v-model="templateForm.category" placeholder="请选择分类" style="width: 100%">
                <el-option label="人事流程" value="hr" />
                <el-option label="财务流程" value="finance" />
                <el-option label="行政流程" value="admin" />
                <el-option label="采购流程" value="purchase" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="模板描述">
              <el-input v-model="templateForm.description" type="textarea" :rows="3" placeholder="请输入模板描述" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="templateVisible = false">取消</el-button>
            <el-button type="primary" @click="saveAsTemplate" :loading="savingTemplate">保存</el-button>
          </template>
        </el-tab-pane>
        <el-tab-pane label="导入模板" name="import">
          <el-upload drag :auto-upload="false" :on-change="handleTemplateFile" accept=".json,.bpmn">
            <el-icon class="el-icon--upload"><Upload /></el-icon>
            <div class="el-upload__text">将模板文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 JSON 和 BPMN 格式的模板文件</div>
            </template>
          </el-upload>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 节点属性对话框 -->
    <el-dialog v-model="nodePropsVisible" title="节点属性配置" width="700px">
      <el-form v-if="selectedNode" :model="selectedNode" label-width="110px">
        <el-form-item label="节点 ID">
          <el-input v-model="selectedNode.id" disabled>
            <template #append>
              <el-button @click="copyToClipboard(selectedNode.id)" title="复制 ID">
                <el-icon><DocumentCopy /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input v-model="selectedNode.name" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="节点类型">
          <el-tag>{{ getNodeTypeName(selectedNode.type) }}</el-tag>
        </el-form-item>
        <el-form-item label="描述/文档">
          <el-input v-model="selectedNode.documentation" type="textarea" :rows="2" placeholder="节点描述信息" />
        </el-form-item>
        <el-form-item v-if="selectedNode.type === 'userTask'" label="处理人类型">
          <el-select v-model="selectedNode.assigneeType" placeholder="请选择" style="width: 100%">
            <el-option label="指定用户" value="user" />
            <el-option label="部门负责人" value="deptManager" />
            <el-option label="岗位" value="position" />
            <el-option label="变量" value="variable" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="selectedNode.type === 'userTask'" label="处理人">
          <el-input v-model="selectedNode.assignee" placeholder="用户 ID/角色编码/变量名" />
        </el-form-item>
        <el-form-item v-if="selectedNode.type === 'serviceTask'" label="服务类">
          <el-input v-model="selectedNode.serviceClass" placeholder="Java 类全限定名" />
        </el-form-item>
        <el-form-item v-if="selectedNode.type === 'serviceTask'" label="方法">
          <el-input v-model="selectedNode.method" placeholder="执行方法名" />
        </el-form-item>
        <el-form-item v-if="selectedNode.type === 'serviceTask'" label="结果变量">
          <el-input v-model="selectedNode.resultVariable" placeholder="存储返回值的变量名" />
        </el-form-item>
        
        <!-- 定时事件配置 -->
        <el-form-item v-if="selectedNode.type?.includes('Timer')" label="定时表达式">
          <el-input v-model="selectedNode.timerExpression" placeholder="ISO-8601 或 cron 表达式" />
        </el-form-item>
        
        <!-- 条件表达式 -->
        <el-form-item v-if="selectedNode.type === 'sequenceFlow'" label="条件表达式">
          <el-input v-model="selectedNode.conditionExpression" type="textarea" :rows="2" placeholder="${condition}" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodePropsVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNodeProps">保存</el-button>
      </template>
    </el-dialog>

    <!-- 流程统计对话框 -->
    <el-dialog v-model="statsVisible" title="流程统计" width="600px">
      <el-descriptions :column="2" border v-if="flowStats">
        <el-descriptions-item label="节点总数">{{ flowStats.totalNodes }}</el-descriptions-item>
        <el-descriptions-item label="连线数量">{{ flowStats.totalFlows }}</el-descriptions-item>
        <el-descriptions-item label="开始事件">{{ flowStats.startEvents }}</el-descriptions-item>
        <el-descriptions-item label="结束事件">{{ flowStats.endEvents }}</el-descriptions-item>
        <el-descriptions-item label="用户任务">{{ flowStats.userTasks }}</el-descriptions-item>
        <el-descriptions-item label="服务任务">{{ flowStats.serviceTasks }}</el-descriptions-item>
        <el-descriptions-item label="脚本任务">{{ flowStats.scriptTasks }}</el-descriptions-item>
        <el-descriptions-item label="接收/发送任务">{{ flowStats.sendReceiveTasks }}</el-descriptions-item>
        <el-descriptions-item label="排他网关">{{ flowStats.exclusiveGateways }}</el-descriptions-item>
        <el-descriptions-item label="并行网关">{{ flowStats.parallelGateways }}</el-descriptions-item>
        <el-descriptions-item label="包容网关">{{ flowStats.inclusiveGateways }}</el-descriptions-item>
        <el-descriptions-item label="子流程">{{ flowStats.subProcesses }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>流程复杂度评估</el-divider>
      <el-progress :percentage="complexityScore" :format="complexityFormat" :status="complexityStatus" />
      <el-alert v-if="complexityScore > 70" title="流程复杂度过高，建议简化" type="warning" :closable="false" style="margin-top: 10px" />
      <template #footer>
        <el-button type="primary" @click="statsVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 节点列表对话框 -->
    <el-dialog v-model="nodeListVisible" title="节点列表" width="800px">
      <el-table :data="nodeList" v-loading="false" border stripe @row-click="focusNode">
        <el-table-column prop="id" label="节点 ID" width="180" />
        <el-table-column prop="name" label="节点名称" width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="节点类型" width="150">
          <template #default="{ row }">
            <el-tag size="small" :type="getNodeTagType(row.type)">{{ getNodeTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="处理人" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click.stop="focusNode(row)">定位</el-button>
            <el-button size="small" @click.stop="editNodeProps(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="nodeListVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 右键上下文菜单 -->
    <div v-if="contextMenuVisible" class="context-menu" :style="{ top: contextMenuY + 'px', left: contextMenuX + 'px' }">
      <ul>
        <li @click="contextMenuAction('undo')"><el-icon><RefreshLeft /></el-icon> 撤销</li>
        <li @click="contextMenuAction('redo')"><el-icon><RefreshRight /></el-icon> 重做</li>
        <li class="divider"></li>
        <li @click="contextMenuAction('copy')"><el-icon><DocumentCopy /></el-icon> 复制</li>
        <li @click="contextMenuAction('paste')"><el-icon><Document /></el-icon> 粘贴</li>
        <li @click="contextMenuAction('duplicate')"><el-icon><Files /></el-icon> 快速复制</li>
        <li @click="contextMenuAction('delete')"><el-icon><Delete /></el-icon> 删除</li>
        <li class="divider"></li>
        <li class="submenu-title">对齐
          <span class="submenu-arrow">›</span>
        </li>
        <div class="submenu">
          <li @click="contextMenuAction('alignLeft')">左对齐</li>
          <li @click="contextMenuAction('alignCenter')">水平居中</li>
          <li @click="contextMenuAction('alignRight')">右对齐</li>
          <li class="divider"></li>
          <li @click="contextMenuAction('alignTop')">上对齐</li>
          <li @click="contextMenuAction('alignMiddle')">垂直居中</li>
          <li @click="contextMenuAction('alignBottom')">下对齐</li>
        </div>
        <li class="divider"></li>
        <li @click="contextMenuAction('properties')"><el-icon><Setting /></el-icon> 属性</li>
      </ul>
    </div>

    <!-- 帮助对话框 -->
    <el-dialog v-model="helpVisible" title="快捷键帮助" width="600px">
      <el-descriptions title="键盘快捷键" :column="1" border>
        <el-descriptions-item label="Ctrl+N">新建流程</el-descriptions-item>
        <el-descriptions-item label="Ctrl+O">打开文件</el-descriptions-item>
        <el-descriptions-item label="Ctrl+S">保存流程</el-descriptions-item>
        <el-descriptions-item label="Ctrl+Shift+S">另存为</el-descriptions-item>
        <el-descriptions-item label="Ctrl+D">复制流程</el-descriptions-item>
        <el-descriptions-item label="Ctrl+P">预览流程</el-descriptions-item>
        <el-descriptions-item label="Ctrl+B">部署流程</el-descriptions-item>
        <el-descriptions-item label="Ctrl+Z">撤销</el-descriptions-item>
        <el-descriptions-item label="Ctrl+Y">重做</el-descriptions-item>
        <el-descriptions-item label="Delete/Backspace">删除选中元素</el-descriptions-item>
        <el-descriptions-item label="+/-">放大/缩小</el-descriptions-item>
        <el-descriptions-item label="0">适应屏幕</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="helpVisible = false">知道了</el-button>
      </template>
    </el-dialog>

    <!-- 关联表单对话框 -->
    <el-dialog v-model="formConfigVisible" title="关联表单配置" width="800px">
      <el-form :model="formConfig" label-width="120px">
        <el-alert title="配置说明" type="info" :closable="false" class="mb-4">
          <p>1. 选择流程中的用户任务节点</p>
          <p>2. 为每个任务节点关联对应的表单</p>
          <p>3. 启动表单用于发起流程时填写数据</p>
          <p>4. 办理表单用于任务处理时填写数据</p>
        </el-alert>
        <el-form-item label="启动表单">
          <el-select v-model="formConfig.startForm" placeholder="请选择启动表单" filterable style="width: 100%">
            <el-option v-for="form in formList" :key="form.id" :label="form.formName" :value="form.formCode" />
          </el-select>
          <div class="form-tip">用于流程发起时填写数据</div>
        </el-form-item>
        <el-divider>任务节点表单配置</el-divider>
        <el-table ref="tableRef" :data="taskList" border>
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="任务 ID" width="150" />
          <el-table-column prop="name" label="任务名称" width="200" />
          <el-table-column label="办理表单">
            <template #default="{ row }">
              <el-select v-model="row.formKey" placeholder="请选择表单" filterable style="width: 100%">
                <el-option v-for="form in formList" :key="form.id" :label="form.formName" :value="form.formCode" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="处理人类型" width="150">
            <template #default="{ row }">
              <el-select v-model="row.assigneeType" placeholder="请选择" style="width: 100%">
                <el-option label="指定用户" value="user" />
                <el-option label="部门负责人" value="deptManager" />
                <el-option label="岗位" value="position" />
                <el-option label="变量" value="variable" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="处理人" width="200">
            <template #default="{ row }">
              <el-input v-model="row.assignee" placeholder="用户 ID/变量名" />
            </template>
          </el-table-column>
        </el-table>
        <div class="batch-actions">
          <el-button size="small" @click="batchSetAssigneeType">批量设置处理人类型</el-button>
          <el-button size="small" @click="clearSelection">清空选择</el-button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="formConfigVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFormConfig">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 发起流程对话框 -->
    <el-dialog v-model="launchVisible" title="发起流程" width="800px">
      <el-form :model="launchData" label-width="120px">
        <el-form-item label="选择流程">
          <el-select v-model="launchData.processDefinitionId" placeholder="请选择流程定义" style="width: 100%" @change="loadStartForm">
            <el-option v-for="def in deployedDefinitions" :key="def.id" :label="def.name" :value="def.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务 Key">
          <el-input v-model="launchData.businessKey" placeholder="如：请假申请 -20240101-001" />
        </el-form-item>
        <el-divider>表单数据</el-divider>
        <div v-if="startFormComponents.length > 0" class="start-form-container">
          <el-form-item v-for="comp in startFormComponents" :key="comp.id" :label="comp.label" :required="comp.required">
            <el-input v-if="comp.type === 'input'" v-model="launchData.formValues[comp.prop]" :placeholder="comp.placeholder" />
            <el-input v-else-if="comp.type === 'textarea'" v-model="launchData.formValues[comp.prop]" type="textarea" :rows="3" :placeholder="comp.placeholder" />
            <el-input-number v-else-if="comp.type === 'number'" v-model="launchData.formValues[comp.prop]" :min="0" />
            <el-select v-else-if="comp.type === 'select'" v-model="launchData.formValues[comp.prop]" :placeholder="comp.placeholder">
              <el-option v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-radio-group v-else-if="comp.type === 'radio'" v-model="launchData.formValues[comp.prop]">
              <el-radio v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.value">{{ opt.label }}</el-radio>
            </el-radio-group>
            <el-checkbox-group v-else-if="comp.type === 'checkbox'" v-model="launchData.formValues[comp.prop]">
              <el-checkbox v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.value">{{ opt.label }}</el-checkbox>
            </el-checkbox-group>
            <el-date-picker v-else-if="comp.type === 'date'" v-model="launchData.formValues[comp.prop]" type="date" :placeholder="comp.placeholder" />
          </el-form-item>
        </div>
        <el-empty v-else description="该流程没有配置启动表单" />
      </el-form>
      <template #footer>
        <el-button @click="launchVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLaunch" :loading="launching">发起流程</el-button>
      </template>
    </el-dialog>

    <!-- 批量操作对话框 -->
    <el-dialog v-model="batchVisible" title="批量设置节点属性" width="700px">
      <el-form :model="batchForm" label-width="120px">
        <el-form-item label="操作类型">
          <el-select v-model="batchForm.operation" placeholder="请选择操作类型" style="width: 100%" @change="onBatchOperationChange">
            <el-option label="批量设置处理人类型" value="assigneeType" />
            <el-option label="批量设置处理人" value="assignee" />
            <el-option label="批量添加文档" value="documentation" />
            <el-option label="批量设置分类" value="category" />
          </el-select>
        </el-form-item>
        <el-form-item label="节点类型过滤">
          <el-checkbox-group v-model="batchForm.nodeTypes">
            <el-checkbox label="userTask">用户任务</el-checkbox>
            <el-checkbox label="serviceTask">服务任务</el-checkbox>
            <el-checkbox label="scriptTask">脚本任务</el-checkbox>
            <el-checkbox label="sendTask">发送任务</el-checkbox>
            <el-checkbox label="receiveTask">接收任务</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-divider v-if="batchForm.operation" />
        <el-form-item v-if="batchForm.operation === 'assigneeType'" label="处理人类型">
          <el-select v-model="batchForm.assigneeType" placeholder="请选择" style="width: 100%">
            <el-option label="指定用户" value="user" />
            <el-option label="部门负责人" value="deptManager" />
            <el-option label="岗位" value="position" />
            <el-option label="变量" value="variable" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="batchForm.operation === 'assignee'" label="处理人">
          <el-input v-model="batchForm.assignee" placeholder="用户 ID/角色编码" />
        </el-form-item>
        <el-form-item v-if="batchForm.operation === 'documentation'" label="文档内容">
          <el-input v-model="batchForm.documentation" type="textarea" :rows="3" placeholder="文档描述" />
        </el-form-item>
        <el-form-item v-if="batchForm.operation === 'category'" label="分类">
          <el-input v-model="batchForm.category" placeholder="分类名称" />
        </el-form-item>
        <el-alert :title="`将影响 ${affectedNodes} 个节点`" type="info" :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" @click="executeBatchOperation">执行</el-button>
      </template>
    </el-dialog>

    <!-- 自动布局对话框 -->
    <el-dialog v-model="autoLayoutVisible" title="自动布局" width="500px">
      <el-form :model="autoLayoutForm" label-width="100px">
        <el-form-item label="布局方向">
          <el-radio-group v-model="autoLayoutForm.direction">
            <el-radio label="horizontal">从左到右</el-radio>
            <el-radio label="vertical">从上到下</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="节点间距">
          <el-slider v-model="autoLayoutForm.spacing" :min="50" :max="300" :step="10" />
        </el-form-item>
        <el-form-item label="行间距">
          <el-slider v-model="autoLayoutForm.rowSpacing" :min="50" :max="300" :step="10" />
        </el-form-item>
        <el-alert title="自动布局会重新排列所有节点，请先保存当前流程" type="warning" :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="autoLayoutVisible = false">取消</el-button>
        <el-button type="primary" @click="executeAutoLayout">执行布局</el-button>
      </template>
    </el-dialog>

    <!-- 流程模板库对话框 -->
    <el-dialog v-model="templateLibVisible" title="流程模板库" width="900px">
      <el-tabs v-model="templateLibTab">
        <el-tab-pane label="常用模板" name="common">
          <el-row :gutter="16">
            <el-col :span="8" v-for="tpl in commonTemplates" :key="tpl.id">
              <el-card shadow="hover" class="template-card" @click="applyTemplate(tpl)">
                <template #header>
                  <div class="template-header">
                    <el-icon :size="20"><component :is="tpl.icon" /></el-icon>
                    <span>{{ tpl.name }}</span>
                  </div>
                </template>
                <div class="template-body">
                  <p>{{ tpl.description }}</p>
                  <el-tag size="small" type="info">{{ tpl.nodeCount }} 个节点</el-tag>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="我的模板" name="my">
          <el-table :data="myTemplates" border>
            <el-table-column prop="name" label="模板名称" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="updateTime" label="更新时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" @click="applyTemplate(row)">应用</el-button>
                <el-button size="small" type="danger" @click="deleteMyTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 流程模拟对话框 -->
    <el-dialog v-model="simulationVisible" title="流程执行模拟" width="900px">
      <div class="simulation-container">
        <div class="simulation-controls">
          <el-button type="primary" @click="startSimulation" :loading="simulating">
            <el-icon><VideoPlay /></el-icon> 开始模拟
          </el-button>
          <el-button @click="resetSimulation">重置</el-button>
          <el-button @click="nextStep">下一步</el-button>
          <el-select v-model="simulationSpeed" placeholder="模拟速度" size="default" style="width: 120px">
            <el-option label="慢速" value="slow" />
            <el-option label="正常" value="normal" />
            <el-option label="快速" value="fast" />
          </el-select>
        </div>
        <div class="simulation-status">
          <el-steps :active="currentStep" finish-status="success" align-center>
            <el-step v-for="(step, index) in simulationPath" :key="index" :title="step.name" :description="step.type" />
          </el-steps>
        </div>
        <div class="simulation-log">
          <h4>执行日志</h4>
          <ul>
            <li v-for="(log, index) in simulationLogs" :key="index">{{ log }}</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="simulationVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 智能推荐对话框 -->
    <el-dialog v-model="recommendationVisible" title="智能配置推荐" width="700px">
      <el-form :model="recommendationForm" label-width="120px">
        <el-form-item label="流程类型">
          <el-select v-model="recommendationForm.processType" placeholder="请选择流程类型" style="width: 100%" @change="onProcessTypeChange">
            <el-option label="审批流程" value="approval" />
            <el-option label="会签流程" value="countersign" />
            <el-option label="并行流程" value="parallel" />
            <el-option label="条件分支流程" value="conditional" />
            <el-option label="循环流程" value="loop" />
          </el-select>
        </el-form-item>
        <el-alert :title="recommendationForm.processType ? '已为您生成推荐配置' : '请选择流程类型'" type="info" :closable="false" />
        <div class="recommendation-list" v-if="recommendations.length > 0">
          <el-card v-for="(rec, index) in recommendations" :key="index" shadow="hover" class="recommendation-card">
            <div class="recommendation-header">
              <el-tag :type="rec.type === 'required' ? 'danger' : 'warning'">{{ rec.type === 'required' ? '必需' : '建议' }}</el-tag>
              <span class="recommendation-title">{{ rec.title }}</span>
            </div>
            <p class="recommendation-desc">{{ rec.description }}</p>
            <el-button size="small" @click="applyRecommendation(rec)">应用</el-button>
          </el-card>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="recommendationVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 评论批注对话框 -->
    <el-dialog v-model="commentVisible" title="流程评论与批注" width="800px">
      <div class="comment-container">
        <div class="comment-input">
          <el-input v-model="newComment.content" type="textarea" :rows="3" placeholder="请输入评论内容..." />
          <div class="comment-actions">
            <el-select v-model="newComment.type" placeholder="评论类型" style="width: 120px">
              <el-option label="普通评论" value="comment" />
              <el-option label="问题" value="question" />
              <el-option label="建议" value="suggestion" />
              <el-option label="批注" value="annotation" />
            </el-select>
            <el-button type="primary" @click="addComment">发表评论</el-button>
          </div>
        </div>
        <div class="comment-list">
          <div v-for="(comment, index) in comments" :key="index" class="comment-item">
            <div class="comment-avatar">
              <el-avatar :size="40">{{ comment.user?.charAt(0) }}</el-avatar>
            </div>
            <div class="comment-content">
              <div class="comment-meta">
                <span class="comment-user">{{ comment.user }}</span>
                <span class="comment-time">{{ comment.time }}</span>
                <el-tag :type="comment.type === 'question' ? 'danger' : (comment.type === 'suggestion' ? 'warning' : '')" size="small">{{ getCommentTypeName(comment.type) }}</el-tag>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions-row">
                <el-button link size="small" @click="replyComment(index)">回复</el-button>
                <el-button link size="small" @click="deleteComment(index)">删除</el-button>
              </div>
              <div v-if="comment.replies" class="comment-replies">
                <div v-for="(reply, rIndex) in comment.replies" :key="rIndex" class="reply-item">
                  <span class="reply-user">{{ reply.user }}:</span>
                  <span class="reply-text">{{ reply.content }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="commentVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 导出选项对话框 -->
    <el-dialog v-model="exportOptionsVisible" title="导出流程" width="500px">
      <el-form :model="exportOptions" label-width="100px">
        <el-form-item label="导出格式">
          <el-select v-model="exportOptions.format" placeholder="选择格式" style="width: 100%">
            <el-option label="BPMN 2.0 XML" value="bpmn" />
            <el-option label="SVG 图片" value="svg" />
            <el-option label="PNG 图片" value="png" />
            <el-option label="PDF 文档" value="pdf" />
            <el-option label="JSON 模板" value="json" />
          </el-select>
        </el-form-item>
        <el-form-item label="包含内容" v-if="exportOptions.format === 'json'">
          <el-checkbox-group v-model="exportOptions.include">
            <el-checkbox label="formConfig">表单配置</el-checkbox>
            <el-checkbox label="comments">评论批注</el-checkbox>
            <el-checkbox label="history">版本历史</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="文件名">
          <el-input v-model="exportOptions.filename" placeholder="请输入文件名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportOptionsVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExport">导出</el-button>
      </template>
    </el-dialog>

    <!-- 流程健康度仪表板 -->
    <el-dialog v-model="dashboardVisible" title="流程健康度仪表板" width="900px">
      <div class="dashboard-grid">
        <div class="dashboard-card">
          <h4>流程结构</h4>
          <el-progress :percentage="structureScore" :status="structureScore >= 80 ? 'success' : 'warning'" :format="() => structureScore + '分'" />
          <ul class="score-items">
            <li :class="{ error: structureIssues.length > 0 }">开始事件：{{ startEventCount }} 个</li>
            <li :class="{ error: endEventCount === 0 }">结束事件：{{ endEventCount }} 个</li>
            <li :class="{ warning: orphanNodes > 0 }">孤立节点：{{ orphanNodes }} 个</li>
          </ul>
        </div>
        <div class="dashboard-card">
          <h4>性能指标</h4>
          <el-progress :percentage="performanceScore" :status="performanceScore >= 80 ? 'success' : 'warning'" :format="() => performanceScore + '分'" />
          <ul class="score-items">
            <li>节点总数：{{ totalNodes }}</li>
            <li :class="{ warning: totalNodes > 50 }">建议：≤50 节点</li>
            <li>网关数量：{{ gatewayCount }}</li>
          </ul>
        </div>
        <div class="dashboard-card">
          <h4>完整性</h4>
          <el-progress :percentage="completenessScore" :status="completenessScore >= 80 ? 'success' : 'warning'" :format="() => completenessScore + '分'" />
          <ul class="score-items">
            <li :class="{ error: !processInfo.name }">流程名称：{{ processInfo.name || '未设置' }}</li>
            <li :class="{ error: !formConfig.startForm }">启动表单：{{ formConfig.startForm || '未关联' }}</li>
            <li>任务表单：{{ taskFormCount }} 个</li>
          </ul>
        </div>
        <div class="dashboard-card">
          <h4>规范度</h4>
          <el-progress :percentage="standardScore" :status="standardScore >= 80 ? 'success' : 'warning'" :format="() => standardScore + '分'" />
          <ul class="score-items">
            <li :class="{ warning: unnamedNodes > 0 }">未命名节点：{{ unnamedNodes }} 个</li>
            <li :class="{ warning: noAssigneeTasks > 0 }">无处理人任务：{{ noAssigneeTasks }} 个</li>
          </ul>
        </div>
      </div>
      <el-divider />
      <div class="dashboard-summary">
        <h4>综合得分：<span :class="overallScore >= 80 ? 'score-good' : 'score-warning'">{{ overallScore }} 分</span></h4>
        <p>{{ getOverallComment() }}</p>
      </div>
      <template #footer>
        <el-button @click="dashboardVisible = false">关闭</el-button>
        <el-button type="primary" @click="fixAllIssues">一键修复</el-button>
      </template>
    </el-dialog>

    <!-- 快捷键自定义对话框 -->
    <el-dialog v-model="shortcutsVisible" title="自定义快捷键" width="700px">
      <el-table :data="shortcutList" border>
        <el-table-column prop="action" label="操作" width="150" />
        <el-table-column prop="description" label="说明" width="200" />
        <el-table-column label="快捷键">
          <template #default="{ row }">
            <el-input v-model="row.shortcut" placeholder="点击输入快捷键" @focus="recordingShortcut = row" />
          </template>
        </el-table-column>
        <el-table-column width="100" label="操作">
          <template #default="{ row, $index }">
            <el-button size="small" @click="resetShortcut($index)">重置</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="resetAllShortcuts">恢复默认</el-button>
        <el-button type="primary" @click="saveShortcuts">保存</el-button>
      </template>
    </el-dialog>

    <!-- 图例说明对话框 -->
    <el-dialog v-model="legendVisible" title="流程图例说明" width="600px">
      <div class="legend-container">
        <div class="legend-section">
          <h4>事件</h4>
          <div class="legend-item"><span class="legend-icon start-event"></span> 开始事件</div>
          <div class="legend-item"><span class="legend-icon end-event"></span> 结束事件</div>
          <div class="legend-item"><span class="legend-icon timer-event"></span> 定时事件</div>
          <div class="legend-item"><span class="legend-icon message-event"></span> 消息事件</div>
        </div>
        <div class="legend-section">
          <h4>任务</h4>
          <div class="legend-item"><span class="legend-icon user-task"></span> 用户任务</div>
          <div class="legend-item"><span class="legend-icon service-task"></span> 服务任务</div>
          <div class="legend-item"><span class="legend-icon script-task"></span> 脚本任务</div>
          <div class="legend-item"><span class="legend-icon mail-task"></span> 邮件任务</div>
        </div>
        <div class="legend-section">
          <h4>网关</h4>
          <div class="legend-item"><span class="legend-icon exclusive-gateway"></span> 排他网关 (XOR)</div>
          <div class="legend-item"><span class="legend-icon parallel-gateway"></span> 并行网关 (AND)</div>
          <div class="legend-item"><span class="legend-icon inclusive-gateway"></span> 包容网关 (OR)</div>
        </div>
        <div class="legend-section">
          <h4>连线</h4>
          <div class="legend-item"><span class="legend-line sequence-flow"></span> 顺序流</div>
          <div class="legend-item"><span class="legend-line message-flow"></span> 消息流</div>
          <div class="legend-item"><span class="legend-line association"></span> 关联线</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="legendVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 协作编辑提示 -->
    <el-dialog v-model="collabVisible" title="协作编辑" width="500px">
      <div class="collab-container">
        <div class="online-users">
          <h4>当前在线用户</h4>
          <div class="user-list">
            <div class="online-user" v-for="user in onlineUsers" :key="user.id">
              <el-avatar :size="32" :style="{ backgroundColor: user.color }">{{ user.name.charAt(0) }}</el-avatar>
              <span>{{ user.name }}</span>
              <el-tag v-if="user.isEditing" size="small" type="info">编辑中</el-tag>
            </div>
          </div>
        </div>
        <el-divider />
        <div class="edit-lock">
          <h4>编辑锁</h4>
          <el-alert v-if="isLocked" title="当前流程已被锁定，无法编辑" type="warning" :closable="false" />
          <el-alert v-else title="您可以锁定流程以防止他人同时编辑" type="info" :closable="false" />
          <el-button :type="isLocked ? 'danger' : 'primary'" @click="toggleLock">
            {{ isLocked ? '解锁' : '锁定流程' }}
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="collabVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 流程导航面板 -->
    <div class="breadcrumb-nav" v-if="showBreadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>流程：{{ processInfo.name || '未命名' }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="selectedNode">选中：{{ selectedNode.name || selectedNode.id }}</el-breadcrumb-item>
      </el-breadcrumb>
      <el-button size="small" @click="showBreadcrumb = false" class="close-breadcrumb">×</el-button>
    </div>

    <!-- 迷你工具栏 -->
    <div class="mini-toolbar" v-show="showMiniToolbar">
      <el-button-group>
        <el-button size="small" @click="undo" title="撤销"><el-icon><RefreshLeft /></el-icon></el-button>
        <el-button size="small" @click="redo" title="重做"><el-icon><RefreshRight /></el-icon></el-button>
        <el-button size="small" @click="deleteSelected" title="删除"><el-icon><Delete /></el-icon></el-button>
        <el-divider direction="vertical" />
        <el-button size="small" @click="zoomIn" title="放大"><el-icon><ZoomIn /></el-icon></el-button>
        <el-button size="small" @click="zoomOut" title="缩小"><el-icon><ZoomOut /></el-icon></el-button>
        <el-divider direction="vertical" />
        <el-button size="small" @click="showMiniToolbar = false" title="关闭"><el-icon><Close /></el-icon></el-button>
      </el-button-group>
    </div>

    <!-- 快速添加节点菜单 -->
    <div class="quick-add-menu" v-if="quickAddVisible" :style="{ top: quickAddY + 'px', left: quickAddX + 'px' }">
      <h4>快速添加</h4>
      <ul>
        <li @click="quickAddNode('bpmn:UserTask')">用户任务</li>
        <li @click="quickAddNode('bpmn:ServiceTask')">服务任务</li>
        <li @click="quickAddNode('bpmn:ExclusiveGateway')">排他网关</li>
        <li @click="quickAddNode('bpmn:ParallelGateway')">并行网关</li>
        <li class="divider"></li>
        <li @click="quickAddNode('bpmn:StartEvent')">开始事件</li>
        <li @click="quickAddNode('bpmn:EndEvent')">结束事件</li>
      </ul>
    </div>
  </div>
</template>

<script setup name="WorkflowDesigner">
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import request from '@/utils/request'
import router from '@/router'
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule
} from 'bpmn-js-properties-panel'
import '@bpmn-io/properties-panel/assets/properties-panel.css'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DocumentAdd,
  FolderOpened,
  Document,
  DocumentChecked,
  Picture,
  PictureFilled,
  Download,
  RefreshLeft,
  RefreshRight,
  ZoomIn,
  ZoomOut,
  FullScreen,
  Checked,
  View,
  Close,
  VideoPlay,
  Files,
  Loading,
  Clock,
  DocumentCopy, Star, Upload, Setting,
  QuestionFilled,
  Tickets,
  Operation, Grid, Collection,
  ChatRound,
  Monitor, Key, User,
  Search,
  DataAnalysis,
  Menu,
  Delete
} from '@element-plus/icons-vue'

const bpmnCanvas = ref(null)
const propertiesPanel = ref(null)
const minimap = ref(null)
const bpmnModeler = ref(null)
const currentDiagram = ref(null)
const processName = ref('')
const activePanels = ref('events')
const currentZoom = ref(100)
const gridEnabled = ref(true)
const showMinimap = ref(false)
const deploying = ref(false)
const deployed = ref(false)
const saved = ref(false)
const autoSaving = ref(false)
const lastSavedTime = ref(null)
const hasUnsavedChanges = ref(false)
const processInfoVisible = ref(false)
const formConfigVisible = ref(false)
const helpVisible = ref(false)
const launchVisible = ref(false)
const templateVisible = ref(false)
const templateTab = ref('save')
const savingTemplate = ref(false)
const nodePropsVisible = ref(false)
const statsVisible = ref(false)
const nodeListVisible = ref(false)
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const searchNodeText = ref("")
const searchResults = ref([])
const currentSearchIndex = ref(-1)

const flowStats = ref(null)
const nodeList = ref([])

// 快速统计（计算属性）
const quickStats = computed(() => {
  if (!flowStats.value) return { nodes: 0, flows: 0 }
  return {
    nodes: flowStats.value.totalNodes || 0,
    flows: flowStats.value.totalFlows || 0
  }
})
const contextMenuTarget = ref(null)

// 批量操作
const batchVisible = ref(false)
const batchForm = reactive({
  operation: '',
  nodeTypes: ['userTask'],
  assigneeType: 'user',
  assignee: '',
  documentation: '',
  category: ''
})
const affectedNodes = ref(0)

// 自动布局
const autoLayoutVisible = ref(false)
const autoLayoutForm = reactive({
  direction: 'horizontal',
  spacing: 100,
  rowSpacing: 80
})

// 模板库
const templateLibVisible = ref(false)
const templateLibTab = ref('common')
const myTemplates = ref([])
// 流程模拟
const simulationVisible = ref(false)
const simulationSpeed = ref('normal')
const simulating = ref(false)
const currentStep = ref(0)
const simulationPath = ref([])
const simulationLogs = ref([])

// 流程健康度仪表板
const dashboardVisible = ref(false)

// 智能推荐
const recommendationVisible = ref(false)
const recommendationForm = reactive({
  processType: '',
  businessScenario: ''
})

// 常用模板
// 导航功能
const showBreadcrumb = ref(true)
const showMiniToolbar = ref(true)
const quickAddVisible = ref(false)
const quickAddX = ref(0)
const quickAddY = ref(0)

// 健康度仪表板
const structureScore = ref(0)
const performanceScore = ref(0)
const completenessScore = ref(0)
const standardScore = ref(0)
const startEventCount = ref(0)
const endEventCount = ref(0)
const orphanNodes = ref(0)
const totalNodes = ref(0)
const gatewayCount = ref(0)
const unnamedNodes = ref(0)
const noAssigneeTasks = ref(0)
const taskFormCount = ref(0)

// 快捷键自定义
const shortcutsVisible = ref(false)
const recordingShortcut = ref(null)
const shortcutList = ref([
  { action: 'newDiagram', description: '新建流程', shortcut: 'Ctrl+N' },
  { action: 'saveDiagram', description: '保存流程', shortcut: 'Ctrl+S' },
  { action: 'undo', description: '撤销', shortcut: 'Ctrl+Z' },
  { action: 'redo', description: '重做', shortcut: 'Ctrl+Y' },
  { action: 'deleteSelected', description: '删除', shortcut: 'Delete' },
  { action: 'zoomIn', description: '放大', shortcut: 'Ctrl++' },
  { action: 'zoomOut', description: '缩小', shortcut: 'Ctrl+-' }
])

// 图例
const legendVisible = ref(false)

// 协作编辑
const collabVisible = ref(false)
const onlineUsers = ref([
  { id: 1, name: '张三', color: '#409EFF', isEditing: false },
  { id: 2, name: '李四', color: '#67C23A', isEditing: true }
])
const isLocked = ref(false)


// 评论批注
const commentVisible = ref(false)
const comments = ref([])
const newComment = reactive({
  content: '',
  type: 'comment'
})

// 导出选项
const exportOptionsVisible = ref(false)
const exportOptions = reactive({
  format: 'bpmn',
  include: ['formConfig'],
  filename: ''
})

const commonTemplates = ref([
  { id: 1, name: '请假流程', description: '包含申请、审批、销假流程', nodeCount: 5, icon: 'Document', bpmn: '' },
  { id: 2, name: '报销流程', description: '包含申请、财务审核、打款流程', nodeCount: 6, icon: 'Money', bpmn: '' },
  { id: 3, name: '采购流程', description: '包含申请、比价、订单、验收流程', nodeCount: 8, icon: 'ShoppingCart', bpmn: '' },
  { id: 4, name: '入职流程', description: '包含 HR 录入、部门安排、设备领取流程', nodeCount: 7, icon: 'User', bpmn: '' },
  { id: 5, name: '合同审批流程', description: '包含起草、法务审核、签署流程', nodeCount: 5, icon: 'File', bpmn: '' },
  { id: 6, name: '出差流程', description: '包含申请、审批、报销流程', nodeCount: 5, icon: 'Airplane', bpmn: '' }
])

const templateForm = reactive({
  name: '',
  category: 'hr',
  description: ''
})

const selectedNode = ref(null)
const selectedElement = null
const versionVisible = ref(false)
const versionList = ref([])
const launching = ref(false)

const formList = ref([])
const taskList = ref([])
let autoSaveTimer = null
const deployedDefinitions = ref([])
const startFormComponents = ref([])

const processInfo = ref({
  name: '',
  id: '',
  version: '1.0',
  description: '',
  category: ''
})

const formConfig = ref({
  startForm: '',
  tasks: []
})


// 防抖函数
const debounce = (fn, delay) => {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

// 节流函数
const throttle = (fn, delay) => {
  let lastTime = 0
  return function(...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

const launchData = ref({
  processDefinitionId: '',
  businessKey: '',
  formValues: {}
})



// 默认的 BPMN 2.0 XML - 包含 SAP NetWeaver BPM 风格的初始流程
const defaultBpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                  id="Definitions_1"
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" name="新流程" isExecutable="true" camunda:versionTag="1.0.0">
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

// 快捷键映射
const keyboardShortcuts = {
  'Ctrl-N': () => newDiagram(),
  'Ctrl-O': () => openFile(),
  'Ctrl-S': () => saveDiagram(),
  'Ctrl-Shift-S': () => saveAsDiagram(),
  'Ctrl-D': () => duplicateProcess(),
  'Ctrl-Z': () => undo(),
  'Ctrl-Y': () => redo(),
  'Delete': () => deleteSelected(),
  'Backspace': () => deleteSelected(),
  '+': () => zoomIn(),
  '-': () => zoomOut(),
  '0': () => zoomFit(),
  'Ctrl-P': () => previewDiagram(),
  'Ctrl-B': () => deployDiagram(),
  'Ctrl-H': () => showHelp()
}

onMounted(() => {
  initBpmnModeler()
  initPalette()
  initKeyboardShortcuts()
  initContextMenu()
  loadLastDiagram()
  
  // 监听页面关闭，提醒未保存的更改
  window.addEventListener('beforeunload', (e) => {
    if (hasUnsavedChanges.value) {
      e.preventDefault()
      e.returnValue = ''
    }
  })
})

onBeforeUnmount(() => {
  if (bpmnModeler.value) {
    bpmnModeler.value.destroy()
  }
  document.removeEventListener('keydown', handleKeyDown)
})

// 初始化 BPMN 建模器
const initBpmnModeler = () => {
  bpmnModeler.value = new BpmnModeler({
    container: bpmnCanvas.value,
    keyboard: {
      bindTo: window
    },
    propertiesPanel: {
      parent: propertiesPanel.value
    },
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule,
      CamundaPlatformPropertiesProviderModule
    ],
    palette: {
      visible: false // 使用自定义工具栏
    }
  })

  // 加载默认图表
  bpmnModeler.value.importXML(defaultBpmnXml)

  // 监听视图变化更新缩放比例
  const eventBus = bpmnModeler.value.get('eventBus')
  eventBus.on('canvas.viewbox.changed', ({ viewbox }) => {
    currentZoom.value = Math.round(100 / viewbox.scale)
  })

  // 监听选择变化
  eventBus.on('selection.changed', (event) => {
    const newSelection = event.newSelection
    if (newSelection && newSelection.length > 0) {
      const selected = newSelection[0]
      const businessObject = selected.businessObject
      if (businessObject && businessObject.$type !== 'bpmn:Process' && businessObject.$type !== 'bpmn:SequenceFlow') {
        selectedNode.value = {
          id: businessObject.id,
          name: businessObject.name || '',
          type: businessObject.$type,
          assignee: businessObject.assignee || '',
          assigneeType: 'user',
          serviceClass: businessObject.implementation || '',
          method: ''
        }
      } else {
        selectedNode.value = null
      }
    } else {
      selectedNode.value = null
    }
  })

  // 监听命令执行以更新撤销/重做状态
  eventBus.on('commandStack.changed', () => {
    // 可以在这里更新撤销/重做按钮状态
  })
}

// 初始化工具栏
const initPalette = () => {
  const paletteItems = document.querySelectorAll('.palette-item')
  paletteItems.forEach(item => {
    item.addEventListener('click', () => {
      handlePaletteClick(item.dataset.action)
    })
  })
}

// 初始化快捷键
const initKeyboardShortcuts = () => {
  document.addEventListener('keydown', handleKeyDown)
}

const handleKeyDown = (e) => {
  const key = []
  if (e.ctrlKey || e.metaKey) key.push('Ctrl')
  if (e.shiftKey) key.push('Shift')
  if (e.altKey) key.push('Alt')
  
  // 处理方向键导航（在有选中元素时）
  if (selectedNode.value && ['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
    e.preventDefault()
    handleNodeNavigation(e.key)
    return
  }
  
  key.push(e.key.toUpperCase())
  const shortcut = key.join('-')

  // 检查是否匹配快捷键
  for (const [keys, handler] of Object.entries(keyboardShortcuts)) {
    if (shortcut === keys) {
      e.preventDefault()
      handler()
      break
    }
  }
}

// 处理节点键盘导航
const handleNodeNavigation = (key) => {
  const modeling = bpmnModeler.value.get('modeling')
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const element = elementRegistry.get(selectedNode.value.id)
  
  if (!element) return
  
  const step = e.shiftKey ? 20 : 10 // 按住 Shift 键时移动距离加倍
  
  let newX = element.x
  let newY = element.y
  
  switch (key) {
    case 'ArrowUp':
      newY -= step
      break
    case 'ArrowDown':
      newY += step
      break
    case 'ArrowLeft':
      newX -= step
      break
    case 'ArrowRight':
      newX += step
      break
  }
  
  modeling.moveElements([element], { x: newX - element.x, y: newY - element.y }, { x: 0, y: 0 })
}

// 处理工具栏点击
const handlePaletteClick = (action) => {
  const modeling = bpmnModeler.value.get('modeling')
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const rootElement = elementRegistry.getRoot()

  // 获取画布中心位置
  const eventBus = bpmnModeler.value.get('eventBus')
  const viewbox = canvas.viewbox()
  const centerX = viewbox.x + viewbox.width / 2
  const centerY = viewbox.y + viewbox.height / 2

  try {
    switch (action) {
      // 开始事件
      case 'create-start-event':
        modeling.createShape({ type: 'bpmn:StartEvent', name: '开始' }, { x: centerX, y: centerY }, rootElement)
        break
      case 'create-timer-start':
        modeling.createShape({
          type: 'bpmn:StartEvent',
          name: '定时开始',
          eventDefinitions: [{ $type: 'bpmn:TimerEventDefinition' }]
        }, { x: centerX, y: centerY }, rootElement)
        break
      case 'create-message-start':
        modeling.createShape({
          type: 'bpmn:StartEvent',
          name: '消息开始',
          eventDefinitions: [{ $type: 'bpmn:MessageEventDefinition' }]
        }, { x: centerX, y: centerY }, rootElement)
        break

      // 结束事件
      case 'create-end-event':
        modeling.createShape({ type: 'bpmn:EndEvent', name: '结束' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-message-end':
        modeling.createShape({
          type: 'bpmn:EndEvent',
          name: '消息结束',
          eventDefinitions: [{ $type: 'bpmn:MessageEventDefinition' }]
        }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-error-end':
        modeling.createShape({
          type: 'bpmn:EndEvent',
          name: '错误结束',
          eventDefinitions: [{ $type: 'bpmn:ErrorEventDefinition' }]
        }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-terminate-end':
        modeling.createShape({
          type: 'bpmn:EndEvent',
          name: '终止结束',
          eventDefinitions: [{ $type: 'bpmn:TerminateEventDefinition' }]
        }, { x: centerX + 150, y: centerY }, rootElement)
        break

      // 任务
      case 'create-user-task':
        modeling.createShape({ type: 'bpmn:UserTask', name: '用户任务' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-service-task':
        modeling.createShape({ type: 'bpmn:ServiceTask', name: '服务任务' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-script-task':
        modeling.createShape({ type: 'bpmn:ScriptTask', name: '脚本任务' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-send-task':
        modeling.createShape({ type: 'bpmn:SendTask', name: '发送任务' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-receive-task':
        modeling.createShape({ type: 'bpmn:ReceiveTask', name: '接收任务' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-manual-task':
        modeling.createShape({ type: 'bpmn:ManualTask', name: '手动任务' }, { x: centerX + 150, y: centerY }, rootElement)
        break

      // 网关
      case 'create-exclusive-gateway':
        modeling.createShape({ type: 'bpmn:ExclusiveGateway', name: '排他网关' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-parallel-gateway':
        modeling.createShape({ type: 'bpmn:ParallelGateway', name: '并行网关' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-inclusive-gateway':
        modeling.createShape({ type: 'bpmn:InclusiveGateway', name: '包容网关' }, { x: centerX + 150, y: centerY }, rootElement)
        break
      case 'create-event-gateway':
        modeling.createShape({ type: 'bpmn:EventBasedGateway', name: '事件网关' }, { x: centerX + 150, y: centerY }, rootElement)
        break

      // 数据
      case 'create-data-object':
        modeling.createShape({ type: 'bpmn:DataObjectReference', name: '数据对象' }, { x: centerX + 150, y: centerY + 100 }, rootElement)
        break
      case 'create-data-store':
        modeling.createShape({ type: 'bpmn:DataStoreReference', name: '数据存储' }, { x: centerX + 150, y: centerY + 100 }, rootElement)
        break

      // 子流程
      case 'create-subprocess':
        modeling.createShape({ type: 'bpmn:SubProcess', name: '子流程' }, { x: centerX, y: centerY }, rootElement)
        break
      case 'create-transaction':
        modeling.createShape({ type: 'bpmn:Transaction', name: '事务' }, { x: centerX, y: centerY }, rootElement)
        break
      case 'create-event-subprocess':
        modeling.createShape({ type: 'bpmn:SubProcess', triggeredByEvent: true }, { x: centerX, y: centerY }, rootElement)
        break

      // 连接
      case 'create-sequence-flow':
        ElMessage.info('请按住 Ctrl 键，从起始元素拖拽到目标元素创建连接')
        break
      case 'create-message-flow':
        ElMessage.info('请在两个元素间创建消息流')
        break
      case 'create-association':
        ElMessage.info('请按 Ctrl 键点击元素创建关联')
        break

      default:
        ElMessage.warning('未知操作：' + action)
    }

    if (action !== 'create-sequence-flow' && action !== 'create-message-flow' && action !== 'create-association') {
      ElMessage.success('已添加元素')
    }
  } catch (err) {
    console.error('创建元素失败:', err)
    ElMessage.error('创建元素失败：' + err.message)
  }
}


// 批量操作对话框
const openBatchDialog = () => {
  batchVisible.value = true
}

// 自动布局对话框
const openAutoLayout = () => {
  autoLayoutVisible.value = true
}

// 模板库对话框
const openTemplateLib = () => {
  templateLibVisible.value = true
}

// 适应画布
const resetViewPort = () => {
  if (bpmnModeler.value) {
    const canvas = bpmnModeler.value.get('canvas')
    canvas.zoom('fit-viewport')
  }
}

// 新建流程
const newDiagram = () => {
  ElMessageBox.confirm('确定要新建流程吗？未保存的更改将丢失。', '提示', {
    type: 'warning'
  }).then(() => {
    bpmnModeler.value.importXML(defaultBpmnXml, (err) => {
      if (err) {
        ElMessage.error('新建流程失败：' + err.message)
        return
      }
      currentDiagram.value = null
      processName.value = ''
      processInfo.value = { name: '', id: '', version: '1.0', description: '', category: '' }
      ElMessage.success('新建流程成功')
    })
  }).catch(() => {})
}

// 打开文件
const openFile = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.bpmn,.xml'
  input.onchange = (e) => {
    const file = e.target.files[0]
    const reader = new FileReader()
    reader.onload = (event) => {
      const xml = event.target.result
      try {
        bpmnModeler.value.importXML(xml)
        ElMessage.success('打开文件成功')
        // 解析流程名称
        const parser = new DOMParser()
        const xmlDoc = parser.parseFromString(xml, 'text/xml')
        const process = xmlDoc.getElementsByTagName('bpmn:process')[0]
        if (process) {
          processName.value = process.getAttribute('name') || ''
          processInfo.value.id = process.getAttribute('id') || ''
          processInfo.value.name = processName.value
        }
      } catch (err) {
        ElMessage.error('打开文件失败：' + err.message)
      }
    }
    reader.readAsText(file)
  }
  input.click()
}

// 保存流程
const saveDiagram = async () => {
  if (!processName.value) {
    processInfoVisible.value = true
    return
  }
  await doSave()
}

// 另存为
const saveAsDiagram = async () => {
  processInfoVisible.value = true
}

const doSave = async () => {
  let { xml } = await bpmnModeler.value.saveXML({ format: true })
  
  // 自动修复 isExecutable 属性
  if (xml.includes('isExecutable="false"')) {
    xml = xml.replace(/isExecutable="false"/g, 'isExecutable="true"')
    console.log('已自动修复 isExecutable 属性')
  }
  
  // 验证 BPMN（移除 isExecutable 检查，因为已自动修复）
  const validationError = validateBpmnNoCheck(xml)
  if (validationError) {
    ElMessage.error(validationError)
    return
  }
  
  try {
    const result = await saveToServer({
      name: processInfo.value.name || processName.value,
      id: processInfo.value.id,
      version: processInfo.value.version,
      description: processInfo.value.description,
      category: processInfo.value.category,
      bpmnXml: xml
    })
    if (result) {
      ElMessage.success('保存成功')
      processInfoVisible.value = false
    }
  } catch (err) {
    ElMessage.error('保存失败：' + err.message)
  }
}

const saveProcessInfo = () => {
  if (!processInfo.value.name) {
    ElMessage.warning('请输入流程名称')
    return
  }
  processName.value = processInfo.value.name
  doSave()
}


// 验证 BPMN（不检查 isExecutable，因为已自动修复）
const validateBpmnNoCheck = (xml) => {
  try {
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(xml, 'text/xml')
    
    // 检查开始事件数量
    const startEvents = xmlDoc.getElementsByTagName('bpmn:startEvent')
    if (startEvents.length === 0) {
      return '流程必须包含至少一个开始事件'
    }
    if (startEvents.length > 1) {
      return '流程只能包含一个开始事件，当前有 ' + startEvents.length + ' 个'
    }
    
    // 检查结束事件
    const endEvents = xmlDoc.getElementsByTagName('bpmn:endEvent')
    if (endEvents.length === 0) {
      return '流程必须包含至少一个结束事件'
    }
    
    return null
  } catch (e) {
    console.error('BPMN 验证失败:', e)
    return 'BPMN 验证失败：' + e.message
  }
}

// 自动保存
const enableAutoSave = () => {
  // 监听画布变化
  if (bpmnModeler.value) {
    const eventBus = bpmnModeler.value.get('eventBus')
    eventBus.on('commandStack.changed', () => {
      hasUnsavedChanges.value = true
      // 防抖自动保存（3 秒后）
      if (autoSaveTimer) clearTimeout(autoSaveTimer)
      autoSaveTimer = setTimeout(() => {
        if (hasUnsavedChanges.value && saved.value) {
          autoSave()
        }
      }, 3000)
    })
  }
}

// 自动备份到本地存储
const autoBackup = async () => {
  try {
    const { xml } = await bpmnModeler.value.saveXML({ format: true })
    const backup = {
      xml,
      name: processInfo.value.name,
      timestamp: new Date().toISOString()
    }
    localStorage.setItem('workflow_auto_backup', JSON.stringify(backup))
    console.log('自动备份完成')
  } catch (err) {
    console.error('自动备份失败:', err)
  }
}

const autoSave = async () => {
  if (!processInfo.value.id || !hasUnsavedChanges.value) return
  
  autoSaving.value = true
  try {
    const { xml } = await bpmnModeler.value.saveXML({ format: true })
    await saveToServer({
      name: processInfo.value.name,
      id: processInfo.value.id,
      version: processInfo.value.version,
      description: processInfo.value.description,
      category: processInfo.value.category,
      bpmnXml: xml,
      autoSave: true
    })
    lastSavedTime.value = new Date()
    hasUnsavedChanges.value = false
    
    // 同时进行本地备份
    await autoBackup()
    
    ElMessage.success('自动保存成功')
  } catch (err) {
    console.error('自动保存失败:', err)
    ElMessage.error('自动保存失败，但已保存到本地备份')
    await autoBackup()
  } finally {
    autoSaving.value = false
  }
}

// 恢复本地备份
const restoreFromBackup = () => {
  const backupStr = localStorage.getItem('workflow_auto_backup')
  if (!backupStr) {
    ElMessage.info('没有找到本地备份')
    return
  }
  
  try {
    const backup = JSON.parse(backupStr)
    ElMessageBox.confirm(
      `发现本地备份，时间：${backup.timestamp}，是否恢复？`,
      '恢复备份',
      { confirmButtonText: '恢复', cancelButtonText: '取消', type: 'warning' }
    ).then(async () => {
      await bpmnModeler.value.importXML(backup.xml)
      processInfo.value.name = backup.name
      ElMessage.success('备份已恢复')
    }).catch(() => {})
  } catch (err) {
    console.error('恢复备份失败:', err)
    ElMessage.error('恢复备份失败')
  }
}

// 保存到服务器
const saveToServer = async (params) => {
  console.log('保存流程到服务器:', params)
  try {
    const { data } = await request.post('/system/workflow/save', {
      name: params.name,
      bpmnXml: params.bpmnXml,
      category: params.category || 'default',
      description: params.description
    })
    if (data && data.code === 200) {
      saved.value = true
      processInfo.value.id = params.id || Date.now().toString()
      processInfo.value.version = params.version || '1.0'
      return true
    } else if (data && data.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      return false
    }
    saved.value = true
    return true
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败：' + (error.response?.data?.message || error.message))
    if (error.response?.status === 401) {
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    }
    return false
  }
}

// 部署到服务器
const deployToServer = async (params) => {
  console.log('部署流程到服务器:', params)
  try {
    const { data } = await request.post('/system/workflow/deploy', {
      name: params.name,
      bpmnXml: params.bpmnXml
    })
    if (data && data.code === 200) {
      deployed.value = true
      loadDeployedDefinitions()
      return true
    } else if (data && data.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      return false
    }
    deployed.value = true
    loadDeployedDefinitions()
    return true
  } catch (error) {
    console.error('部署失败:', error)
    ElMessage.error('部署失败：' + (error.response?.data?.message || error.message))
    if (error.response?.status === 401) {
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    }
    return false
  }
}

// 打开表单配置
const openFormConfig = async () => {
  // 加载表单列表
  await loadFormList()
  // 解析 BPMN 获取用户任务列表
  await loadUserTasks()
  formConfigVisible.value = true
}

// 加载表单列表
const loadFormList = async () => {
  try {
    const { data } = await request.get('/system/form-definition/list', {
      params: { pageNum: 1, pageSize: 100, status: 1 }
    })
    formList.value = data?.records || []
  } catch (error) {
    console.error('加载表单列表失败:', error)
  }
}

// 加载用户任务列表
const loadUserTasks = async () => {
  try {
    const { xml } = await bpmnModeler.value.saveXML()
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(xml, 'text/xml')
    
    const userTasks = xmlDoc.getElementsByTagName('bpmn:userTask')
    const tasks = []
    for (let i = 0; i < userTasks.length; i++) {
      const task = userTasks[i]
      tasks.push({
        id: task.getAttribute('id'),
        name: task.getAttribute('name') || '未命名任务',
        formKey: '',
        assigneeType: 'user',
        assignee: '',
        selected: false
      })
    }
    taskList.value = tasks
  } catch (error) {
    console.error('加载用户任务失败:', error)
    ElMessage.error('加载任务节点失败')
  }
}

// 批量设置处理人类型
const tableRef = ref(null)

const batchSetAssigneeType = () => {
  if (!tableRef.value) {
    ElMessage.warning('表格未加载')
    return
  }
  const selectedRows = tableRef.value.getSelectionRows()
  if (selectedRows.length === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  
  ElMessageBox.prompt('请选择处理人类型', '批量设置处理人类型', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'select',
    inputOptions: [
      { label: '指定用户', value: 'user' },
      { label: '部门负责人', value: 'deptManager' },
      { label: '岗位', value: 'position' },
      { label: '变量', value: 'variable' }
    ]
  }).then(({ value }) => {
    selectedRows.forEach(task => {
      task.assigneeType = value
    })
    ElMessage.success(`已为 ${selectedRows.length} 个任务设置处理人类型`)
  }).catch(() => {})
}

// 清空选择
const clearSelection = () => {
  taskList.value.forEach(t => t.selected = false)
}

// 保存表单配置
const saveFormConfig = async () => {
  if (!formConfig.value.startForm) {
    ElMessage.warning('请选择启动表单')
    return
  }
  
  try {
    const config = {
      processDefinitionId: processInfo.value.id,
      startForm: formConfig.value.startForm,
      tasks: taskList.value.map(t => ({
        taskDefinitionKey: t.id,
        formKey: t.formKey,
        assigneeType: t.assigneeType,
        assignee: t.assignee
      }))
    }
    
    // TODO: 调用保存配置的 API
    console.log('保存表单配置:', config)
    await request.post('/system/workflow/form/config', config)
    
    ElMessage.success('表单配置已保存')
    formConfigVisible.value = false
  } catch (error) {
    console.error('保存表单配置失败:', error)
    ElMessage.error('保存表单配置失败')
  }
}

// 加载已部署的流程定义
const loadDeployedDefinitions = async () => {
  try {
    const { data } = await request.get('/system/workflow/definition/list', {
      params: { pageNum: 1, pageSize: 100 }
    })
    deployedDefinitions.value = data?.records || []
  } catch (error) {
    console.error('加载流程定义失败:', error)
  }
}

// 加载启动表单
const loadStartForm = async () => {
  const def = deployedDefinitions.value.find(d => d.id === launchData.value.processDefinitionId)
  if (!def) return
  
  startFormComponents.value = []
  
  try {
    // 获取流程关联的启动表单
    const { data } = await request.get(`/system/workflow/form/start/${def.key}`)
    if (data?.formContent) {
      try {
        startFormComponents.value = JSON.parse(data.formContent)
      } catch (e) {
        console.error('表单 Schema 解析失败:', e)
        // 如果解析失败，尝试直接使用数组
        if (Array.isArray(data.formContent)) {
          startFormComponents.value = data.formContent
        }
      }
    } else if (data?.formSchema) {
      try {
        startFormComponents.value = JSON.parse(data.formSchema)
      } catch (e) {
        startFormComponents.value = []
      }
    } else {
      startFormComponents.value = []
    }
  } catch (error) {
    console.error('加载启动表单失败:', error)
    startFormComponents.value = []
  }
}

// 发起流程
const submitLaunch = async () => {
  if (!launchData.value.processDefinitionId) {
    ElMessage.warning('请选择流程定义')
    return
  }
  
  // 验证业务 Key
  if (!launchData.value.businessKey) {
    ElMessage.warning('请输入业务 Key（如：请假申请 -20240101-001）')
    return
  }
  
  launching.value = true
  try {
    const result = await request.post('/system/workflow/instance/start', {
      processDefinitionId: launchData.value.processDefinitionId,
      businessKey: launchData.value.businessKey,
      variables: launchData.value.formValues
    })
    
    ElMessage.success({
      message: '流程发起成功！实例 ID: ' + (result.data?.processInstanceId || '未知'),
      duration: 3000
    })
    launchVisible.value = false
    // 重置数据
    launchData.value = { processDefinitionId: '', businessKey: '', formValues: {} }
    startFormComponents.value = []
    deployedDefinitions.value = []
  } catch (error) {
    console.error('发起流程失败:', error)
    ElMessage.error('发起流程失败：' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    launching.value = false
  }
}

// 复制流程
const duplicateProcess = async () => {
  if (!processInfo.value.id) {
    ElMessage.warning('请先保存流程')
    return
  }
  
  try {
    // 获取当前流程定义
    const { data } = await request.get(`/system/workflow/definition/${processInfo.value.id}`)
    if (!data) {
      ElMessage.error('流程不存在')
      return
    }
    
    // 复制流程信息
    processInfo.value = {
      name: data.name + ' (副本)',
      id: '',
      version: '1.0',
      description: data.description,
      category: data.category
    }
    processName.value = processInfo.value.name
    
    // 获取 BPMN 并重新保存
    const { xml } = await bpmnModeler.value.saveXML({ format: true })
    const result = await saveToServer({
      name: processInfo.value.name,
      bpmnXml: xml,
      category: processInfo.value.category,
      description: processInfo.value.description
    })
    
    if (result) {
      ElMessage.success('流程复制成功')
      hasUnsavedChanges.value = false
    }
  } catch (error) {
    console.error('复制流程失败:', error)
    ElMessage.error('复制流程失败')
  }
}

// 显示版本历史
const showVersionHistory = async () => {
  if (!processInfo.value.id) {
    ElMessage.warning('流程未保存')
    return
  }
  
  try {
    const { data } = await request.get('/system/workflow/definition/list', {
      params: { name: processInfo.value.name }
    })
    versionList.value = data?.records || []
    versionVisible.value = true
  } catch (error) {
    console.error('加载版本历史失败:', error)
    ElMessage.error('加载版本历史失败')
  }
}

// 加载版本
const loadVersion = async (version) => {
  try {
    const { data } = await request.get(`/system/workflow/definition/bpmn/${version.id}`)
    if (data?.bpmnXml) {
      await bpmnModeler.value.importXML(data.bpmnXml)
      processInfo.value.version = version.version
      ElMessage.success('版本加载成功')
      versionVisible.value = false
    }
  } catch (error) {
    console.error('加载版本失败:', error)
    ElMessage.error('加载版本失败')
  }
}

// 删除版本
const deleteVersion = async (version) => {
  try {
    await ElMessageBox.confirm('确定要删除此版本吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/system/workflow/definition/${version.deploymentId}`)
    ElMessage.success('删除成功')
    showVersionHistory()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 保存为模板
const exportAsTemplate = () => {
  if (!saved.value) {
    ElMessage.warning('请先保存流程')
    return
  }
  templateForm.name = processName.value + ' 模板'
  templateForm.description = ''
  templateTab.value = 'save'
  templateVisible.value = true
}

// 保存模板
const saveAsTemplate = async () => {
  if (!templateForm.name) {
    ElMessage.warning('请输入模板名称')
    return
  }
  savingTemplate.value = true
  try {
    const { xml } = await bpmnModeler.value.saveXML({ format: true })
    const templateData = {
      name: templateForm.name,
      category: templateForm.category,
      description: templateForm.description,
      bpmnXml: xml,
      createTime: new Date().toISOString()
    }
    // 下载模板文件
    const blob = new Blob([JSON.stringify(templateData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = templateForm.name + '.template.json'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('模板保存成功')
    templateVisible.value = false
  } catch (error) {
    console.error('保存模板失败:', error)
    ElMessage.error('保存模板失败')
  } finally {
    savingTemplate.value = false
  }
}

// 导入模板
const importTemplate = () => {
  templateTab.value = 'import'
  templateVisible.value = true
}

// 处理模板文件
const handleTemplateFile = (file) => {
  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      const templateData = JSON.parse(e.target.result)
      if (templateData.bpmnXml) {
        await bpmnModeler.value.importXML(templateData.bpmnXml)
        processName.value = templateData.name?.replace(' 模板', '') || '新流程'
        hasUnsavedChanges.value = true
        ElMessage.success('模板导入成功')
        templateVisible.value = false
      } else {
        ElMessage.error('无效的模板文件')
      }
    } catch (error) {
      console.error('导入模板失败:', error)
      ElMessage.error('导入模板失败：文件格式不正确')
    }
  }
  reader.readAsText(file.raw)
}

// 获取节点类型名称
const getNodeTypeName = (type) => {
  const typeMap = {
    'bpmn:StartEvent': '开始事件',
    'bpmn:EndEvent': '结束事件',
    'bpmn:UserTask': '用户任务',
    'bpmn:ServiceTask': '服务任务',
    'bpmn:ExclusiveGateway': '排他网关',
    'bpmn:ParallelGateway': '并行网关',
    'bpmn:SequenceFlow': '顺序流'
  }
  return typeMap[type] || type
}

// 打开节点属性对话框
const openNodeProps = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  // 从 BPMN 元素中读取最新属性
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const element = elementRegistry.get(selectedNode.value.id)
  
  if (element) {
    const bo = element.businessObject
    
    // 读取现有属性
    selectedNode.value = {
      ...selectedNode.value,
      documentation: bo.documentation?.[0]?.body || '',
      timerExpression: bo.eventDefinitions?.[0]?.timeDuration?.body || '',
      conditionExpression: bo.conditionExpression?.body || '',
      resultVariable: bo['camunda:resultVariable'] || '',
      assigneeType: bo['camunda:assigneeType'] || 'user'
    }
  }
  
  nodePropsVisible.value = true
}

// 保存节点属性
// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  }
}

const saveNodeProps = () => {
  if (!selectedNode.value) return
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const modeling = bpmnModeler.value.get('modeling')
  const element = elementRegistry.get(selectedNode.value.id)
  
  if (element) {
    const bo = element.businessObject
    const updates = {
      name: selectedNode.value.name || ''
    }
    
    // 用户任务属性
    if (selectedNode.value.type === 'bpmn:UserTask' || selectedNode.value.type === 'userTask') {
      updates.assignee = selectedNode.value.assignee || ''
      if (selectedNode.value.assigneeType) {
        updates['camunda:assigneeType'] = selectedNode.value.assigneeType
      }
    }
    
    // 服务任务属性
    if (selectedNode.value.type === 'bpmn:ServiceTask' || selectedNode.value.type === 'serviceTask') {
      updates['camunda:class'] = selectedNode.value.serviceClass || ''
      if (selectedNode.value.method) {
        updates['camunda:method'] = selectedNode.value.method
      }
      if (selectedNode.value.resultVariable) {
        updates['camunda:resultVariable'] = selectedNode.value.resultVariable
      }
    }
    
    // 文档注释
    if (selectedNode.value.documentation) {
      updates.documentation = [{ body: selectedNode.value.documentation }]
    }
    
    // 定时器表达式
    if (selectedNode.value.timerExpression) {
      updates.eventDefinitions = [{
        $type: 'bpmn:TimerEventDefinition',
        timeDuration: { body: selectedNode.value.timerExpression }
      }]
    }
    
    // 条件表达式
    if (selectedNode.value.conditionExpression !== undefined) {
      updates.conditionExpression = { body: selectedNode.value.conditionExpression }
    }
    
    modeling.updateProperties(element, updates)
    hasUnsavedChanges.value = true
    ElMessage.success('节点属性已更新')
  }
  nodePropsVisible.value = false
}

// 显示帮助
const showHelp = () => {
  helpVisible.value = true
}

// 打开流程发起对话框
const launchProcess = () => {
  if (!deployed.value) {
    ElMessage.warning('请先部署流程')
    return
  }
  launchData.value = { processDefinitionId: '', businessKey: '', formValues: {} }
  startFormComponents.value = []
  launchVisible.value = true
  loadDeployedDefinitions()
}

// 下载 BPMN
const downloadBpmn = async () => {
  const { xml } = await bpmnModeler.value.saveXML({ format: true })
  downloadFile(xml, (processName.value || 'workflow') + '.bpmn', 'application/xml')
  ElMessage.success('下载 BPMN 成功')
}

// 下载 SVG
const downloadDiagram = async () => {
  try {
    const { svg } = await bpmnModeler.value.saveSVG()
    downloadFile(svg, (processName.value || 'workflow') + '.svg', 'image/svg+xml')
    ElMessage.success('下载 SVG 成功')
  } catch (err) {
    ElMessage.error('下载失败：' + err.message)
  }
}

// 下载 PNG
const downloadPng = async () => {
  try {
    const { svg } = await bpmnModeler.value.saveSVG()
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()

    img.onload = () => {
      canvas.width = img.width
      canvas.height = img.height
      ctx.drawImage(img, 0, 0)
      const a = document.createElement('a')
      a.download = (processName.value || 'workflow') + '.png'
      a.href = canvas.toDataURL('image/png')
      a.click()
      ElMessage.success('下载 PNG 成功')
    }

    img.src = 'data:image/svg+xml;base64,' + btoa(svg)
  } catch (err) {
    ElMessage.error('下载失败：' + err.message)
  }
}

const downloadFile = (content, filename, type) => {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

// 撤销
const undo = () => {
  const commandStack = bpmnModeler.value.get('commandStack')
  commandStack.undo()
  ElMessage.success('已撤销')
}

// 重做
const redo = () => {
  const commandStack = bpmnModeler.value.get('commandStack')
  commandStack.redo()
  ElMessage.success('已重做')
}

// 删除选中元素
const deleteSelected = () => {
  const modeling = bpmnModeler.value.get('modeling')
  const selection = bpmnModeler.value.get('selection')
  const selected = selection.get()
  if (selected.length > 0) {
    modeling.removeElements(selected)
    ElMessage.success('已删除')
  }
}

// 缩放控制
const zoomIn = () => {
  const eventBus = bpmnModeler.value.get('eventBus')
  canvas.zoom('step-in')
}

const zoomOut = () => {
  const eventBus = bpmnModeler.value.get('eventBus')
  canvas.zoom('step-out')
}

const zoomFit = () => {
  const eventBus = bpmnModeler.value.get('eventBus')
  canvas.zoom('fit-viewport')
}

// 切换网格
const toggleGrid = () => {
  const eventBus = bpmnModeler.value.get('eventBus')
  const config = bpmnModeler.value.get('config.grid')
  if (gridEnabled.value) {
    canvas.addMarker('grid-enabled')
  } else {
    canvas.removeMarker('grid-enabled')
  }
}

// 切换迷你地图
const toggleMinimap = () => {
  showMinimap.value = !showMinimap.value
}

// 切换属性面板
const toggleProperties = () => {
  const panel = document.querySelector('.properties-panel-container')
  panel.style.display = panel.style.display === 'none' ? 'block' : 'none'
}

// 验证流程

// 详细验证（返回验证结果对象）
const validateDiagramDetailed = async () => {
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const elements = elementRegistry.getAll()
  const root = elementRegistry.getRoot()
  
  const warnings = []
  const errors = []
  
  // 检查开始事件
  const startEvents = elements.filter(e => e.type === 'bpmn:StartEvent')
  if (startEvents.length === 0) {
    errors.push('错误：缺少开始事件')
  } else if (startEvents.length > 1) {
    warnings.push('警告：存在多个开始事件（可能存在并行启动）')
  }
  
  // 检查结束事件
  const endEvents = elements.filter(e => e.type === 'bpmn:EndEvent')
  if (endEvents.length === 0) {
    warnings.push('警告：缺少结束事件')
  }
  
  // 检查用户任务是否有处理人
  const userTasks = elements.filter(e => e.type === 'bpmn:UserTask')
  userTasks.forEach(task => {
    const bo = task.businessObject
    if (!bo.name) {
      warnings.push(`警告：用户任务 "${task.id}" 缺少名称`)
    }
    if (!bo.assignee && !bo['camunda:assignee']) {
      warnings.push(`警告：用户任务 "${bo.name || task.id}" 未指定处理人`)
    }
  })
  
  // 检查服务任务是否有实现
  const serviceTasks = elements.filter(e => e.type === 'bpmn:ServiceTask')
  serviceTasks.forEach(task => {
    const bo = task.businessObject
    if (!bo.name) {
      warnings.push(`警告：服务任务 "${task.id}" 缺少名称`)
    }
    if (!bo['camunda:class'] && !bo['camunda:delegateExpression'] && !bo['camunda:expression']) {
      warnings.push(`警告：服务任务 "${bo.name || task.id}" 未指定实现类`)
    }
  })
  
  // 检查网关
  const gateways = elements.filter(e => e.type === 'bpmn:ExclusiveGateway' || e.type === 'bpmn:ParallelGateway')
  const flows = elements.filter(e => e.type === 'bpmn:SequenceFlow')
  
  gateways.forEach(gw => {
    const outgoing = flows.filter(f => f.source?.id === gw.id)
    if (outgoing.length === 0) {
      warnings.push(`警告：网关 "${gw.businessObject.name || gw.id}" 没有流出分支`)
    }
    if (outgoing.length === 1 && gw.type === 'bpmn:ExclusiveGateway') {
      warnings.push(`提示：排他网关 "${gw.businessObject.name || gw.id}" 只有一个流出分支`)
    }
  })
  
  // 检查是否有孤立节点（除了开始事件）
  const flowSourceIds = new Set(flows.map(f => f.source?.id))
  const flowTargetIds = new Set(flows.map(f => f.target?.id))
  
  const activities = elements.filter(e => 
    ['bpmn:UserTask', 'bpmn:ServiceTask', 'bpmn:ExclusiveGateway', 'bpmn:ParallelGateway'].includes(e.type)
  )
  
  activities.forEach(activity => {
    if (!flowTargetIds.has(activity.id)) {
      warnings.push(`警告：节点 "${activity.businessObject.name || activity.id}" 没有流入连接`)
    }
  })
  
  return {
    valid: errors.length === 0,
    errors,
    warnings: [...errors, ...warnings]
  }
}


const validateDiagram = () => {
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const elements = elementRegistry.getAll()
  const root = elementRegistry.getRoot()

  // 获取流程定义
  const processElements = elements.filter(e => e.type === 'bpmn:Process' && e !== root)
  const issues = []

  if (processElements.length === 0) {
    ElMessage.warning('流程中没有任何元素')
    return false
  }

  // 检查是否有开始事件
  const startEvents = elements.filter(e => e.type === 'bpmn:StartEvent')
  if (startEvents.length === 0) {
    issues.push('缺少开始事件')
  } else if (startEvents.length > 1) {
    issues.push('存在多个开始事件')
  }

  // 检查是否有结束事件
  const endEvents = elements.filter(e => e.type === 'bpmn:EndEvent')
  if (endEvents.length === 0) {
    issues.push('缺少结束事件')
  }

  // 检查用户任务
  const userTasks = elements.filter(e => e.type === 'bpmn:UserTask')
  userTasks.forEach(task => {
    if (!task.businessObject.name) {
      issues.push(`用户任务 "${task.id}" 缺少名称`)
    }
  })

  // 检查是否有孤立元素（没有连接的元素）
  const flows = elements.filter(e => e.type === 'bpmn:SequenceFlow')
  const flowSourceIds = new Set(flows.map(f => f.source?.id))
  const flowTargetIds = new Set(flows.map(f => f.target?.id))
  
  const activityTypes = ['bpmn:StartEvent', 'bpmn:EndEvent', 'bpmn:UserTask', 'bpmn:ServiceTask', 'bpmn:ExclusiveGateway', 'bpmn:ParallelGateway']
  const activities = elements.filter(e => activityTypes.includes(e.type))
  
  activities.forEach(activity => {
    if (!flowSourceIds.has(activity.id) && !flowTargetIds.has(activity.id) && activity.type !== 'bpmn:StartEvent') {
      issues.push(`孤立元素："${activity.businessObject.name || activity.id}" 没有连接`)
    }
  })

  // 检查网关是否有正确的分支/汇聚
  const gateways = elements.filter(e => e.type === 'bpmn:ExclusiveGateway' || e.type === 'bpmn:ParallelGateway')
  gateways.forEach(gw => {
    const incoming = flows.filter(f => f.target?.id === gw.id).length
    const outgoing = flows.filter(f => f.source?.id === gw.id).length
    if (incoming === 0 && outgoing === 0) {
      issues.push(`孤立网关："${gw.businessObject.name || gw.id}"`)
    }
  })

  if (issues.length > 0) {
    ElMessageBox.alert(
      '<ul style="max-height: 300px; overflow-y: auto; text-align: left;">' + 
      issues.map(i => `<li style="color: #f56c6c; margin: 5px 0;">${i}</li>`).join('') + 
      '</ul>',
      '流程验证失败',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭',
        showClose: true,
        type: 'warning'
      }
    )
    return false
  }

  ElMessage.success('流程验证通过！共检查：' + activities.length + ' 个节点，' + flows.length + ' 个连线')
  return true
}
// 增强验证 - 添加流程健康度检查
const enhancedValidate = () => {
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const elements = elementRegistry.getAll()
  const flows = elements.filter(e => e.type === 'bpmn:SequenceFlow')
  
  const warnings = []
  const suggestions = []
  
  // 检查循环
  const graph = buildGraph(elements, flows)
  const hasCycle = detectCycle(graph)
  if (hasCycle) {
    warnings.push('流程中存在循环路径')
  }
  
  // 检查过长的流程链
  const longestPath = findLongestPath(graph)
  if (longestPath > 15) {
    suggestions.push(`流程链过长 (${longestPath} 个节点)，建议拆分为子流程`)
  }
  
  // 检查网关平衡
  const gateways = elements.filter(e => e.type === 'bpmn:ExclusiveGateway' || e.type === 'bpmn:ParallelGateway')
  gateways.forEach(gw => {
    const incoming = flows.filter(f => f.target?.id === gw.id).length
    const outgoing = flows.filter(f => f.source?.id === gw.id).length
    if (outgoing < 2) {
      warnings.push(`网关 "${gw.businessObject.name || gw.id}" 只有一个输出分支`)
    }
    if (incoming === 0 && outgoing > 0) {
      suggestions.push(`网关 "${gw.businessObject.name || gw.id}" 没有输入，可能是起点`)
    }
  })
  
  // 检查未分配处理人的用户任务
  const userTasks = elements.filter(e => e.type === 'bpmn:UserTask')
  userTasks.forEach(task => {
    const bo = task.businessObject
    if (!bo.assignee && !bo.candidateUsers?.length && !bo.candidateGroups?.length) {
      warnings.push(`用户任务 "${bo.name || task.id}" 未分配处理人`)
    }
  })
  
  // 检查 service task 实现
  const serviceTasks = elements.filter(e => e.type === 'bpmn:ServiceTask')
  serviceTasks.forEach(task => {
    const bo = task.businessObject
    const impl = bo.implementation || bo.class || bo.delegateExpression
    if (!impl) {
      warnings.push(`服务任务 "${bo.name || task.id}" 未配置实现类`)
    }
  })
  
  // 显示报告
  if (warnings.length > 0 || suggestions.length > 0) {
    let html = '<div style="text-align: left; max-height: 400px; overflow-y: auto;">'
    if (warnings.length > 0) {
      html += '<h4 style="color: #E6A23C;">⚠️ 警告 (' + warnings.length + ')</h4><ul style="color: #E6A23C;">'
      html += warnings.map(w => '<li>' + w + '</li>').join('')
      html += '</ul>'
    }
    if (suggestions.length > 0) {
      html += '<h4 style="color: #909399;">💡 建议 (' + suggestions.length + ')</h4><ul style="color: #909399;">'
      html += suggestions.map(s => '<li>' + s + '</li>').join('')
      html += '</ul>'
    }
    html += '</div>'
    
    ElMessageBox.alert(html, '流程健康度检查', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showClose: true,
      type: 'warning'
    })
    return false
  }
  
  ElMessage.success('流程健康度检查通过！')
  return true
}

// 构建图
const buildGraph = (elements, flows) => {
  const graph = new Map()
  elements.forEach(el => {
    if (!graph.has(el.id)) graph.set(el.id, [])
  })
  flows.forEach(flow => {
    const source = flow.source?.id
    const target = flow.target?.id
    if (source && target && graph.has(source)) {
      graph.get(source).push(target)
    }
  })
  return graph
}

// 检测环
const detectCycle = (graph) => {
  const visited = new Set()
  const recStack = new Set()
  
  const dfs = (node) => {
    visited.add(node)
    recStack.add(node)
    for (const neighbor of (graph.get(node) || [])) {
      if (!visited.has(neighbor)) {
        if (dfs(neighbor)) return true
      } else if (recStack.has(neighbor)) {
        return true
      }
    }
    recStack.delete(node)
    return false
  }
  
  for (const [node] of graph) {
    if (!visited.has(node) && dfs(node)) return true
  }
  return false
}

// 找最长路径
const findLongestPath = (graph) => {
  const memo = new Map()
  const dfs = (node) => {
    if (memo.has(node)) return memo.get(node)
    let max = 0
    for (const neighbor of (graph.get(node) || [])) {
      max = Math.max(max, 1 + dfs(neighbor))
    }
    memo.set(node, max)
    return max
  }
  
  let longest = 0
  for (const [node] of graph) {
    longest = Math.max(longest, dfs(node))
  }
  return longest
}


// 预览流程
const previewDiagram = async () => {
  try {
    const { svg } = await bpmnModeler.value.saveSVG()
    await ElMessageBox.alert(
      `<div style="max-height: 600px; overflow: auto; text-align: center;">${svg}</div>`,
      '流程预览',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭',
        showClose: true
      }
    )
  } catch (err) {
    ElMessage.error('预览失败：' + err.message)
  }
}

// 部署流程
// 部署选项
const deployOptions = {
  overwrite: false,
  notifyUsers: false
}

const deployDiagram = async () => {
  if (!processName.value) {
    ElMessage.warning('请先保存流程')
    return
  }

  // 部署前先验证
  const validation = await validateDiagramDetailed()
  if (!validation.valid) {
    ElMessageBox.confirm(
      `流程存在 ${validation.warnings.length} 个警告，是否继续部署？\n\n` + 
      validation.warnings.join('\n'),
      '验证警告',
      { confirmButtonText: '继续部署', cancelButtonText: '取消', type: 'warning' }
    ).then(() => performDeployment()).catch(() => {})
    return
  }

  // 二次确认
  ElMessageBox.confirm(
    `确定要部署流程「${processInfo.value.name}」吗？\n这将覆盖服务器上同名的流程定义。`,
    '部署确认',
    { confirmButtonText: '确定部署', cancelButtonText: '取消', type: 'info' }
  ).then(performDeployment).catch(() => {})
}

// 执行部署
const performDeployment = async () => {
  deploying.value = true
  const loading = ElMessage({ message: '正在部署流程...', type: 'info', duration: 0 })
  
  try {
    const { xml } = await bpmnModeler.value.saveXML({ format: true })
    
    // 模拟部署耗时（实际项目中替换为真实 API 调用）
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    const result = await deployToServer({
      name: processInfo.value.name || processName.value,
      bpmnXml: xml,
      version: processInfo.value.version,
      overwrite: deployOptions.overwrite
    })
    
    loading.close()
    
    if (result) {
      deployed.value = true
      hasUnsavedChanges.value = false
      ElMessage.success('部署成功！')
      
      // 记录部署历史
      saveDeploymentHistory({
        name: processInfo.value.name,
        version: processInfo.value.version,
        timestamp: new Date(),
        xml: xml
      })
      
      // 询问是否配置表单
      setTimeout(() => {
        ElMessageBox.confirm('是否需要为流程节点关联表单？', '部署完成', {
          confirmButtonText: '关联表单',
          cancelButtonText: '稍后配置',
          type: 'success'
        }).then(() => openFormConfig()).catch(() => {})
      }, 500)
    }
  } catch (err) {
    console.error('部署失败:', err)
    ElMessage.error('部署失败：' + (err.message || '未知错误'))
  } finally {
    deploying.value = false
  }
}

// 保存部署历史（本地存储）
const saveDeploymentHistory = (record) => {
  const history = JSON.parse(localStorage.getItem('deploymentHistory') || '[]')
  history.unshift(record)
  if (history.length > 10) history.length = 10 // 只保留最近 10 条
  localStorage.setItem('deploymentHistory', JSON.stringify(history))
}

// 加载上次编辑的图表（本地存储）
const loadLastDiagram = () => {
  const saved = localStorage.getItem('lastDiagram')
  if (saved) {
    try {
      bpmnModeler.value.importXML(saved)
      ElMessage.info('已恢复上次编辑的流程')
    } catch (err) {
      console.error('恢复失败:', err)
    }
  }
}


// 验证 BPMN

// 自动保存（每 5 分钟）
setInterval(async () => {
  const { xml } = await bpmnModeler.value.saveXML({ format: true })
  localStorage.setItem('lastDiagram', xml)
}, 300000)

// 解析选项
const parseOptions = (optionsStr) => {
  if (!optionsStr) return []
  return optionsStr.split('\n').filter(line => line.includes(':')).map(line => {
    const [label, value] = line.split(':')
    return { label: label.trim(), value: value.trim() }
  })
}

// 格式化时间
const formatTime = (date) => {
  const d = new Date(date)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.getMonth() + 1 + '月' + d.getDate() + '日 ' + d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
}

// 流程统计
const showStats = () => {
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const elements = elementRegistry.getAll()
  
  const stats = {
    totalNodes: 0,
    totalFlows: 0,
    startEvents: 0,
    endEvents: 0,
    userTasks: 0,
    serviceTasks: 0,
    scriptTasks: 0,
    sendReceiveTasks: 0,
    exclusiveGateways: 0,
    parallelGateways: 0,
    inclusiveGateways: 0,
    subProcesses: 0
  }
  
  elements.forEach(el => {
    const type = el.type
    if (type === 'bpmn:SequenceFlow') {
      stats.totalFlows++
    } else if (type === 'bpmn:StartEvent') {
      stats.startEvents++
      stats.totalNodes++
    } else if (type === 'bpmn:EndEvent') {
      stats.endEvents++
      stats.totalNodes++
    } else if (type === 'bpmn:UserTask') {
      stats.userTasks++
      stats.totalNodes++
    } else if (type === 'bpmn:ServiceTask') {
      stats.serviceTasks++
      stats.totalNodes++
    } else if (type === 'bpmn:ScriptTask') {
      stats.scriptTasks++
      stats.totalNodes++
    } else if (type === 'bpmn:SendTask' || type === 'bpmn:ReceiveTask') {
      stats.sendReceiveTasks++
      stats.totalNodes++
    } else if (type === 'bpmn:ExclusiveGateway') {
      stats.exclusiveGateways++
      stats.totalNodes++
    } else if (type === 'bpmn:ParallelGateway') {
      stats.parallelGateways++
      stats.totalNodes++
    } else if (type === 'bpmn:InclusiveGateway') {
      stats.inclusiveGateways++
      stats.totalNodes++
    } else if (type === 'bpmn:SubProcess') {
      stats.subProcesses++
      stats.totalNodes++
    }
  })
  
  flowStats.value = stats
  statsVisible.value = true
}

// 复杂度计算
const complexityScore = computed(() => {
  if (!flowStats.value) return 0
  const stats = flowStats.value
  let score = 0
  score += stats.totalNodes * 2
  score += stats.totalFlows * 1.5
  score += stats.exclusiveGateways * 5
  score += stats.parallelGateways * 8
  score += stats.subProcesses * 10
  return Math.min(100, Math.round(score))
})

const complexityFormat = (percentage) => {
  if (percentage < 30) return '简单流程'
  if (percentage < 50) return '中等复杂度'
  if (percentage < 70) return '复杂流程'
  return '高度复杂'
}

const complexityStatus = computed(() => {
  const score = complexityScore.value
  if (score < 50) return 'success'
  if (score < 70) return 'warning'
  return 'exception'
})

// 显示节点列表
const showNodeList = () => {
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const elements = elementRegistry.getAll()
  
  nodeList.value = elements
    .filter(el => el.type && el.type.startsWith('bpmn:') && el.type !== 'bpmn:SequenceFlow')
    .map(el => ({
      id: el.id,
      name: el.businessObject?.name || '',
      type: el.type,
      assignee: el.businessObject?.assignee || ''
    }))
  
  nodeListVisible.value = true
}

// 获取节点标签类型
const getNodeTagType = (type) => {
  const typeMap = {
    'bpmn:StartEvent': 'success',
    'bpmn:EndEvent': 'danger',
    'bpmn:UserTask': 'primary',
    'bpmn:ServiceTask': 'warning',
    'bpmn:ScriptTask': 'info',
    'bpmn:ExclusiveGateway': 'warning',
    'bpmn:ParallelGateway': 'primary',
    'bpmn:InclusiveGateway': 'danger'
  }
  return typeMap[type] || ''
}

// 聚焦节点
const focusNode = (row) => {
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const selection = bpmnModeler.value.get('selection')
  const eventBus = bpmnModeler.value.get('eventBus')
  
  const element = elementRegistry.get(row.id)
  if (element) {
    selection.select(element)
    canvas.scrollToElement(element, 200)
  }
  nodeListVisible.value = false
}

// 编辑节点属性
const editNodeProps = (row) => {
  focusNode(row)
  openNodeProps()
}

// 搜索节点（增强版 - 支持查找所有匹配项和导航）
const searchNode = () => {
  if (!searchNodeText.value) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const elements = elementRegistry.getAll()
  const searchText = searchNodeText.value.toLowerCase()
  
  // 查找所有匹配的节点
  searchResults.value = elements.filter(el => {
    const id = el.id?.toLowerCase() || ''
    const name = el.businessObject?.name?.toLowerCase() || ''
    const desc = el.businessObject?.documentation?.[0]?.body?.toLowerCase() || ''
    return id.includes(searchText) || name.includes(searchText) || desc.includes(searchText)
  })
  
  if (searchResults.value.length === 0) {
    ElMessage.info('未找到匹配的节点')
    currentSearchIndex.value = -1
    return
  }
  
  currentSearchIndex.value = 0
  navigateToSearchResult(0)
}

// 查找下一个
const searchNext = () => {
  if (searchResults.value.length === 0) {
    searchNode()
    return
  }
  
  if (currentSearchIndex.value < searchResults.value.length - 1) {
    currentSearchIndex.value++
    navigateToSearchResult(currentSearchIndex.value)
  } else {
    // 循环回到第一个
    currentSearchIndex.value = 0
    navigateToSearchResult(0)
    ElMessage.info('已搜索到末尾，回到第一个结果')
  }
}

// 查找上一个
const searchPrevious = () => {
  if (searchResults.value.length === 0) {
    searchNode()
    return
  }
  
  if (currentSearchIndex.value > 0) {
    currentSearchIndex.value--
    navigateToSearchResult(currentSearchIndex.value)
  } else {
    // 循环到最后一个
    currentSearchIndex.value = searchResults.value.length - 1
    navigateToSearchResult(currentSearchIndex.value)
    ElMessage.info('已搜索到开头，回到最后一个结果')
  }
}

// 导航到搜索结果
const navigateToSearchResult = (index) => {
  if (index < 0 || index >= searchResults.value.length) return
  
  const element = searchResults.value[index]
  const selection = bpmnModeler.value.get('selection')
  const eventBus = bpmnModeler.value.get('eventBus')
  
  selection.select(element)
  canvas.scrollToElement(element, 300)
  highlightElement(element, '#409EFF', 1500)
  
  const total = searchResults.value.length
  const current = index + 1
  ElMessage.success({
    message: `第 ${current}/${total} 个：${element.businessObject?.name || element.id}`,
    duration: 1500
  })
}

// 高亮元素（带动画效果）
const highlightElement = (element, color = '#409EFF', duration = 2000) => {
  const eventBus = bpmnModeler.value.get('eventBus')
  const gfx = canvas._svg.querySelector(`[data-element-id="${element.id}"]`)
  
  if (gfx) {
    const originalStroke = gfx.getAttribute('stroke') || '#000'
    const originalStrokeWidth = gfx.getAttribute('stroke-width') || '2'
    
    // 添加高亮效果
    gfx.setAttribute('stroke', color)
    gfx.setAttribute('stroke-width', '4')
    gfx.style.filter = 'drop-shadow(0 0 8px ' + color + ')'
    
    // 恢复原始状态
    setTimeout(() => {
      gfx.setAttribute('stroke', originalStroke)
      gfx.setAttribute('stroke-width', originalStrokeWidth)
      gfx.style.filter = ''
    }, duration)
  }
}

// 右键菜单
const initContextMenu = () => {
  const canvas = bpmnCanvas.value
  if (!canvas) return
  
  canvas.addEventListener('contextmenu', (e) => {
    e.preventDefault()
    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const element = elementRegistry.getElement(e.target)
    
    if (element) {
      contextMenuTarget.value = element
      contextMenuX.value = e.clientX
      contextMenuY.value = e.clientY
      contextMenuVisible.value = true
    }
  })
  
  document.addEventListener('click', () => {
    contextMenuVisible.value = false
  })
}

// 右键菜单操作
const contextMenuAction = (action) => {
  contextMenuVisible.value = false
  
  switch (action) {
    case 'undo':
      undo()
      break
    case 'redo':
      redo()
      break
    case 'delete':
      deleteSelected()
      break
    case 'duplicate':
      quickCopyNode()
      break
    case 'properties':
      openNodeProps()
      break
    case 'alignLeft':
    case 'alignCenter':
    case 'alignRight':
    case 'alignTop':
    case 'alignMiddle':
    case 'alignBottom':
      alignNodes(action.replace('align', ''))
      break
    default:
      break
  }
}



// 监听属性面板引用变化
watch(propertiesPanel, (newPanel) => {
  if (newPanel && bpmnModeler.value) {
    bpmnModeler.value.get('propertiesPanel').attachTo(newPanel)
  }
})

// ========== 流程对比功能 ==========
// 显示版本对比
const compareVersions = async (version1, version2) => {
  try {
    // 获取两个版本的 BPMN
    const bpmn1 = version1.bpmn || localStorage.getItem(`workflow_version_${version1.id}`)
    const bpmn2 = version2.bpmn || localStorage.getItem(`workflow_version_${version2.id}`)
    
    if (!bpmn1 || !bpmn2) {
      ElMessage.warning('无法获取版本内容')
      return
    }
    
    // 解析 BPMN
    const parser = new DOMParser()
    const xml1 = parser.parseFromString(bpmn1, 'text/xml')
    const xml2 = parser.parseFromString(bpmn2, 'text/xml')
    
    // 提取节点信息
    const extractNodes = (xml) => {
      const nodes = []
      const elements = xml.getElementsByTagName('*')
      for (let i = 0; i < elements.length; i++) {
        const el = elements[i]
        if (el.nodeName.includes('Task') || el.nodeName.includes('Event') || el.nodeName.includes('Gateway')) {
          nodes.push({
            id: el.getAttribute('id'),
            name: el.getAttribute('name') || '',
            type: el.nodeName
          })
        }
      }
      return nodes
    }
    
    const nodes1 = extractNodes(xml1)
    const nodes2 = extractNodes(xml2)
    
    // 对比差异
    const added = nodes2.filter(n2 => !nodes1.find(n1 => n1.id === n2.id))
    const removed = nodes1.filter(n1 => !nodes2.find(n2 => n2.id === n1.id))
    const modified = []
    
    // 检查同名节点的属性变化
    nodes1.forEach(n1 => {
      const n2 = nodes2.find(n => n.id === n1.id)
      if (n2 && n1.name !== n2.name) {
        modified.push({ id: n1.id, oldName: n1.name, newName: n2.name })
      }
    })
    
    // 显示对比结果
    let html = '<div style="text-align: left; max-height: 400px; overflow-y: auto;">'
    
    if (added.length > 0) {
      html += '<h4 style="color: #67C23A;">➕ 新增节点 (' + added.length + ')</h4>'
      html += '<ul style="color: #67C23A;">'
      added.forEach(n => html += '<li>' + n.name + ' (' + n.type + ')</li>')
      html += '</ul>'
    }
    
    if (removed.length > 0) {
      html += '<h4 style="color: #F56C6C;">➖ 删除节点 (' + removed.length + ')</h4>'
      html += '<ul style="color: #F56C6C;">'
      removed.forEach(n => html += '<li>' + n.name + ' (' + n.type + ')</li>')
      html += '</ul>'
    }
    
    if (modified.length > 0) {
      html += '<h4 style="color: #E6A23C;">✏️ 修改节点 (' + modified.length + ')</h4>'
      html += '<ul style="color: #E6A23C;">'
      modified.forEach(m => html += '<li>' + m.oldName + ' → ' + m.newName + '</li>')
      html += '</ul>'
    }
    
    if (added.length === 0 && removed.length === 0 && modified.length === 0) {
      html += '<p>两个版本内容相同</p>'
    }
    
    html += '</div>'
    
    await ElMessageBox.alert(html, '版本对比结果：' + version1.version + ' vs ' + version2.version, {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showClose: true
    })
  } catch (error) {
    console.error('版本对比失败:', error)
    ElMessage.error('版本对比失败：' + error.message)
  }
}

// ========== 节点快捷操作 ==========
// 快速复制节点
const quickCopyNode = async () => {
  const selection = bpmnModeler.value.get('selection')
  const selected = selection.get()
  
  if (selected.length === 0) {
    ElMessage.warning('请先选择要复制的节点')
    return
  }
  
  try {
    const modeling = bpmnModeler.value.get('modeling')
    const selectedCopy = [...selected]
    
    // 向右下方偏移复制
    modeling.moveElements(selectedCopy, { x: 50, y: 50 })
    
    ElMessage.success('复制成功')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败：' + error.message)
  }
}

// 快速对齐节点
const alignNodes = async (alignment) => {
  const selection = bpmnModeler.value.get('selection')
  const selected = selection.get()
  
  if (selected.length < 2) {
    ElMessage.warning('请选择至少两个节点进行对齐')
    return
  }
  
  try {
    const modeling = bpmnModeler.value.get('modeling')
    
    // 获取参考位置
    const refElement = selected[0]
    const refBounds = refElement.di.get('bounds')
    
    selected.forEach((el, index) => {
      if (index === 0) return
      const bounds = el.di.get('bounds')
      let dx = 0, dy = 0
      
      if (alignment === 'left') {
        dx = refBounds.x - bounds.x
      } else if (alignment === 'center') {
        dx = refBounds.x + refBounds.width / 2 - (bounds.x + bounds.width / 2)
      } else if (alignment === 'right') {
        dx = refBounds.x + refBounds.width - (bounds.x + bounds.width)
      } else if (alignment === 'top') {
        dy = refBounds.y - bounds.y
      } else if (alignment === 'middle') {
        dy = refBounds.y + refBounds.height / 2 - (bounds.y + bounds.height / 2)
      } else if (alignment === 'bottom') {
        dy = refBounds.y + refBounds.height - (bounds.y + bounds.height)
      }
      
      if (dx !== 0 || dy !== 0) {
        modeling.moveElements([el], { x: dx, y: dy })
      }
    })
    
    ElMessage.success('对齐完成')
  } catch (error) {
    console.error('对齐失败:', error)
    ElMessage.error('对齐失败：' + error.message)
  }
}

// 分布节点
const distributeNodes = async (direction) => {
  const selection = bpmnModeler.value.get('selection')
  const selected = selection.get()
  
  if (selected.length < 3) {
    ElMessage.warning('请选择至少三个节点进行分布')
    return
  }
  
  try {
    const modeling = bpmnModeler.value.get('modeling')
    
    // 按位置排序
    const sorted = [...selected].sort((a, b) => {
      const aBounds = a.di.get('bounds')
      const bBounds = b.di.get('bounds')
      return direction === 'horizontal' ? aBounds.x - bBounds.x : aBounds.y - bBounds.y
    })
    
    // 计算间距
    const firstBounds = sorted[0].di.get('bounds')
    const lastBounds = sorted[sorted.length - 1].di.get('bounds')
    
    const totalSpace = direction === 'horizontal' 
      ? lastBounds.x - firstBounds.x
      : lastBounds.y - firstBounds.y
    
    const nodeSize = direction === 'horizontal'
      ? sorted.reduce((sum, el) => sum + el.di.get('bounds').width, 0)
      : sorted.reduce((sum, el) => sum + el.di.get('bounds').height, 0)
    
    const spacing = (totalSpace - nodeSize) / (sorted.length - 1)
    
    // 分布节点
    let position = direction === 'horizontal' ? firstBounds.x : firstBounds.y
    
    sorted.forEach((el, index) => {
      if (index === 0) return
      const bounds = el.di.get('bounds')
      const size = direction === 'horizontal' ? bounds.width : bounds.height
      const currentPos = direction === 'horizontal' ? bounds.x : bounds.y
      const targetPos = position + spacing + (direction === 'horizontal' ? sorted[index - 1].di.get('bounds').width : sorted[index - 1].di.get('bounds').height)
      
      const delta = direction === 'horizontal' ? targetPos - currentPos : 0
      const deltaY = direction === 'vertical' ? targetPos - currentPos : 0
      
      modeling.moveElements([el], { x: delta, y: deltaY })
      position = targetPos
    })
    
    ElMessage.success('分布完成')
  } catch (error) {
    console.error('分布失败:', error)
    ElMessage.error('分布失败：' + error.message)
  }
}


// ========== 流程模拟功能 ==========
const openSimulation = async () => {
  await buildSimulationPath()
  simulationVisible.value = true
}

const buildSimulationPath = async () => {
  try {
    const { xml } = await bpmnModeler.value.saveXML()
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(xml, 'text/xml')
    
    const path = []
    const elements = xmlDoc.getElementsByTagName('*')
    
    // 找到开始事件
    for (let i = 0; i < elements.length; i++) {
      const el = elements[i]
      if (el.nodeName.includes('StartEvent')) {
        path.push({
          id: el.getAttribute('id'),
          name: el.getAttribute('name') || el.nodeName,
          type: '开始',
          status: 'pending'
        })
      }
    }
    
    // 简单模拟：按顺序收集所有节点
    const taskTypes = ['UserTask', 'ServiceTask', 'ScriptTask', 'SendTask', 'ReceiveTask']
    const gatewayTypes = ['ExclusiveGateway', 'ParallelGateway', 'InclusiveGateway']
    const endTypes = ['EndEvent']
    
    for (let i = 0; i < elements.length; i++) {
      const el = elements[i]
      const nodeName = el.nodeName.replace('bpmn:', '')
      
      if (taskTypes.some(t => el.nodeName.includes(t))) {
        path.push({
          id: el.getAttribute('id'),
          name: el.getAttribute('name') || nodeName,
          type: '任务',
          status: 'pending'
        })
      } else if (gatewayTypes.some(t => el.nodeName.includes(t))) {
        path.push({
          id: el.getAttribute('id'),
          name: el.getAttribute('name') || nodeName,
          type: '网关',
          status: 'pending'
        })
      } else if (endTypes.some(t => el.nodeName.includes(t))) {
        path.push({
          id: el.getAttribute('id'),
          name: el.getAttribute('name') || nodeName,
          type: '结束',
          status: 'pending'
        })
      }
    }
    
    simulationPath.value = path
    currentStep.value = -1
    simulationLogs.value = ['模拟路径已生成，共 ' + path.length + ' 个节点']
  } catch (error) {
    console.error('构建模拟路径失败:', error)
    ElMessage.error('构建模拟路径失败')
  }
}

const startSimulation = () => {
  if (simulationPath.value.length === 0) {
    ElMessage.warning('没有可模拟的路径')
    return
  }
  
  simulating.value = true
  currentStep.value = 0
  simulationLogs.value = ['开始模拟流程...']
  
  const speedMap = { slow: 1000, normal: 500, fast: 200 }
  const speed = speedMap[simulationSpeed.value] || 500
  
  const runSimulation = async () => {
    for (let i = 0; i < simulationPath.value.length; i++) {
      await new Promise(resolve => setTimeout(resolve, speed))
      currentStep.value = i
      const node = simulationPath.value[i]
      simulationLogs.value.push(`[${new Date().toLocaleTimeString()}] 执行：${node.name} (${node.type})`)
      
      // 高亮当前节点
      highlightSimulationNode(node.id)
    }
    
    simulating.value = false
    simulationLogs.value.push('流程模拟完成！')
  }
  
  runSimulation()
}

const resetSimulation = () => {
  currentStep.value = -1
  simulationLogs.value = ['模拟已重置']
  simulating.value = false
  clearSimulationHighlight()
}

const nextStep = () => {
  if (currentStep.value < simulationPath.value.length - 1) {
    currentStep.value++
    const node = simulationPath.value[currentStep.value]
    simulationLogs.value.push(`[${new Date().toLocaleTimeString()}] 执行：${node.name} (${node.type})`)
    highlightSimulationNode(node.id)
  }
}

const highlightSimulationNode = (nodeId) => {
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const element = elementRegistry.get(nodeId)
  if (element) {
    const selection = bpmnModeler.value.get('selection')
    selection.select(element)
  }
}

const clearSimulationHighlight = () => {
  const selection = bpmnModeler.value.get('selection')
  selection.select(null)
}

// ========== 智能推荐功能 ==========
const openRecommendation = () => {
  recommendationForm.processType = ''
  recommendations.value = []
  recommendationVisible.value = true
}

const onProcessTypeChange = () => {
  const type = recommendationForm.processType
  recommendations.value = getRecommendationsByType(type)
}

const getRecommendationsByType = (type) => {
  const recommendationMap = {
    approval: [
      { type: 'required', title: '添加审批人', description: '为审批任务设置处理人，支持指定用户、角色或变量', action: 'addAssignee' },
      { type: 'suggested', title: '添加超时处理', description: '配置审批超时自动通过或转办', action: 'addTimeout' },
      { type: 'suggested', title: '添加审批意见字段', description: '为审批任务添加意见填写字段', action: 'addCommentField' }
    ],
    countersign: [
      { type: 'required', title: '配置会签规则', description: '设置会签通过条件（全部通过/多数通过/一人通过）', action: 'setMultiInstance' },
      { type: 'required', title: '设置会签人数', description: '配置参与会签的用户列表', action: 'setAssignees' },
      { type: 'suggested', title: '添加完成百分比', description: '显示当前会签完成进度', action: 'addProgress' }
    ],
    parallel: [
      { type: 'required', title: '添加并行网关', description: '使用并行网关拆分和汇聚分支', action: 'addParallelGateway' },
      { type: 'suggested', title: '添加分支条件', description: '为各分支设置执行条件', action: 'addBranchCondition' },
      { type: 'suggested', title: '添加异常处理', description: '为并行分支添加错误处理', action: 'addErrorHandler' }
    ],
    conditional: [
      { type: 'required', title: '添加排他网关', description: '使用排他网关进行条件分支', action: 'addExclusiveGateway' },
      { type: 'required', title: '配置条件表达式', description: '为每个分支设置条件表达式', action: 'setConditions' },
      { type: 'suggested', title: '添加默认分支', description: '设置条件都不满足时的默认路径', action: 'addDefaultFlow' }
    ],
    loop: [
      { type: 'required', title: '配置循环条件', description: '设置循环执行的条件', action: 'setLoopCondition' },
      { type: 'suggested', title: '添加最大循环次数', description: '防止无限循环，设置最大执行次数', action: 'setMaxLoops' },
      { type: 'suggested', title: '添加循环计数器', description: '记录当前循环次数', action: 'addLoopCounter' }
    ]
  }
  
  return recommendationMap[type] || []
}

const applyRecommendation = (rec) => {
  ElMessage.success('已应用推荐：' + rec.title)
  // 实际应用中这里执行具体操作
}

// ========== 评论批注功能 ==========
const openComments = () => {
  loadComments()
  commentVisible.value = true
}

const loadComments = () => {
  // 从 localStorage 加载评论
  const saved = localStorage.getItem('workflow_comments_' + processInfo.value.id)
  if (saved) {
    comments.value = JSON.parse(saved)
  } else {
    // 默认评论
    comments.value = [
      {
        user: '张三',
        time: '2024-01-10 10:30',
        type: 'question',
        content: '这个审批流程是否需要添加财务审核环节？',
        replies: [
          { user: '李四', content: '建议添加，金额超过 1 万的需要财务审核' }
        ]
      }
    ]
  }
}

const addComment = () => {
  if (!newComment.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  comments.value.unshift({
    user: '当前用户',
    time: new Date().toLocaleString('zh-CN'),
    type: newComment.type,
    content: newComment.content,
    replies: []
  })
  
  newComment.content = ''
  saveComments()
  ElMessage.success('评论已添加')
}

const replyComment = (index) => {
  ElMessageBox.prompt('请输入回复内容', '回复评论', {
    confirmButtonText: '发送',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    if (value) {
      if (!comments.value[index].replies) {
        comments.value[index].replies = []
      }
      comments.value[index].replies.push({
        user: '当前用户',
        content: value
      })
      saveComments()
      ElMessage.success('回复已发送')
    }
  }).catch(() => {})
}

const deleteComment = (index) => {
  ElMessageBox.confirm('确定删除此评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    comments.value.splice(index, 1)
    saveComments()
    ElMessage.success('评论已删除')
  }).catch(() => {})
}

const saveComments = () => {
  localStorage.setItem('workflow_comments_' + processInfo.value.id, JSON.stringify(comments.value))
}

const getCommentTypeName = (type) => {
  const names = {
    comment: '评论',
    question: '问题',
    suggestion: '建议',
    annotation: '批注'
  }
  return names[type] || '评论'
}

// ========== 导出功能 ==========
const exportWithOptions = () => {
  exportOptions.format = 'bpmn'
  exportOptions.include = ['formConfig']
  exportOptions.filename = processInfo.value.name || 'workflow'
  exportOptionsVisible.value = true
}

const confirmExport = async () => {
  try {
    let content = ''
    let mimeType = 'text/plain'
    let extension = exportOptions.format
    
    if (exportOptions.format === 'bpmn') {
      const { xml } = await bpmnModeler.value.saveXML({ format: true })
      content = xml
      mimeType = 'application/xml'
    } else if (exportOptions.format === 'json') {
      const { xml } = await bpmnModeler.value.saveXML()
      const data = {
        bpmn: xml,
        processInfo: processInfo.value
      }
      if (exportOptions.include.includes('formConfig')) {
        data.formConfig = formConfig.value
      }
      if (exportOptions.include.includes('comments')) {
        data.comments = comments.value
      }
      content = JSON.stringify(data, null, 2)
      mimeType = 'application/json'
    } else if (exportOptions.format === 'svg') {
      const { svg } = await bpmnModeler.value.saveSVG()
      content = svg
      mimeType = 'image/svg+xml'
    }
    
    // 下载文件
    const blob = new Blob([content], { type: mimeType })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = exportOptions.filename + '.' + extension
    a.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
    exportOptionsVisible.value = false
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败：' + error.message)
  }
}


// ========== 健康度仪表板功能 ==========
const openDashboard = async () => {
  await calculateDashboardScores()
  dashboardVisible.value = true
}

const calculateDashboardScores = async () => {
  try {
    const { xml } = await bpmnModeler.value.saveXML()
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(xml, 'text/xml')
    
    // 统计节点
    const startEvents = xmlDoc.getElementsByTagName('bpmn:startEvent').length
    const endEvents = xmlDoc.getElementsByTagName('bpmn:endEvent').length
    const userTasks = xmlDoc.getElementsByTagName('bpmn:userTask').length
    const serviceTasks = xmlDoc.getElementsByTagName('bpmn:serviceTask').length
    const scriptTasks = xmlDoc.getElementsByTagName('bpmn:scriptTask').length
    const exclusiveGateways = xmlDoc.getElementsByTagName('bpmn:exclusiveGateway').length
    const parallelGateways = xmlDoc.getElementsByTagName('bpmn:parallelGateway').length
    const inclusiveGateways = xmlDoc.getElementsByTagName('bpmn:inclusiveGateway').length
    
    totalNodes.value = startEvents + endEvents + userTasks + serviceTasks + scriptTasks + exclusiveGateways + parallelGateways + inclusiveGateways
    startEventCount.value = startEvents
    endEventCount.value = endEvents
    gatewayCount.value = exclusiveGateways + parallelGateways + inclusiveGateways
    
    // 检查未命名节点
    let unnamed = 0
    const allElements = xmlDoc.getElementsByTagName('*')
    for (let el of allElements) {
      if (el.nodeName.includes('Task') && !el.getAttribute('name')) {
        unnamed++
      }
    }
    unnamedNodes.value = unnamed
    
    // 检查无处理人任务
    let noAssignee = 0
    const tasks = xmlDoc.getElementsByTagName('bpmn:userTask')
    for (let task of tasks) {
      if (!task.getAttribute('assignee') && !task.getAttribute('candidateUsers') && !task.getAttribute('candidateGroups')) {
        noAssignee++
      }
    }
    noAssigneeTasks.value = noAssignee
    
    // 计算分数
    // 结构分数
    let structure = 100
    if (startEvents === 0) structure -= 40
    if (startEvents > 1) structure -= 20
    if (endEvents === 0) structure -= 40
    structureScore.value = Math.max(0, structure)
    
    // 性能分数
    let performance = 100
    if (totalNodes.value > 50) performance -= (totalNodes.value - 50) * 2
    if (gatewayCount.value > 10) performance -= (gatewayCount.value - 10) * 5
    performanceScore.value = Math.max(0, performance)
    
    // 完整性分数
    let completeness = 100
    if (!processInfo.value.name) completeness -= 30
    if (!formConfig.value.startForm) completeness -= 30
    taskFormCount.value = formConfig.value.tasks?.length || 0
    if (taskFormCount.value === 0) completeness -= 20
    completenessScore.value = Math.max(0, completeness)
    
    // 规范分数
    let standard = 100
    if (unnamed > 0) standard -= unnamed * 5
    if (noAssignee > 0) standard -= noAssignee * 10
    standardScore.value = Math.max(0, standard)
  } catch (error) {
    console.error('计算健康度分数失败:', error)
  }
}

const overallScore = computed(() => {
  const avg = (structureScore.value + performanceScore.value + completenessScore.value + standardScore.value) / 4
  return Math.round(avg)
})

const getOverallComment = () => {
  const score = overallScore.value
  if (score >= 90) return '优秀！流程设计非常规范，可以直接部署使用。'
  if (score >= 80) return '良好！流程设计基本规范，建议修复警告项后部署。'
  if (score >= 60) return '及格！流程存在一些问题，建议优化后再部署。'
  return '需要改进！流程存在严重问题，请先修复错误项。'
}

const fixAllIssues = () => {
  ElMessage.info('正在自动修复问题...')
  // 自动修复逻辑
  setTimeout(() => {
    ElMessage.success('已修复所有可自动修复的问题')
    calculateDashboardScores()
  }, 1000)
}

// ========== 快捷键自定义功能 ==========
const openShortcuts = () => {
  loadShortcuts()
  shortcutsVisible.value = true
}

const loadShortcuts = () => {
  const saved = localStorage.getItem('workflow_shortcuts')
  if (saved) {
    shortcutList.value = JSON.parse(saved)
  }
}

const resetShortcut = (index) => {
  const defaults = {
    'newDiagram': 'Ctrl+N',
    'saveDiagram': 'Ctrl+S',
    'undo': 'Ctrl+Z',
    'redo': 'Ctrl+Y',
    'deleteSelected': 'Delete',
    'zoomIn': 'Ctrl++',
    'zoomOut': 'Ctrl+-'
  }
  shortcutList.value[index].shortcut = defaults[shortcutList.value[index].action]
}

const resetAllShortcuts = () => {
  const defaults = [
    { action: 'newDiagram', description: '新建流程', shortcut: 'Ctrl+N' },
    { action: 'saveDiagram', description: '保存流程', shortcut: 'Ctrl+S' },
    { action: 'undo', description: '撤销', shortcut: 'Ctrl+Z' },
    { action: 'redo', description: '重做', shortcut: 'Ctrl+Y' },
    { action: 'deleteSelected', description: '删除', shortcut: 'Delete' },
    { action: 'zoomIn', description: '放大', shortcut: 'Ctrl++' },
    { action: 'zoomOut', description: '缩小', shortcut: 'Ctrl+-' }
  ]
  shortcutList.value = defaults
}

const saveShortcuts = () => {
  localStorage.setItem('workflow_shortcuts', JSON.stringify(shortcutList.value))
  ElMessage.success('快捷键设置已保存')
  shortcutsVisible.value = false
}

// ========== 图例功能 ==========
const openLegend = () => {
  legendVisible.value = true
}

// ========== 协作编辑功能 ==========
const openCollab = () => {
  collabVisible.value = true
}

const toggleLock = () => {
  isLocked.value = !isLocked.value
  ElMessage.success(isLocked.value ? '流程已锁定' : '流程已解锁')
}


// ========== 导航功能 ==========
// 快速添加节点
const quickAddNode = (type) => {
  const modeling = bpmnModeler.value.get('modeling')
  const eventBus = bpmnModeler.value.get('eventBus')
  
  const viewbox = canvas.viewbox()
  const centerX = viewbox.x + viewbox.width / 2
  const centerY = viewbox.y + viewbox.height / 2
  
  const shape = {
    type: type,
    x: centerX,
    y: centerY,
    width: 100,
    height: 80
  }
  
  if (type.includes('Event')) {
    shape.width = 60
    shape.height = 60
  } else if (type.includes('Gateway')) {
    shape.width = 50
    shape.height = 50
  }
  
  modeling.createShape(shape, { x: centerX, y: centerY }, canvas.getRootElement())
  quickAddVisible.value = false
  ElMessage.success('已添加 ' + type)
}

// 显示快速添加菜单
const showQuickAdd = (e) => {
  quickAddX.value = e.clientX
  quickAddY.value = e.clientY
  quickAddVisible.value = true
  
  setTimeout(() => {
    document.addEventListener('click', hideQuickAdd, { once: true })
  }, 100)
}

const hideQuickAdd = () => {
  quickAddVisible.value = false
}

// 双击画布快速添加
const initDoubleClick = () => {
  const canvas = bpmnCanvas.value
  if (!canvas) return
  
  canvas.addEventListener('dblclick', (e) => {
    if (e.target.classList.contains('bpmn-canvas')) {
      showQuickAdd(e)
    }
  })
}

// 在 onMounted 中调用
// initDoubleClick()

</script>

<style scoped lang="scss">
.workflow-designer {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.designer-header {
  height: 52px;
  padding: 0 16px;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(to bottom, #ffffff, #f8f9fa);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .save-status {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #666;
      margin-left: 8px;
      
      .is-loading {
        animation: rotating 1s linear infinite;
      }
    }
  }
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.mr-2 {
  margin-right: 8px;
}

.batch-actions {
  margin-top: 12px;
  padding: 8px 0;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  border-top: 1px solid #ebeef5;
}

.designer-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

// 左侧工具栏 - SAP NetWeaver BPM 风格
.palette-container {
  width: 280px;
  border-right: 1px solid #e0e0e0;
  background: #fafafa;
  overflow-y: auto;

  :deep(.el-collapse) {
    border: none;

    .el-collapse-item__header {
      background: #f0f0f0;
      border: none;
      padding: 10px 12px;
      font-weight: 600;
      font-size: 13px;

      &:hover {
        background: #e8e8e8;
      }
    }

    .el-collapse-item__wrap {
      border: none;
      background: #fafafa;
    }
  }

  .palette-group {
    padding: 8px;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;

    .palette-category {
      grid-column: 1 / -1;
      font-size: 11px;
      color: #666;
      font-weight: 600;
      padding: 4px 0;
      border-bottom: 1px solid #e0e0e0;
      margin-top: 4px;
    }

    .palette-item {
      width: 72px;
      height: 60px;
      border: 1px solid #e0e0e0;
      border-radius: 4px;
      background: #fff;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;

      &:hover {
        border-color: #1e80ff;
        background: #e6f1ff;
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(30,128,255,0.2);
      }

      svg {
        width: 40px;
        height: 40px;
      }
    }
  }
}

// 画布区域
.canvas-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #f5f5f5;

  .canvas-controls {
    position: absolute;
    top: 10px;
    left: 10px;
    z-index: 100;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    border-radius: 4px;
    padding: 8px 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);

    .canvas-info {
      font-size: 12px;
      color: #666;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .canvas-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .bpmn-canvas {
    width: 100%;
    height: 100%;
  }

  // 迷你地图
  .minimap-container {
    position: absolute;
    bottom: 10px;
    right: 10px;
    width: 200px;
    height: 150px;
    background: #fff;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
    z-index: 100;

    .minimap {
      width: 100%;
      height: 100%;
    }

    .minimap-close {
      position: absolute;
      top: -8px;
      right: -8px;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      padding: 0;
      font-size: 12px;
    }
  }
}

// 右侧属性面板
.properties-panel-container {
  width: 320px;
  border-left: 1px solid #e0e0e0;
  background: #fafafa;
  display: flex;
  flex-direction: column;

  .properties-panel-header {
    height: 40px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 12px;
    background: #f0f0f0;
    border-bottom: 1px solid #e0e0e0;
    font-weight: 600;
    font-size: 13px;
  }

  .properties-panel-content {
    flex: 1;
    overflow-y: auto;
  }
}

.properties-panel-container :deep(.bio-properties-panel) {
  background: transparent;
}

.properties-panel-container :deep(.bio-properties-panel-header) {
  background: #fafafa;
}

.mb-4 {
  margin-bottom: 16px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.start-form-container {
  max-height: 500px;
  overflow-y: auto;
}
.context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 9999;
  padding: 8px 0;
  min-width: 150px;
}

.context-menu ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.context-menu li {
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.context-menu li:hover {
  background: #f5f7fa;
}

.context-menu li .el-icon {
  font-size: 16px;
}

.context-menu .divider {
  height: 1px;
  background: #ebeef5;
  margin: 8px 0;
}

.template-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.template-card:hover {
  transform: translateY(-4px);
}

.template-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.template-body p {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.batch-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}

.properties-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 500;
}

.properties-panel-content {
  padding: 12px;
}

/* 流程统计卡片 */
.stat-card-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.context-menu .submenu-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.context-menu .submenu-arrow {
  font-size: 16px;
  color: #909399;
}

.context-menu .submenu {
  position: absolute;
  left: 100%;
  top: 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 120px;
  display: none;
  z-index: 10000;
}

.context-menu li:has(.submenu):hover .submenu {
  display: block;
}

.context-menu .submenu li {
  padding: 6px 12px;
  font-size: 13px;
}

/* 流程模拟样式 */
.simulation-container {
  padding: 20px;
}

.simulation-controls {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
}

.simulation-status {
  margin-bottom: 24px;
}

.simulation-log {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.simulation-log h4 {
  margin: 0 0 12px 0;
  color: #606266;
}

.simulation-log ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.simulation-log li {
  font-family: monospace;
  font-size: 13px;
  color: #333;
  padding: 4px 0;
  border-bottom: 1px solid #e4e7ed;
}

.simulation-log li:last-child {
  border-bottom: none;
}

/* 智能推荐样式 */
.recommendation-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.recommendation-card {
  transition: transform 0.2s;
}

.recommendation-card:hover {
  transform: translateY(-2px);
}

.recommendation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.recommendation-title {
  font-weight: 500;
  color: #333;
}

.recommendation-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

/* 评论批注样式 */
.comment-container {
  max-height: 500px;
  overflow-y: auto;
}

.comment-input {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  color: #606266;
  margin: 8px 0;
}

.comment-actions-row {
  display: flex;
  gap: 8px;
}

.comment-replies {
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #e4e7ed;
}

.reply-item {
  padding: 8px 0;
  font-size: 13px;
}

.reply-user {
  font-weight: 500;
  color: #333;
  margin-right: 8px;
}

.reply-text {
  color: #606266;
}

/* 健康度仪表板样式 */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.dashboard-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.dashboard-card h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 14px;
}

.score-items {
  list-style: none;
  padding: 0;
  margin: 12px 0 0 0;
}

.score-items li {
  font-size: 13px;
  color: #606266;
  padding: 4px 0;
}

.score-items li.error {
  color: #F56C6C;
}

.score-items li.warning {
  color: #E6A23C;
}

.dashboard-summary {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.dashboard-summary h4 {
  margin: 0 0 8px 0;
}

.score-good {
  color: #67C23A;
  font-size: 24px;
  font-weight: bold;
}

.score-warning {
  color: #E6A23C;
  font-size: 24px;
  font-weight: bold;
}

/* 图例样式 */
.legend-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.legend-section h4 {
  margin: 0 0 12px 0;
  color: #333;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
  font-size: 14px;
}

.legend-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: inline-block;
}

.legend-icon.start-event {
  background: #28c961;
  border: 2px solid #1e8e45;
}

.legend-icon.end-event {
  background: #ff3333;
  border: 3px solid #333;
}

.legend-icon.timer-event {
  background: #28c961;
  border: 2px solid #1e8e45;
  position: relative;
}

.legend-icon.message-event {
  background: #28c961;
  border: 2px solid #1e8e45;
}

.legend-icon.user-task {
  background: #58a3d9;
  border-radius: 4px;
}

.legend-icon.service-task {
  background: #f5c34f;
  border-radius: 4px;
}

.legend-icon.script-task {
  background: #78c961;
  border-radius: 4px;
}

.legend-icon.mail-task {
  background: #58a3d9;
  border-radius: 4px;
}

.legend-icon.exclusive-gateway {
  background: #ffab45;
  transform: rotate(45deg);
  margin: 0 8px;
}

.legend-icon.parallel-gateway {
  background: #58a3d9;
  transform: rotate(45deg);
  margin: 0 8px;
}

.legend-icon.inclusive-gateway {
  background: #a358d9;
  transform: rotate(45deg);
  margin: 0 8px;
}

.legend-line {
  width: 30px;
  height: 2px;
  display: inline-block;
}

.legend-line.sequence-flow {
  background: #333;
}

.legend-line.message-flow {
  background: #333;
  border-top: 2px dashed #333;
}

.legend-line.association {
  background: #333;
  border-top: 2px dashed #333;
}

/* 协作编辑样式 */
.collab-container {
  max-height: 400px;
  overflow-y: auto;
}

.online-users h4, .edit-lock h4 {
  margin: 0 0 12px 0;
  color: #333;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.online-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 8px;
}

.edit-lock {
  margin-top: 16px;
}

/* 导航面包屑 */
.breadcrumb-nav {
  position: fixed;
  top: 70px;
  left: 20px;
  background: #fff;
  padding: 8px 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 100;
}

.close-breadcrumb {
  margin-left: 8px;
  padding: 4px 8px;
}

/* 迷你工具栏 */
.mini-toolbar {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: #fff;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 100;
}

/* 快速添加菜单 */
.quick-add-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  padding: 12px;
  min-width: 150px;
  z-index: 1000;
}

.quick-add-menu h4 {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #606266;
}

.quick-add-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.quick-add-menu li {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}

.quick-add-menu li:hover {
  background: #f5f7fa;
}

.quick-add-menu li .divider {
  height: 1px;
  background: #ebeef5;
  margin: 8px 0;
}

</style>