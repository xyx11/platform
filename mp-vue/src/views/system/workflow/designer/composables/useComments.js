/**
 * 评论批注组合式函数
 * 封装流程设计器的评论、批注、回复等功能
 */

import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 评论类型
 */
const COMMENT_TYPE = {
  COMMENT: 'comment',     // 普通评论
  QUESTION: 'question',   // 问题
  SUGGESTION: 'suggestion', // 建议
  ANNOTATION: 'annotation'  // 批注
}

/**
 * 评论类型配置
 */
const COMMENT_TYPE_CONFIG = {
  [COMMENT_TYPE.COMMENT]: { label: '评论', color: '#409EFF', icon: 'ChatRound' },
  [COMMENT_TYPE.QUESTION]: { label: '问题', color: '#F56C6C', icon: 'QuestionFilled' },
  [COMMENT_TYPE.SUGGESTION]: { label: '建议', color: '#E6A23C', icon: 'Light' },
  [COMMENT_TYPE.ANNOTATION]: { label: '批注', color: '#909399', icon: 'Edit' }
}

/**
 * 评论批注
 * @param {Object} options - 配置选项
 * @returns {Object} 评论批注 API
 */
export function useComments(options = {}) {
  const { bpmnModeler, processInfo } = options

  // 状态
  const commentVisible = ref(false)
  const comments = ref([])
  const selectedComment = ref(null)
  const commentFilter = ref('all') // all, comment, question, suggestion, annotation

  // 新评论表单
  const newComment = reactive({
    content: '',
    type: COMMENT_TYPE.COMMENT,
    elementId: null,
    elementName: null
  })

  // 回复表单
  const replyForm = reactive({
    content: '',
    parentId: null
  })

  // 统计
  const stats = computed(() => {
    return {
      total: comments.value.length,
      [COMMENT_TYPE.COMMENT]: comments.value.filter(c => c.type === COMMENT_TYPE.COMMENT).length,
      [COMMENT_TYPE.QUESTION]: comments.value.filter(c => c.type === COMMENT_TYPE.QUESTION).length,
      [COMMENT_TYPE.SUGGESTION]: comments.value.filter(c => c.type === COMMENT_TYPE.SUGGESTION).length,
      [COMMENT_TYPE.ANNOTATION]: comments.value.filter(c => c.type === COMMENT_TYPE.ANNOTATION).length,
      unresolved: comments.value.filter(c => c.type === COMMENT_TYPE.QUESTION && !c.resolved).length
    }
  })

  // 过滤后的评论列表
  const filteredComments = computed(() => {
    if (commentFilter.value === 'all') return comments.value
    return comments.value.filter(c => c.type === commentFilter.value)
  })

  /**
   * 打开评论面板
   */
  const openComments = () => {
    loadComments()
    commentVisible.value = true
  }

  /**
   * 关闭评论面板
   */
  const closeComments = () => {
    commentVisible.value = false
  }

  /**
   * 加载评论
   */
  const loadComments = () => {
    if (!processInfo?.value?.id) {
      // 使用示例评论
      comments.value = getSampleComments()
      return
    }

    // 从 localStorage 加载
    const saved = localStorage.getItem('workflow_comments_' + processInfo.value.id)
    if (saved) {
      try {
        comments.value = JSON.parse(saved)
      } catch (e) {
        comments.value = getSampleComments()
      }
    } else {
      comments.value = getSampleComments()
    }
  }

  /**
   * 获取示例评论
   */
  const getSampleComments = () => {
    return [
      {
        id: 1,
        userId: 1,
        userName: '张三',
        userAvatar: null,
        type: COMMENT_TYPE.QUESTION,
        content: '这个审批流程是否需要添加财务审核环节？',
        elementId: 'Task_1',
        elementName: '部门审批',
        resolved: false,
        createdAt: '2024-01-10 10:30',
        updatedAt: '2024-01-10 10:30',
        replies: [
          {
            id: 11,
            userId: 2,
            userName: '李四',
            content: '建议添加，金额超过 1 万的需要财务审核',
            createdAt: '2024-01-10 11:00'
          }
        ]
      },
      {
        id: 2,
        userId: 2,
        userName: '李四',
        userAvatar: null,
        type: COMMENT_TYPE.SUGGESTION,
        content: '可以考虑将这个并行网关改为排他网关，根据实际情况只执行一个分支',
        elementId: 'Gateway_1',
        elementName: '并行网关',
        resolved: true,
        createdAt: '2024-01-10 09:00',
        updatedAt: '2024-01-10 14:00',
        replies: []
      },
      {
        id: 3,
        userId: 3,
        userName: '王五',
        userAvatar: null,
        type: COMMENT_TYPE.ANNOTATION,
        content: '此处需要添加超时处理逻辑',
        elementId: 'Task_2',
        elementName: '审批处理',
        resolved: false,
        createdAt: '2024-01-09 16:00',
        updatedAt: '2024-01-09 16:00',
        replies: []
      }
    ]
  }

  /**
   * 保存评论
   */
  const saveComments = () => {
    if (!processInfo?.value?.id) {
      ElMessage.warning('请先保存流程')
      return false
    }

    localStorage.setItem('workflow_comments_' + processInfo.value.id, JSON.stringify(comments.value))
    return true
  }

  /**
   * 添加评论
   */
  const addComment = () => {
    if (!newComment.content.trim()) {
      ElMessage.warning('请输入评论内容')
      return
    }

    const comment = {
      id: Date.now(),
      userId: getCurrentUserId(),
      userName: getCurrentUserName(),
      userAvatar: null,
      type: newComment.type,
      content: newComment.content,
      elementId: newComment.elementId,
      elementName: newComment.elementName,
      resolved: false,
      createdAt: new Date().toLocaleString('zh-CN'),
      updatedAt: new Date().toLocaleString('zh-CN'),
      replies: []
    }

    comments.value.unshift(comment)
    saveComments()

    // 重置表单
    newComment.content = ''
    newComment.type = COMMENT_TYPE.COMMENT
    newComment.elementId = null
    newComment.elementName = null

    ElMessage.success('评论已添加')
  }

  /**
   * 回复评论
   */
  const replyComment = (parentId) => {
    if (!replyForm.content.trim()) {
      ElMessage.warning('请输入回复内容')
      return
    }

    const parentComment = comments.value.find(c => c.id === parentId)
    if (!parentComment) return

    const reply = {
      id: Date.now(),
      userId: getCurrentUserId(),
      userName: getCurrentUserName(),
      content: replyForm.content,
      createdAt: new Date().toLocaleString('zh-CN')
    }

    if (!parentComment.replies) {
      parentComment.replies = []
    }
    parentComment.replies.push(reply)
    parentComment.updatedAt = new Date().toLocaleString('zh-CN')

    saveComments()

    // 重置表单
    replyForm.content = ''
    replyForm.parentId = null

    ElMessage.success('回复已发送')
  }

  /**
   * 删除评论
   */
  const deleteComment = async (commentId) => {
    try {
      await ElMessageBox.confirm('确定要删除此评论吗？', '提示', {
        type: 'warning'
      })
    } catch {
      return
    }

    const index = comments.value.findIndex(c => c.id === commentId)
    if (index === -1) return

    comments.value.splice(index, 1)
    saveComments()

    ElMessage.success('评论已删除')
  }

  /**
   * 删除回复
   */
  const deleteReply = async (commentId, replyId) => {
    try {
      await ElMessageBox.confirm('确定要删除此回复吗？', '提示', {
        type: 'warning'
      })
    } catch {
      return
    }

    const comment = comments.value.find(c => c.id === commentId)
    if (!comment || !comment.replies) return

    const index = comment.replies.findIndex(r => r.id === replyId)
    if (index === -1) return

    comment.replies.splice(index, 1)
    comment.updatedAt = new Date().toLocaleString('zh-CN')
    saveComments()

    ElMessage.success('回复已删除')
  }

  /**
   * 标记问题为已解决
   */
  const resolveQuestion = (commentId) => {
    const comment = comments.value.find(c => c.id === commentId)
    if (!comment) return

    comment.resolved = true
    comment.updatedAt = new Date().toLocaleString('zh-CN')
    saveComments()

    ElMessage.success('问题已标记为已解决')
  }

  /**
   * 重新打开问题
   */
  const reopenQuestion = (commentId) => {
    const comment = comments.value.find(c => c.id === commentId)
    if (!comment) return

    comment.resolved = false
    comment.updatedAt = new Date().toLocaleString('zh-CN')
    saveComments()

    ElMessage.success('问题已重新打开')
  }

  /**
   * 评论/回复关联到元素
   */
  const linkToElement = (elementId, elementName) => {
    if (!bpmnModeler?.value) return

    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const element = elementRegistry.get(elementId)

    if (element) {
      const selection = bpmnModeler.value.get('selection')
      const canvas = bpmnModeler.value.get('canvas')

      selection.select(element)
      canvas.scrollToElement(element, 300)

      // 高亮元素
      highlightElement(element)
    }
  }

  /**
   * 高亮元素
   */
  const highlightElement = (element) => {
    if (!bpmnModeler?.value) return

    const canvas = bpmnModeler.value.get('canvas')
    const gfx = canvas._svg.querySelector(`[data-element-id="${element.id}"]`)

    if (gfx) {
      const originalStroke = gfx.getAttribute('stroke') || '#000'
      const originalStrokeWidth = gfx.getAttribute('stroke-width') || '2'

      gfx.setAttribute('stroke', '#E6A23C')
      gfx.setAttribute('stroke-width', '4')
      gfx.style.filter = 'drop-shadow(0 0 8px #E6A23C)'

      setTimeout(() => {
        gfx.setAttribute('stroke', originalStroke)
        gfx.setAttribute('stroke-width', originalStrokeWidth)
        gfx.style.filter = ''
      }, 2000)
    }
  }

  /**
   * 获取当前用户 ID
   */
  const getCurrentUserId = () => {
    // 从用户信息获取，这里简化处理
    return 1
  }

  /**
   * 获取当前用户名
   */
  const getCurrentUserName = () => {
    // 从用户信息获取，这里简化处理
    return '当前用户'
  }

  /**
   * 获取评论类型名称
   */
  const getCommentTypeName = (type) => {
    return COMMENT_TYPE_CONFIG[type]?.label || type
  }

  /**
   * 获取评论类型颜色
   */
  const getCommentTypeColor = (type) => {
    return COMMENT_TYPE_CONFIG[type]?.color || '#909399'
  }

  /**
   * 获取评论类型图标
   */
  const getCommentTypeIcon = (type) => {
    return COMMENT_TYPE_CONFIG[type]?.icon || 'ChatRound'
  }

  /**
   * 清空所有评论
   */
  const clearAllComments = async () => {
    try {
      await ElMessageBox.confirm('确定要清空所有评论吗？此操作不可恢复！', '警告', {
        type: 'warning'
      })
    } catch {
      return
    }

    comments.value = []
    saveComments()

    ElMessage.success('已清空所有评论')
  }

  /**
   * 导出评论
   */
  const exportComments = () => {
    if (comments.value.length === 0) {
      ElMessage.warning('暂无评论可导出')
      return
    }

    const content = JSON.stringify(comments.value, null, 2)
    const blob = new Blob([content], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${processInfo?.value?.name || 'workflow'}_comments.json`
    a.click()
    URL.revokeObjectURL(url)

    ElMessage.success('评论已导出')
  }

  return {
    // 状态
    commentVisible,
    comments,
    selectedComment,
    commentFilter,
    newComment,
    replyForm,
    stats,
    filteredComments,

    // 方法
    openComments,
    closeComments,
    loadComments,
    saveComments,
    addComment,
    replyComment,
    deleteComment,
    deleteReply,
    resolveQuestion,
    reopenQuestion,
    linkToElement,
    highlightElement,
    getCommentTypeName,
    getCommentTypeColor,
    getCommentTypeIcon,
    clearAllComments,
    exportComments
  }
}

export { COMMENT_TYPE, COMMENT_TYPE_CONFIG }