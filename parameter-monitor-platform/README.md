# 参数监测平台 (Parameter Monitor Platform)

## 项目简介

基于 Java Spring Boot + Vue 3 的参数监测平台，支持多种工业数据源接入，具备实时监控、异常告警、趋势预测和驾驶舱展示功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: MySQL 8.0 (JPA/Hibernate)
- **时序数据库**: Apache IoTDB
- **工业协议**: OPC UA (Eclipse Milo)
- **算法库**: Apache Commons Math3
- **安全**: Spring Security

### 前端
- **框架**: Vue 3 + Vite
- **UI组件**: Element Plus
- **图表**: ECharts 5
- **状态管理**: Pinia
- **路由**: Vue Router 4

## 核心功能

### 1. 参数接入
- **IoTDB**: 支持Apache IoTDB时序数据库连接和数据读取
- **OSI PI**: 支持OSI PI实时数据库接入
- **MySQL**: 支持关系型数据库接入
- **OPC UA**: 支持PLC、传感器等工业设备通过OPC UA协议接入

### 2. 参数监控
- 实时数据采集与存储
- 历史数据查询与统计
- 数据质量标识

### 3. 异常规则配置
- 阈值告警（高/低）
- 范围检查
- 变化率监测
- 自定义表达式
- 趋势预测异常

### 4. 趋势预测算法
- 线性回归预测
- 置信区间计算
- 趋势变化检测

### 5. 监督驾驶舱
- 概览统计卡片
- 实时趋势图表
- 最新告警列表
- 数据源状态监控

## 项目结构

```
parameter-monitor-platform/
├── backend/                    # 后端Java项目
│   ├── src/main/java/com/monitor/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # REST控制器
│   │   ├── service/           # 业务逻辑层
│   │   ├── repository/        # 数据访问层
│   │   ├── model/             # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   ├── connector/         # 数据源连接器
│   │   │   ├── IoTDBConnector.java
│   │   │   └── OpcUaConnector.java
│   │   └── algorithm/         # 算法模块
│   │       ├── TrendPredictionAlgorithm.java
│   │       └── AnomalyDetectionAlgorithm.java
│   ├── src/main/resources/
│   │   └── application.yml    # 应用配置
│   └── pom.xml                # Maven配置
├── frontend/                   # 前端Vue项目
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   │   ├── Dashboard.vue  # 驾驶舱
│   │   │   ├── Parameters.vue # 参数管理
│   │   │   └── ...
│   │   ├── components/        # 通用组件
│   │   ├── router/            # 路由配置
│   │   └── App.vue            # 根组件
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml          # Docker编排
└── README.md                   # 项目说明
```

## 快速开始

### 前置要求
- JDK 17+
- Node.js 18+
- Maven 3.8+
- Docker & Docker Compose (可选)

### 方式一：Docker部署（推荐）

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

访问地址:
- 前端驾驶舱: http://localhost:8080
- 后端API: http://localhost:8081/api

### 方式二：本地开发

#### 启动后端
```bash
cd backend
mvn spring-boot:run
```

#### 启动前端
```bash
cd frontend
npm install
npm run dev
```

## API接口

### 数据源管理
- `GET /api/monitoring/datasources` - 获取所有数据源
- `POST /api/monitoring/datasources` - 创建数据源
- `PUT /api/monitoring/datasources/{id}` - 更新数据源
- `DELETE /api/monitoring/datasources/{id}` - 删除数据源
- `POST /api/monitoring/datasources/{id}/test` - 测试连接

### 参数管理
- `GET /api/monitoring/parameters` - 获取所有参数
- `POST /api/monitoring/parameters` - 创建参数
- `GET /api/monitoring/data/{parameterId}` - 查询历史数据
- `GET /api/monitoring/data/{parameterId}/latest` - 获取最新数据

### 规则管理
- `GET /api/monitoring/rules/parameter/{parameterId}` - 获取参数规则
- `POST /api/monitoring/rules` - 创建规则
- `DELETE /api/monitoring/rules/{id}` - 删除规则

### 告警管理
- `GET /api/monitoring/alarms` - 获取告警列表
- `POST /api/monitoring/alarms/{id}/handle` - 处理告警

### 驾驶舱
- `GET /api/monitoring/dashboard/overview` - 获取概览数据
- `GET /api/monitoring/prediction/{parameterId}` - 获取趋势预测

## 配置说明

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/monitor_db
    username: root
    password: your_password

iotdb:
  host: localhost
  port: 6667
  username: root
  password: root

opcua:
  endpoint: opc.tcp://localhost:4840
```

## 扩展开发

### 添加新的数据源类型
1. 在 `DataSource.DataSourceType` 枚举中添加新类型
2. 创建新的连接器类（参考 `IoTDBConnector`）
3. 在 `DataSourceService.testConnection()` 中添加测试逻辑

### 添加新的算法
1. 在 `algorithm` 包中创建新算法类
2. 使用 `@Component` 注解注册为Spring Bean
3. 在 `MonitoringService` 中注入并使用

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交Issue。
