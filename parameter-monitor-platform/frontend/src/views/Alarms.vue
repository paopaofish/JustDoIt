<template>
  <div class="simple-view">
    <el-card>
      <template #header>
        <span>{{ title }}</span>
      </template>
      <el-empty :description="description" />
    </el-card>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '页面' },
  description: { type: String, default: '功能开发中...' }
})
</script>

<style scoped>
.simple-view {
  padding: 20px;
}
</style>
