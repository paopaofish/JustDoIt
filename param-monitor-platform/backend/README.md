# 参数监测平台后端

## 目录结构

```
backend/
├── app/
│   ├── api/              # API 路由
│   │   ├── data_sources.py
│   │   ├── parameters.py
│   │   ├── monitoring.py
│   │   └── dashboard.py
│   ├── core/             # 核心配置
│   │   ├── config.py
│   │   ├── database.py
│   │   └── models/
│   └── services/         # 业务服务
│       ├── ingestion/    # 数据接入
│       ├── monitoring/   # 监控服务
│       └── algorithm/    # 算法服务
├── requirements.txt
└── Dockerfile
```

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 配置环境变量

创建 `.env` 文件：

```env
# 数据库配置
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=param_monitor
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379

# IoTDB 配置
IOTDB_HOST=localhost
IOTDB_PORT=6667
IOTDB_USERNAME=root
IOTDB_PASSWORD=root
```

### 3. 启动服务

```bash
# 开发模式
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# 生产模式
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

### 4. 访问 API 文档

打开浏览器访问：http://localhost:8000/docs

## API 接口

### 数据源管理
- `POST /api/v1/data-sources/` - 创建数据源
- `GET /api/v1/data-sources/` - 获取数据源列表
- `GET /api/v1/data-sources/{id}` - 获取数据源详情
- `PUT /api/v1/data-sources/{id}` - 更新数据源
- `DELETE /api/v1/data-sources/{id}` - 删除数据源
- `POST /api/v1/data-sources/{id}/test` - 测试连接

### 参数管理
- `POST /api/v1/parameters/` - 创建参数
- `GET /api/v1/parameters/` - 获取参数列表
- `GET /api/v1/parameters/{id}` - 获取参数详情
- `PUT /api/v1/parameters/{id}` - 更新参数
- `DELETE /api/v1/parameters/{id}` - 删除参数
- `GET /api/v1/parameters/{id}/data` - 获取参数历史数据

### 监控管理
- `POST /api/v1/monitoring/rules` - 创建监控规则
- `GET /api/v1/monitoring/rules` - 获取监控规则列表
- `PUT /api/v1/monitoring/rules/{id}` - 更新监控规则
- `DELETE /api/v1/monitoring/rules/{id}` - 删除监控规则
- `GET /api/v1/monitoring/alerts` - 获取告警事件列表
- `POST /api/v1/monitoring/alerts/{id}/acknowledge` - 确认告警
- `POST /api/v1/monitoring/alerts/{id}/resolve` - 解决告警
- `GET /api/v1/monitoring/parameters/{id}/predict` - 预测参数趋势

### 驾驶舱管理
- `POST /api/v1/dashboard/` - 创建驾驶舱
- `GET /api/v1/dashboard/` - 获取驾驶舱列表
- `GET /api/v1/dashboard/{id}` - 获取驾驶舱详情
- `PUT /api/v1/dashboard/{id}` - 更新驾驶舱
- `DELETE /api/v1/dashboard/{id}` - 删除驾驶舱
- `GET /api/v1/dashboard/{id}/data` - 获取驾驶舱实时数据

## 数据源适配器

### 支持的数据库类型
- **IoTDB**: 时序数据库
- **OPC UA**: 工业协议 (传感器/PLC)
- **MySQL**: 关系数据库 (通过 SQLAlchemy)

### 添加新的数据源适配器

1. 在 `app/services/ingestion/` 创建适配器类
2. 继承 `BaseIngestion` 基类
3. 实现所有抽象方法
4. 在 `__init__.py` 中注册到工厂

```python
from .base import BaseIngestion, IngestionFactory

class MyAdapter(BaseIngestion):
    # 实现适配器逻辑
    pass

IngestionFactory.register("myadapter", MyAdapter)
```

## 预测算法

### 支持的算法
- **线性回归**: 简单趋势预测
- **LSTM**: 深度学习时间序列预测

### 添加新的预测算法

1. 在 `app/services/algorithm/` 创建算法类
2. 继承 `BasePredictionAlgorithm` 基类
3. 实现训练、预测、评估等方法
