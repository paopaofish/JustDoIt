from sqlalchemy import create_engine, Column, Integer, String, Float, DateTime, Boolean, JSON, ForeignKey, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from datetime import datetime
from typing import Optional

Base = declarative_base()


class DataSource(Base):
    """数据源配置表"""
    __tablename__ = "data_sources"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), unique=True, nullable=False)
    source_type = Column(String(50), nullable=False)  # iotdb, mysql, opcua, etc.
    config = Column(JSON, nullable=False)  # 连接配置
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联参数
    parameters = relationship("Parameter", back_populates="data_source")


class Parameter(Base):
    """监测参数表"""
    __tablename__ = "parameters"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    code = Column(String(100), unique=True, nullable=False)  # 参数编码
    description = Column(Text)
    unit = Column(String(50))  # 单位
    data_source_id = Column(Integer, ForeignKey("data_sources.id"))
    data_path = Column(String(200))  # 在数据源中的路径/表名/字段名
    data_type = Column(String(20))  # float, int, string, boolean
    sampling_rate = Column(Float)  # 采样率 (秒)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联
    data_source = relationship("DataSource", back_populates="parameters")
    rules = relationship("MonitoringRule", back_populates="parameter")
    alerts = relationship("AlertEvent", back_populates="parameter")


class MonitoringRule(Base):
    """监控规则表"""
    __tablename__ = "monitoring_rules"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    parameter_id = Column(Integer, ForeignKey("parameters.id"))
    rule_type = Column(String(50), nullable=False)  # threshold, statistical, prediction, custom
    condition = Column(JSON, nullable=False)  # 规则条件配置
    severity = Column(String(20), default="warning")  # info, warning, error, critical
    notification_enabled = Column(Boolean, default=True)
    notification_channels = Column(JSON)  # ["email", "sms", "webhook"]
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关联
    parameter = relationship("Parameter", back_populates="rules")


class AlertEvent(Base):
    """告警事件表"""
    __tablename__ = "alert_events"
    
    id = Column(Integer, primary_key=True, index=True)
    rule_id = Column(Integer, ForeignKey("monitoring_rules.id"))
    parameter_id = Column(Integer, ForeignKey("parameters.id"))
    alert_value = Column(Float)  # 触发告警的值
    threshold_value = Column(Float)  # 阈值
    message = Column(Text)  # 告警消息
    severity = Column(String(20))
    status = Column(String(20), default="active")  # active, acknowledged, resolved
    acknowledged_by = Column(String(100))
    acknowledged_at = Column(DateTime)
    resolved_at = Column(DateTime)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # 关联
    parameter = relationship("Parameter", back_populates="alerts")


class Dashboard(Base):
    """驾驶舱仪表盘表"""
    __tablename__ = "dashboards"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    description = Column(Text)
    layout_config = Column(JSON)  # 布局配置
    widgets_config = Column(JSON)  # 组件配置
    is_public = Column(Boolean, default=False)
    created_by = Column(String(100))
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class PredictionModel(Base):
    """预测模型表"""
    __tablename__ = "prediction_models"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    parameter_id = Column(Integer, ForeignKey("parameters.id"))
    algorithm = Column(String(50), nullable=False)  # linear, arima, lstm, prophet, xgboost
    model_config = Column(JSON)  # 模型参数配置
    model_path = Column(String(200))  # 模型文件存储路径
    metrics = Column(JSON)  # 模型评估指标
    training_status = Column(String(20), default="pending")  # pending, training, completed, failed
    last_trained_at = Column(DateTime)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
