<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据源管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 新增数据源
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="searchForm.sourceType" placeholder="全部" clearable>
            <el-option label="IoTDB" value="iotdb" />
            <el-option label="OPC UA" value="opcua" />
            <el-option label="MySQL" value="mysql" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isActive" placeholder="全部" clearable>
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDataSources">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据列表 -->
      <el-table :data="dataSources" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="source_type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getSourceTypeTag(row.source_type)">{{ row.source_type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="is_active" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="testConnection(row)">测试连接</el-button>
            <el-button size="small" type="primary" @click="editDataSource(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDataSource(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="formData.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="formData.source_type" placeholder="请选择类型" style="width: 100%;">
            <el-option label="IoTDB" value="iotdb" />
            <el-option label="OPC UA" value="opcua" />
            <el-option label="MySQL" value="mysql" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置" required>
          <el-input
            v-model="formData.configStr"
            type="textarea"
            :rows="8"
            placeholder='请输入 JSON 格式配置，例如：{"host": "localhost", "port": 6667}'
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.is_active" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  sourceType: '',
  isActive: null,
})

// 数据列表
const dataSources = ref([
  { id: 1, name: '工厂 IoTDB', source_type: 'iotdb', is_active: true, created_at: '2024-01-10 09:00:00' },
  { id: 2, name: 'PLC 产线 1', source_type: 'opcua', is_active: true, created_at: '2024-01-11 10:30:00' },
  { id: 3, name: '生产数据库', source_type: 'mysql', is_active: false, created_at: '2024-01-12 14:20:00' },
])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据源')
const isEdit = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  source_type: '',
  configStr: '',
  is_active: true,
})

// 获取类型标签
const getSourceTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    iotdb: 'success',
    opcua: 'warning',
    mysql: 'primary',
  }
  return tagMap[type] || 'info'
}

// 加载数据
const loadDataSources = () => {
  // 实际应从 API 加载
  console.log('Loading data sources...', searchForm)
}

// 显示新增对话框
const showCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增数据源'
  Object.assign(formData, { id: null, name: '', source_type: '', configStr: '', is_active: true })
  dialogVisible.value = true
}

// 编辑数据源
const editDataSource = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑数据源'
  Object.assign(formData, { ...row, configStr: JSON.stringify(row.config || {}, null, 2) })
  dialogVisible.value = true
}

// 测试连接
const testConnection = async (row: any) => {
  try {
    // 实际应调用 API
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(`[${row.name}] 连接测试成功!`)
  } catch (error) {
    ElMessage.error(`[${row.name}] 连接测试失败!`)
  }
}

// 提交表单
const submitForm = () => {
  if (!formData.name || !formData.source_type) {
    ElMessage.warning('请填写必填项')
    return
  }

  // 实际应调用 API
  ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
  dialogVisible.value = false
  loadDataSources()
}

// 删除数据源
const deleteDataSource = (row: any) => {
  ElMessageBox.confirm(`确定要删除数据源 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    // 实际应调用 API
    ElMessage.success('删除成功')
    loadDataSources()
  }).catch(() => {})
}
</script>

<style scoped>
.page-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
