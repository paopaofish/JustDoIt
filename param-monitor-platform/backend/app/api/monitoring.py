from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List
from pydantic import BaseModel, Field
from datetime import datetime

from app.core.database import get_db
from app.core.models.database import MonitoringRule, AlertEvent, Parameter


router = APIRouter()


# Pydantic 模型
class MonitoringRuleCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    parameter_id: int
    rule_type: str = Field(..., description="规则类型：threshold, statistical, prediction, custom")
    condition: dict = Field(..., description="规则条件配置")
    severity: str = Field(default="warning", description="严重程度：info, warning, error, critical")
    notification_enabled: bool = True
    notification_channels: list | None = None


class MonitoringRuleUpdate(BaseModel):
    name: str | None = None
    condition: dict | None = None
    severity: str | None = None
    notification_enabled: bool | None = None
    notification_channels: list | None = None
    is_active: bool | None = None


class MonitoringRuleResponse(BaseModel):
    id: int
    name: str
    parameter_id: int
    rule_type: str
    condition: dict
    severity: str
    notification_enabled: bool
    notification_channels: list | None
    is_active: bool
    
    class Config:
        from_attributes = True


class AlertEventResponse(BaseModel):
    id: int
    rule_id: int
    parameter_id: int
    alert_value: float | None
    threshold_value: float | None
    message: str
    severity: str
    status: str
    acknowledged_by: str | None
    acknowledged_at: datetime | None
    resolved_at: datetime | None
    created_at: datetime
    
    class Config:
        from_attributes = True


@router.post("/rules", response_model=MonitoringRuleResponse, status_code=status.HTTP_201_CREATED)
async def create_monitoring_rule(
    rule: MonitoringRuleCreate,
    db: AsyncSession = Depends(get_db),
):
    """创建监控规则"""
    # 验证参数是否存在
    result = await db.execute(
        select(Parameter).where(Parameter.id == rule.parameter_id)
    )
    if not result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Parameter not found",
        )
    
    db_rule = MonitoringRule(**rule.model_dump())
    
    db.add(db_rule)
    await db.commit()
    await db.refresh(db_rule)
    
    return db_rule


@router.get("/rules", response_model=List[MonitoringRuleResponse])
async def list_monitoring_rules(
    skip: int = 0,
    limit: int = 100,
    parameter_id: int | None = None,
    rule_type: str | None = None,
    is_active: bool | None = None,
    db: AsyncSession = Depends(get_db),
):
    """获取监控规则列表"""
    query = select(MonitoringRule)
    
    if parameter_id:
        query = query.where(MonitoringRule.parameter_id == parameter_id)
    if rule_type:
        query = query.where(MonitoringRule.rule_type == rule_type)
    if is_active is not None:
        query = query.where(MonitoringRule.is_active == is_active)
    
    query = query.offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()


@router.put("/rules/{rule_id}", response_model=MonitoringRuleResponse)
async def update_monitoring_rule(
    rule_id: int,
    rule_update: MonitoringRuleUpdate,
    db: AsyncSession = Depends(get_db),
):
    """更新监控规则"""
    result = await db.execute(
        select(MonitoringRule).where(MonitoringRule.id == rule_id)
    )
    rule = result.scalar_one_or_none()
    
    if not rule:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Rule not found",
        )
    
    update_data = rule_update.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(rule, field, value)
    
    await db.commit()
    await db.refresh(rule)
    
    return rule


@router.delete("/rules/{rule_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_monitoring_rule(
    rule_id: int,
    db: AsyncSession = Depends(get_db),
):
    """删除监控规则"""
    result = await db.execute(
        select(MonitoringRule).where(MonitoringRule.id == rule_id)
    )
    rule = result.scalar_one_or_none()
    
    if not rule:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Rule not found",
        )
    
    await db.delete(rule)
    await db.commit()
    
    return None


@router.get("/alerts", response_model=List[AlertEventResponse])
async def list_alert_events(
    skip: int = 0,
    limit: int = 100,
    parameter_id: int | None = None,
    status: str | None = None,
    severity: str | None = None,
    db: AsyncSession = Depends(get_db),
):
    """获取告警事件列表"""
    query = select(AlertEvent)
    
    if parameter_id:
        query = query.where(AlertEvent.parameter_id == parameter_id)
    if status:
        query = query.where(AlertEvent.status == status)
    if severity:
        query = query.where(AlertEvent.severity == severity)
    
    query = query.order_by(AlertEvent.created_at.desc()).offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()


@router.post("/alerts/{alert_id}/acknowledge")
async def acknowledge_alert(
    alert_id: int,
    acknowledged_by: str,
    db: AsyncSession = Depends(get_db),
):
    """确认告警"""
    result = await db.execute(
        select(AlertEvent).where(AlertEvent.id == alert_id)
    )
    alert = result.scalar_one_or_none()
    
    if not alert:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Alert not found",
        )
    
    alert.status = "acknowledged"
    alert.acknowledged_by = acknowledged_by
    alert.acknowledged_at = datetime.utcnow()
    
    await db.commit()
    await db.refresh(alert)
    
    return {"message": "Alert acknowledged", "alert_id": alert_id}


@router.post("/alerts/{alert_id}/resolve")
async def resolve_alert(
    alert_id: int,
    db: AsyncSession = Depends(get_db),
):
    """解决告警"""
    result = await db.execute(
        select(AlertEvent).where(AlertEvent.id == alert_id)
    )
    alert = result.scalar_one_or_none()
    
    if not alert:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Alert not found",
        )
    
    alert.status = "resolved"
    alert.resolved_at = datetime.utcnow()
    
    await db.commit()
    await db.refresh(alert)
    
    return {"message": "Alert resolved", "alert_id": alert_id}


@router.get("/parameters/{parameter_id}/predict")
async def predict_parameter(
    parameter_id: int,
    horizon: int = 24,
    algorithm: str = "linear",
    db: AsyncSession = Depends(get_db),
):
    """预测参数趋势"""
    from app.core.models.database import DataSource
    from app.services.algorithm import LinearRegressionPredictor, LSTM_predictor
    
    # 获取参数
    result = await db.execute(
        select(Parameter).where(Parameter.id == parameter_id)
    )
    parameter = result.scalar_one_or_none()
    
    if not parameter:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Parameter not found",
        )
    
    # 获取数据源
    ds_result = await db.execute(
        select(DataSource).where(DataSource.id == parameter.data_source_id)
    )
    data_source = ds_result.scalar_one_or_none()
    
    if not data_source:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Data source not found",
        )
    
    # 读取历史数据
    from app.services.ingestion import IngestionFactory
    from datetime import timedelta
    
    try:
        adapter = IngestionFactory.create(data_source.source_type, data_source.config)
        await adapter.connect()
        
        end_time = datetime.utcnow()
        start_time = end_time - timedelta(days=30)  # 读取 30 天历史数据
        
        historical_data = await adapter.read_range(parameter.data_path, start_time, end_time)
        await adapter.disconnect()
        
        if len(historical_data) < 10:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Insufficient historical data for prediction",
            )
        
        # 选择算法
        if algorithm == "lstm":
            predictor = LSTM_predictor()
        else:
            predictor = LinearRegressionPredictor()
        
        # 训练模型
        if not predictor.train(historical_data):
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to train prediction model",
            )
        
        # 进行预测
        predictions = predictor.predict(horizon)
        
        return {
            "parameter": parameter.code,
            "algorithm": algorithm,
            "horizon": horizon,
            "predictions": predictions,
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prediction failed: {str(e)}",
        )
