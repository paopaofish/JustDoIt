<template>
  <div class="dashboard-container">
    <!-- KPI 卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon" style="background-color: #409EFF;">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">监测参数总数</div>
              <div class="kpi-value">{{ stats.totalParameters }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon" style="background-color: #67C23A;">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">正常运行</div>
              <div class="kpi-value">{{ stats.normalCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon" style="background-color: #E6A23C;">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">警告数量</div>
              <div class="kpi-value">{{ stats.warningCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon" style="background-color: #F56C6C;">
              <el-icon><Close /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">严重告警</div>
              <div class="kpi-value">{{ stats.criticalCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <!-- 实时趋势图 -->
      <el-col :span="16">
        <el-card shadow="always">
          <template #header>
            <div class="card-header">
              <span>实时参数趋势</span>
              <el-select v-model="selectedParam" size="small" style="width: 200px;">
                <el-option label="温度传感器" value="temp_01" />
                <el-option label="压力传感器" value="pressure_01" />
                <el-option label="流量传感器" value="flow_01" />
              </el-select>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 告警分布 -->
      <el-col :span="8">
        <el-card shadow="always">
          <template #header>
            <span>告警分布</span>
          </template>
          <div ref="alertChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新告警列表 -->
    <el-row :gutter="20" class="mt-4">
      <el-col :span="24">
        <el-card shadow="always">
          <template #header>
            <div class="card-header">
              <span>最新告警</span>
              <el-button type="primary" size="small" @click="loadAlerts">刷新</el-button>
            </div>
          </template>
          <el-table :data="alerts" stripe style="width: 100%">
            <el-table-column prop="parameter" label="参数" width="150" />
            <el-table-column prop="message" label="告警信息" />
            <el-table-column prop="severity" label="严重程度" width="100">
              <template #default="{ row }">
                <el-tag :type="getSeverityType(row.severity)">{{ row.severity }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Connection, CircleCheck, Warning, Close } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

// 统计数据
const stats = ref({
  totalParameters: 128,
  normalCount: 115,
  warningCount: 10,
  criticalCount: 3,
})

// 选择的参数
const selectedParam = ref('temp_01')

// 告警列表
const alerts = ref([
  { parameter: '温度传感器_01', message: '温度超过上限阈值 (85°C)', severity: 'critical', status: 'active', created_at: '2024-01-15 10:30:00' },
  { parameter: '压力传感器_02', message: '压力异常波动', severity: 'warning', status: 'acknowledged', created_at: '2024-01-15 09:15:00' },
  { parameter: '流量传感器_01', message: '流量低于下限阈值', severity: 'warning', status: 'resolved', created_at: '2024-01-15 08:00:00' },
])

// 图表引用
const trendChartRef = ref<HTMLElement>()
const alertChartRef = ref<HTMLElement>()
let trendChart: ECharts | null = null
let alertChart: ECharts | null = null

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['10:00', '10:05', '10:10', '10:15', '10:20', '10:25', '10:30'],
    },
    yAxis: { type: 'value', name: '温度 (°C)' },
    series: [
      {
        name: '实时值',
        type: 'line',
        smooth: true,
        data: [72, 73, 75, 78, 82, 85, 83],
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.5)' },
            { offset: 1, color: 'rgba(64,158,255,0.1)' },
          ]),
        },
      },
    ],
  }
  
  trendChart.setOption(option)
}

// 初始化告警分布图
const initAlertChart = () => {
  if (!alertChartRef.value) return
  
  alertChart = echarts.init(alertChartRef.value)
  
  const option = {
    tooltip: { trigger: 'item' },
    series: [
      {
        name: '告警分布',
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 3, name: '严重', itemStyle: { color: '#F56C6C' } },
          { value: 10, name: '警告', itemStyle: { color: '#E6A23C' } },
          { value: 115, name: '正常', itemStyle: { color: '#67C23A' } },
        ],
        label: { show: false },
      },
    ],
  }
  
  alertChart.setOption(option)
}

// 获取严重程度类型
const getSeverityType = (severity: string) => {
  const typeMap: Record<string, string> = {
    critical: 'danger',
    error: 'danger',
    warning: 'warning',
    info: 'info',
  }
  return typeMap[severity] || 'info'
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    active: 'danger',
    acknowledged: 'warning',
    resolved: 'success',
  }
  return typeMap[status] || 'info'
}

// 加载告警数据
const loadAlerts = () => {
  // 实际应从 API 加载
  console.log('Loading alerts...')
}

// 监听参数变化
watch(selectedParam, () => {
  // 更新趋势图数据
  initTrendChart()
})

onMounted(() => {
  initTrendChart()
  initAlertChart()
  
  // 窗口大小变化时重绘图表
  window.addEventListener('resize', () => {
    trendChart?.resize()
    alertChart?.resize()
  })
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

.mb-4 {
  margin-bottom: 20px;
}

.mt-4 {
  margin-top: 20px;
}

.kpi-card {
  border-radius: 8px;
}

.kpi-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.kpi-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

.kpi-info {
  flex: 1;
}

.kpi-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.kpi-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>
