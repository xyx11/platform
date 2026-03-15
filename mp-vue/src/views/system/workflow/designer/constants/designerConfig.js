/**
 * 流程设计器配置常量
 */

/**
 * 设计师 UI 配置
 */
export const DESIGNER_CONFIG = {
  palette: {
    width: 260,
    minWidth: 200,
    maxWidth: 320
  },
  propertiesPanel: {
    width: 340,
    minWidth: 280,
    maxWidth: 400
  },
  grid: {
    size: 20,
    color: 'rgba(64, 158, 255, 0.03)'
  },
  zoom: {
    min: 0.1,
    max: 4,
    step: 0.1
  },
  autoSave: {
    enabled: true,
    interval: 30000, // 30 秒
    maxBackups: 5
  }
}

/**
 * 默认快捷键配置
 */
export const DEFAULT_SHORTCUTS = {
  'Ctrl-N': { action: 'newDiagram', desc: '新建流程' },
  'Ctrl-O': { action: 'openFile', desc: '打开文件' },
  'Ctrl-S': { action: 'saveDiagram', desc: '保存流程' },
  'Ctrl-Shift-S': { action: 'saveAsDiagram', desc: '另存为' },
  'Ctrl-D': { action: 'duplicateProcess', desc: '复制流程' },
  'Ctrl-Z': { action: 'undo', desc: '撤销' },
  'Ctrl-Y': { action: 'redo', desc: '重做' },
  'Delete': { action: 'deleteSelected', desc: '删除' },
  'Backspace': { action: 'deleteSelected', desc: '删除' },
  '+': { action: 'zoomIn', desc: '放大' },
  '-': { action: 'zoomOut', desc: '缩小' },
  '0': { action: 'zoomFit', desc: '适应画布' },
  'Ctrl-P': { action: 'previewDiagram', desc: '预览' },
  'Ctrl-B': { action: 'deployDiagram', desc: '部署' },
  'Ctrl-H': { action: 'showHelp', desc: '帮助' },
  'Ctrl-F': { action: 'searchNode', desc: '搜索节点' },
  'F5': { action: 'refreshDiagram', desc: '刷新' }
}

/**
 * 流程分类选项
 */
export const PROCESS_CATEGORIES = [
  { value: 'HR', label: '人力资源' },
  { value: 'FIN', label: '财务管理' },
  { value: 'PROC', label: '采购管理' },
  { value: 'SALES', label: '销售管理' },
  { value: 'PROD', label: '生产管理' },
  { value: 'ADMIN', label: '行政管理' }
]

/**
 * 模板分类选项
 */
export const TEMPLATE_CATEGORIES = [
  { value: 'hr', label: '人事流程' },
  { value: 'finance', label: '财务流程' },
  { value: 'admin', label: '行政流程' },
  { value: 'purchase', label: '采购流程' },
  { value: 'other', label: '其他' }
]

/**
 * 处理人类型选项
 */
export const ASSIGNEE_TYPES = [
  { value: 'user', label: '指定用户' },
  { value: 'deptManager', label: '部门负责人' },
  { value: 'position', label: '岗位' },
  { value: 'variable', label: '变量' }
]

/**
 * 评论类型选项
 */
export const COMMENT_TYPES = [
  { value: 'comment', label: '普通评论' },
  { value: 'question', label: '问题' },
  { value: 'suggestion', label: '建议' },
  { value: 'annotation', label: '批注' }
]

/**
 * 导出格式选项
 */
export const EXPORT_FORMATS = [
  { value: 'bpmn', label: 'BPMN 2.0 XML' },
  { value: 'svg', label: 'SVG 图片' },
  { value: 'png', label: 'PNG 图片' },
  { value: 'pdf', label: 'PDF 文档' },
  { value: 'json', label: 'JSON 模板' }
]