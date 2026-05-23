from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List
from pydantic import BaseModel, Field

from app.core.database import get_db
from app.core.models.database import Parameter


router = APIRouter()


# Pydantic 模型
class ParameterCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    code: str = Field(..., min_length=1, max_length=100)
    description: str | None = None
    unit: str | None = None
    data_source_id: int
    data_path: str = Field(..., max_length=200)
    data_type: str = Field(default="float", description="数据类型：float, int, string, boolean")
    sampling_rate: float | None = None


class ParameterUpdate(BaseModel):
    name: str | None = None
    description: str | None = None
    unit: str | None = None
    data_path: str | None = None
    data_type: str | None = None
    sampling_rate: float | None = None
    is_active: bool | None = None


class ParameterResponse(BaseModel):
    id: int
    name: str
    code: str
    description: str | None
    unit: str | None
    data_source_id: int
    data_path: str
    data_type: str
    sampling_rate: float | None
    is_active: bool
    
    class Config:
        from_attributes = True


@router.post("/", response_model=ParameterResponse, status_code=status.HTTP_201_CREATED)
async def create_parameter(
    parameter: ParameterCreate,
    db: AsyncSession = Depends(get_db),
):
    """创建新的监测参数"""
    # 检查编码是否已存在
    result = await db.execute(
        select(Parameter).where(Parameter.code == parameter.code)
    )
    if result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Parameter with this code already exists",
        )
    
    # 验证数据源是否存在
    from app.core.models.database import DataSource
    ds_result = await db.execute(
        select(DataSource).where(DataSource.id == parameter.data_source_id)
    )
    if not ds_result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Data source not found",
        )
    
    db_parameter = Parameter(**parameter.model_dump())
    
    db.add(db_parameter)
    await db.commit()
    await db.refresh(db_parameter)
    
    return db_parameter


@router.get("/", response_model=List[ParameterResponse])
async def list_parameters(
    skip: int = 0,
    limit: int = 100,
    data_source_id: int | None = None,
    is_active: bool | None = None,
    db: AsyncSession = Depends(get_db),
):
    """获取参数列表"""
    query = select(Parameter)
    
    if data_source_id:
        query = query.where(Parameter.data_source_id == data_source_id)
    if is_active is not None:
        query = query.where(Parameter.is_active == is_active)
    
    query = query.offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()


@router.get("/{parameter_id}", response_model=ParameterResponse)
async def get_parameter(
    parameter_id: int,
    db: AsyncSession = Depends(get_db),
):
    """获取单个参数详情"""
    result = await db.execute(
        select(Parameter).where(Parameter.id == parameter_id)
    )
    parameter = result.scalar_one_or_none()
    
    if not parameter:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Parameter not found",
        )
    
    return parameter


@router.put("/{parameter_id}", response_model=ParameterResponse)
async def update_parameter(
    parameter_id: int,
    parameter_update: ParameterUpdate,
    db: AsyncSession = Depends(get_db),
):
    """更新参数"""
    result = await db.execute(
        select(Parameter).where(Parameter.id == parameter_id)
    )
    parameter = result.scalar_one_or_none()
    
    if not parameter:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Parameter not found",
        )
    
    update_data = parameter_update.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(parameter, field, value)
    
    await db.commit()
    await db.refresh(parameter)
    
    return parameter


@router.delete("/{parameter_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_parameter(
    parameter_id: int,
    db: AsyncSession = Depends(get_db),
):
    """删除参数"""
    result = await db.execute(
        select(Parameter).where(Parameter.id == parameter_id)
    )
    parameter = result.scalar_one_or_none()
    
    if not parameter:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Parameter not found",
        )
    
    await db.delete(parameter)
    await db.commit()
    
    return None


@router.get("/{parameter_id}/data")
async def get_parameter_data(
    parameter_id: int,
    start_time: str,
    end_time: str,
    db: AsyncSession = Depends(get_db),
):
    """获取参数历史数据"""
    from datetime import datetime
    from app.core.models.database import DataSource
    
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
    data_source = result.scalar_one_or_none()
    
    if not data_source:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Data source not found",
        )
    
    # 使用数据接入服务读取数据
    from app.services.ingestion import IngestionFactory
    from datetime import datetime
    
    try:
        adapter = IngestionFactory.create(data_source.source_type, data_source.config)
        await adapter.connect()
        
        start_dt = datetime.fromisoformat(start_time)
        end_dt = datetime.fromisoformat(end_time)
        
        data = await adapter.read_range(parameter.data_path, start_dt, end_dt)
        await adapter.disconnect()
        
        return {"parameter": parameter.code, "data": data}
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to read data: {str(e)}",
        )
