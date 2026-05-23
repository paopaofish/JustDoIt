<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="app-aside">
      <div class="logo">
        <h2>参数监测平台</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>监督驾驶舱</span>
        </el-menu-item>
        <el-menu-item index="/parameters">
          <el-icon><Connection /></el-icon>
          <span>参数管理</span>
        </el-menu-item>
        <el-menu-item index="/data-sources">
          <el-icon><Database /></el-icon>
          <span>数据源管理</span>
        </el-menu-item>
        <el-menu-item index="/monitoring">
          <el-icon><Warning /></el-icon>
          <span>监控规则</span>
        </el-menu-item>
        <el-menu-item index="/alerts">
          <el-icon><Bell /></el-icon>
          <span>告警事件</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag type="success" size="small">系统运行正常</el-tag>
          <el-dropdown>
            <span class="user-info">
              管理员 <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { 
  DataBoard, 
  Connection, 
  Database, 
  Warning, 
  Bell,
  ArrowDown 
} from '@element-plus/icons-vue'

const route = useRoute()

const currentTitle = computed(() => {
  const titleMap: Record<string, string> = {
    '/dashboard': '监督驾驶舱',
    '/parameters': '参数管理',
    '/data-sources': '数据源管理',
    '/monitoring': '监控规则',
    '/alerts': '告警事件',
  }
  return titleMap[route.path] || '参数监测平台'
})
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
}

.logo h2 {
  color: #fff;
  font-size: 18px;
  margin: 0;
}

.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.app-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
