<template>
  <div id="app">
    <el-container class="layout-container">
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h2>参数监测平台</h2>
        </div>
        <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
          <el-menu-item index="/">
            <el-icon><DataBoard /></el-icon>
            <span>驾驶舱</span>
          </el-menu-item>
          <el-menu-item index="/parameters">
            <el-icon><Setting /></el-icon>
            <span>参数管理</span>
          </el-menu-item>
          <el-menu-item index="/datasources">
            <el-icon><Connection /></el-icon>
            <span>数据源</span>
          </el-menu-item>
          <el-menu-item index="/rules">
            <el-icon><WarnTriangleFilled /></el-icon>
            <span>规则配置</span>
          </el-menu-item>
          <el-menu-item index="/alarms">
            <el-icon><Bell /></el-icon>
            <span>告警记录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header class="header">
          <div class="header-title">监督驾驶舱</div>
          <div class="header-info">
            <el-tag type="success">系统运行正常</el-tag>
            <span class="time">{{ currentTime }}</span>
          </div>
        </el-header>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { DataBoard, Setting, Connection, WarnTriangleFilled, Bell } from '@element-plus/icons-vue'

const currentTime = ref('')

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN')
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
})

onUnmounted(() => {
  // 清理定时器
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: #fff;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #3d4a5a;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.time {
  color: #666;
  font-size: 14px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
