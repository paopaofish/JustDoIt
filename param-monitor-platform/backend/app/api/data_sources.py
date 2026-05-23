from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List
from pydantic import BaseModel, Field

from app.core.database import get_db
from app.core.models.database import DataSource


router = APIRouter()


# Pydantic 模型
class DataSourceCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    source_type: str = Field(..., description="数据源类型：iotdb, mysql, opcua")
    config: dict = Field(..., description="连接配置")


class DataSourceUpdate(BaseModel):
    name: str | None = None
    config: dict | None = None
    is_active: bool | None = None


class DataSourceResponse(BaseModel):
    id: int
    name: str
    source_type: str
    config: dict
    is_active: bool
    
    class Config:
        from_attributes = True


@router.post("/", response_model=DataSourceResponse, status_code=status.HTTP_201_CREATED)
async def create_data_source(
    data_source: DataSourceCreate,
    db: AsyncSession = Depends(get_db),
):
    """创建新的数据源"""
    # 检查名称是否已存在
    result = await db.execute(
        select(DataSource).where(DataSource.name == data_source.name)
    )
    if result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Data source with this name already exists",
        )
    
    db_data_source = DataSource(
        name=data_source.name,
        source_type=data_source.source_type,
        config=data_source.config,
    )
    
    db.add(db_data_source)
    await db.commit()
    await db.refresh(db_data_source)
    
    return db_data_source


@router.get("/", response_model=List[DataSourceResponse])
async def list_data_sources(
    skip: int = 0,
    limit: int = 100,
    source_type: str | None = None,
    is_active: bool | None = None,
    db: AsyncSession = Depends(get_db),
):
    """获取数据源列表"""
    query = select(DataSource)
    
    if source_type:
        query = query.where(DataSource.source_type == source_type)
    if is_active is not None:
        query = query.where(DataSource.is_active == is_active)
    
    query = query.offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()


@router.get("/{data_source_id}", response_model=DataSourceResponse)
async def get_data_source(
    data_source_id: int,
    db: AsyncSession = Depends(get_db),
):
    """获取单个数据源详情"""
    result = await db.execute(
        select(DataSource).where(DataSource.id == data_source_id)
    )
    data_source = result.scalar_one_or_none()
    
    if not data_source:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Data source not found",
        )
    
    return data_source


@router.put("/{data_source_id}", response_model=DataSourceResponse)
async def update_data_source(
    data_source_id: int,
    data_source_update: DataSourceUpdate,
    db: AsyncSession = Depends(get_db),
):
    """更新数据源"""
    result = await db.execute(
        select(DataSource).where(DataSource.id == data_source_id)
    )
    data_source = result.scalar_one_or_none()
    
    if not data_source:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Data source not found",
        )
    
    update_data = data_source_update.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(data_source, field, value)
    
    await db.commit()
    await db.refresh(data_source)
    
    return data_source


@router.delete("/{data_source_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_data_source(
    data_source_id: int,
    db: AsyncSession = Depends(get_db),
):
    """删除数据源"""
    result = await db.execute(
        select(DataSource).where(DataSource.id == data_source_id)
    )
    data_source = result.scalar_one_or_none()
    
    if not data_source:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Data source not found",
        )
    
    await db.delete(data_source)
    await db.commit()
    
    return None


@router.post("/{data_source_id}/test")
async def test_data_source_connection(
    data_source_id: int,
    db: AsyncSession = Depends(get_db),
):
    """测试数据源连接"""
    result = await db.execute(
        select(DataSource).where(DataSource.id == data_source_id)
    )
    data_source = result.scalar_one_or_none()
    
    if not data_source:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Data source not found",
        )
    
    # 使用数据接入工厂测试连接
    from app.services.ingestion import IngestionFactory
    
    try:
        adapter = IngestionFactory.create(data_source.source_type, data_source.config)
        is_connected = await adapter.test_connection()
        
        return {
            "success": is_connected,
            "message": "Connection successful" if is_connected else "Connection failed",
        }
    except Exception as e:
        return {
            "success": False,
            "message": f"Connection error: {str(e)}",
        }
