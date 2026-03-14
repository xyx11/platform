<template>
  <div :class="{ 'hidden': hidden }" class="pagination-container">
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      background
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits } from 'vue'

const props = defineProps({
  page: {
    type: Number,
    default: 1
  },
  limit: {
    type: Number,
    default: 10
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 30, 50]
  },
  total: {
    type: Number,
    default: 0
  },
  hidden: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['pagination', 'update:page', 'update:limit'])

const currentPage = computed({
  get() {
    return props.page
  },
  set(val) {
    emit('update:page', val)
  }
})

const pageSize = computed({
  get() {
    return props.limit
  },
  set(val) {
    emit('update:limit', val)
  }
})

const handleSizeChange = (val) => {
  emit('pagination', { page: props.page, limit: val })
}

const handleCurrentChange = (val) => {
  emit('pagination', { page: val, limit: props.limit })
}
</script>

<style scoped lang="scss">
.pagination-container {
  background: #fff;
  padding: 16px 32px;

  &.hidden {
    display: none;
  }
}
</style>