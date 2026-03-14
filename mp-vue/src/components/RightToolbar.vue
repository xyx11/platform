<template>
  <div class="right-toolbar">
    <div class="right-toolbar-item">
      <el-tooltip content="刷新" placement="top">
        <el-button link type="primary" icon="Refresh" @click="handleQueryTable" />
      </el-tooltip>
    </div>
    <div class="right-toolbar-item" v-if="showSearch !== undefined">
      <el-tooltip :content="showSearch ? '隐藏搜索' : '显示搜索'" placement="top">
        <el-button link type="primary" :icon="showSearch ? 'Search' : 'Close'" @click="toggleSearch" />
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  showSearch: {
    type: Boolean,
    default: undefined
  }
})

const emit = defineEmits(['queryTable', 'update:showSearch'])

const handleQueryTable = () => {
  emit('queryTable')
}

const toggleSearch = () => {
  emit('update:showSearch', !props.showSearch)
}
</script>

<style scoped lang="scss">
.right-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;

  .right-toolbar-item {
    display: flex;
    align-items: center;
  }
}
</style>