# 参数监测平台 (Param Monitor Platform)

## 项目概述
一个支持多源数据接入、实时监控、异常检测、趋势预测和驾驶舱展示的工业参数监测平台。

## 核心功能模块

### 1. 参数接入服务 (Data Ingestion)
- **实时数据库接入**
  - IoTDB 时序数据库
  - OSI PI 实时数据库
  - 其他时序数据库适配器
- **关系数据库接入**
  - MySQL/PostgreSQL/Oracle
- **工业协议接入**
  - OPC UA 传感器/PLC数据采集
  - MQTT 消息队列
  - Modbus TCP/RTU

### 2. 参数监控服务 (Monitoring)
- **异常规则配置**
  - 阈值告警（上下限、变化率）
  - 统计规则（3σ原则、箱线图）
  - 自定义脚本规则
- **趋势预测算法**
  - 线性回归预测
  - 时间序列模型（ARIMA、Prophet）
  - 机器学习模型（LSTM、XGBoost）
- **实时监控面板**
  - 实时数据流展示
  - 历史趋势分析
  - 告警事件管理

### 3. 监督驾驶舱 (Dashboard)
- **综合监控视图**
  - 关键参数KPI卡片
  - 实时数据图表
  - 告警统计与分布
- **多维度分析**
  - 按区域/设备/参数类型分组
  - 对比分析与关联分析
- **可配置布局**
  - 拖拽式组件布局
  - 自定义仪表盘模板

## 技术架构

### 后端技术栈
- **框架**: FastAPI (Python)
- **数据库**: 
  - PostgreSQL (元数据存储)
  - IoTDB/InfluxDB (时序数据)
  - Redis (缓存与消息队列)
- **数据处理**: 
  - Pandas/Numpy (数据分析)
  - Scikit-learn/PyTorch (机器学习)
- **OPC UA**: opcua-asyncio
- **任务调度**: Celery + Redis

### 前端技术栈
- **框架**: Vue 3 + TypeScript
- **UI库**: Element Plus
- **图表**: ECharts
- **状态管理**: Pinia
- **构建工具**: Vite

## 项目结构

```
param-monitor-platform/
├── backend/                    # 后端服务
│   ├── app/
│   │   ├── api/               # API路由
│   │   ├── core/              # 核心配置
│   │   ├── models/            # 数据模型
│   │   └── services/          # 业务服务
│   │       ├── ingestion/     # 数据接入服务
│   │       ├── monitoring/    # 监控服务
│   │       └── algorithm/     # 算法服务
│   ├── requirements.txt
│   └── Dockerfile
├── frontend/                   # 前端应用
│   ├── src/
│   │   ├── views/             # 页面视图
│   │   ├── components/        # 组件
│   │   └── assets/            # 静态资源
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml          # Docker编排
└── README.md
```

## 快速开始

### 环境要求
- Python 3.9+
- Node.js 18+
- Docker & Docker Compose

### 安装步骤

#### 1. 克隆项目
```bash
git clone <repository-url>
cd param-monitor-platform
```

#### 2. 启动服务
```bash
docker-compose up -d
```

#### 3. 访问应用
- 前端界面: http://localhost:8080
- 后端API: http://localhost:8000
- API文档: http://localhost:8000/docs

## 配置示例

### 数据源配置
```yaml
data_sources:
  iotdb:
    host: localhost
    port: 6667
    username: root
    password: root
  mysql:
    host: localhost
    port: 3306
    database: production_db
  opcua:
    endpoint: opc.tcp://plc-host:4840
    security_mode: SignAndEncrypt
```

### 监控规则配置
```yaml
rules:
  - name: temperature_high
    parameter: temp_sensor_01
    type: threshold
    condition: value > 80
    duration: 5m
  - name: pressure_trend
    parameter: pressure_01
    type: prediction
    algorithm: lstm
    horizon: 1h
```

## 开发指南

### 添加新的数据源适配器
1. 在 `backend/app/services/ingestion/` 创建适配器类
2. 实现 `BaseIngestion` 接口
3. 注册到数据源工厂

### 添加新的预测算法
1. 在 `backend/app/services/algorithm/` 实现算法类
2. 继承 `BasePredictionAlgorithm`
3. 在监控服务中注册使用

## License
MIT License
