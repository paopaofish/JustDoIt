<template>
  <div class="dashboard">
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalParameters || 0 }}</div>
              <div class="stat-label">总参数数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.activeParameters || 0 }}</div>
              <div class="stat-label">活跃参数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.alarmCount || 0 }}</div>
              <div class="stat-label">当前告警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon><PieChart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.healthScore?.toFixed(1) || 100 }}</div>
              <div class="stat-label">健康评分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>实时趋势监控</span>
              <el-select v-model="selectedParam" placeholder="选择参数" size="small" @change="loadTrendData">
                <el-option v-for="param in parameters" :key="param.id" :label="param.name" :value="param.id" />
              </el-select>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="alarm-card">
          <template #header>
            <div class="card-header">
              <span>最新告警</span>
              <el-button type="primary" link @click="$router.push('/alarms')">查看全部</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item 
              v-for="alarm in recentAlarms" 
              :key="alarm.id"
              :type="getAlarmType(alarm.alertLevel)"
              :timestamp="formatTime(alarm.alarmTime)"
              placement="top"
            >
              <el-card>
                <h4>{{ alarm.parameterName }}</h4>
                <p>{{ alarm.message }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据源状态 -->
    <el-row :gutter="20" class="datasource-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>数据源状态</span>
          </template>
          <el-table :data="dataSources" style="width: 100%">
            <el-table-column prop="name" label="名称" width="180" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getTypeTag(row.type)">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="endpoint" label="连接地址" />
            <el-table-column prop="enabled" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">
                  {{ row.enabled ? '已启用' : '已禁用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { DataLine, CircleCheck, Warning, PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import axios from 'axios'

const API_BASE = '/api/monitoring'

const overview = ref({})
const parameters = ref([])
const dataSources = ref([])
const recentAlarms = ref([])
const selectedParam = ref(null)
const trendChartRef = ref(null)
let trendChart = null

// 加载概览数据
const loadOverview = async () => {
  try {
    const res = await axios.get(`${API_BASE}/dashboard/overview`)
    overview.value = res.data
  } catch (error) {
    console.error('加载概览数据失败:', error)
  }
}

// 加载参数列表
const loadParameters = async () => {
  try {
    const res = await axios.get(`${API_BASE}/parameters`)
    parameters.value = res.data
    if (parameters.value.length > 0 && !selectedParam.value) {
      selectedParam.value = parameters.value[0].id
      loadTrendData()
    }
  } catch (error) {
    console.error('加载参数列表失败:', error)
  }
}

// 加载数据源
const loadDataSources = async () => {
  try {
    const res = await axios.get(`${API_BASE}/datasources`)
    dataSources.value = res.data
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

// 加载最新告警
const loadRecentAlarms = async () => {
  try {
    const res = await axios.get(`${API_BASE}/alarms?status=NEW`)
    recentAlarms.value = res.data.slice(0, 5)
  } catch (error) {
    console.error('加载告警失败:', error)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  if (!selectedParam.value || !trendChartRef.value) return
  
  await nextTick()
  
  try {
    const endTime = new Date()
    const startTime = new Date(endTime.getTime() - 3600000) // 1小时前
    
    const res = await axios.get(`${API_BASE}/data/${selectedParam.value}`, {
      params: {
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString()
      }
    })
    
    const data = res.data
    renderTrendChart(data)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 渲染趋势图表
const renderTrendChart = (data) => {
  if (!trendChartRef.value) return
  
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.timestamp.substring(11, 19))
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: data.map(item => item.value),
      type: 'line',
      smooth: true,
      showSymbol: false
    }]
  }
  
  trendChart.setOption(option)
  
  window.addEventListener('resize', () => {
    trendChart.resize()
  })
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN')
}

// 获取告警类型
const getAlarmType = (level) => {
  const typeMap = {
    'INFO': 'info',
    'WARNING': 'warning',
    'ERROR': 'danger',
    'CRITICAL': 'danger'
  }
  return typeMap[level] || 'info'
}

// 获取类型标签
const getTypeTag = (type) => {
  const typeMap = {
    'IOTDB': 'success',
    'MYSQL': 'primary',
    'OPC_UA': 'warning',
    'OSI_PI': 'info'
  }
  return typeMap[type] || ''
}

onMounted(() => {
  loadOverview()
  loadParameters()
  loadDataSources()
  loadRecentAlarms()
  
  // 定时刷新
  setInterval(() => {
    loadOverview()
    loadRecentAlarms()
    if (selectedParam.value) {
      loadTrendData()
    }
  }, 30000) // 30秒刷新一次
})
</script>

<style scoped>
.dashboard {
  padding: 10px;
}

.overview-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.alarm-card {
  height: 400px;
  overflow-y: auto;
}

.datasource-row {
  margin-bottom: 20px;
}
</style>
