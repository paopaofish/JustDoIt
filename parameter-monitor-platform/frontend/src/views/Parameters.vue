<template>
  <div class="parameters-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>监测参数管理</span>
          <el-button type="primary" @click="showAddDialog">新增参数</el-button>
        </div>
      </template>

      <el-table :data="parameters" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="code" label="编码" />
        <el-table-column prop="unit" label="单位" width="100" />
        <el-table-column prop="dataType" label="数据类型" width="120" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editParameter(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteParameter(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑参数' : '新增参数'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="编码" required>
          <el-input v-model="form.code" placeholder="请输入参数编码" />
        </el-form-item>
        <el-form-item label="数据路径" required>
          <el-input v-model="form.path" placeholder="IoTDB测点路径或OPC UA节点ID" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如：°C, MPa" />
        </el-form-item>
        <el-form-item label="数据类型" required>
          <el-select v-model="form.dataType" placeholder="请选择">
            <el-option label="DOUBLE" value="DOUBLE" />
            <el-option label="FLOAT" value="FLOAT" />
            <el-option label="INTEGER" value="INTEGER" />
            <el-option label="LONG" value="LONG" />
            <el-option label="BOOLEAN" value="BOOLEAN" />
            <el-option label="STRING" value="STRING" />
          </el-select>
        </el-form-item>
        <el-form-item label="最小值">
          <el-input-number v-model="form.minValue" :precision="2" />
        </el-form-item>
        <el-form-item label="最大值">
          <el-input-number v-model="form.maxValue" :precision="2" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveParameter">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = '/api/monitoring'

const parameters = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

const loadParameters = async () => {
  try {
    const res = await axios.get(`${API_BASE}/parameters`)
    parameters.value = res.data
  } catch (error) {
    ElMessage.error('加载参数列表失败')
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    name: '',
    code: '',
    path: '',
    unit: '',
    dataType: 'DOUBLE',
    minValue: null,
    maxValue: null,
    description: '',
    enabled: true
  }
  dialogVisible.value = true
}

const editParameter = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const saveParameter = async () => {
  try {
    if (isEdit.value) {
      await axios.put(`${API_BASE}/parameters/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await axios.post(`${API_BASE}/parameters`, form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadParameters()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const deleteParameter = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该参数吗？', '提示', { type: 'warning' })
    await axios.delete(`${API_BASE}/parameters/${id}`)
    ElMessage.success('删除成功')
    loadParameters()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadParameters()
})
</script>

<style scoped>
.parameters-view {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
