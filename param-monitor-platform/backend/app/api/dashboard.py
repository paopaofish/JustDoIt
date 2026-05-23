from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List
from pydantic import BaseModel, Field

from app.core.database import get_db
from app.core.models.database import Dashboard


router = APIRouter()


# Pydantic 模型
class DashboardCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    description: str | None = None
    layout_config: dict | None = None
    widgets_config: list | None = None
    is_public: bool = False


class DashboardUpdate(BaseModel):
    name: str | None = None
    description: str | None = None
    layout_config: dict | None = None
    widgets_config: list | None = None
    is_public: bool | None = None


class DashboardResponse(BaseModel):
    id: int
    name: str
    description: str | None
    layout_config: dict | None
    widgets_config: list | None
    is_public: bool
    created_by: str | None
    
    class Config:
        from_attributes = True


@router.post("/", response_model=DashboardResponse, status_code=status.HTTP_201_CREATED)
async def create_dashboard(
    dashboard: DashboardCreate,
    db: AsyncSession = Depends(get_db),
):
    """创建驾驶舱仪表盘"""
    db_dashboard = Dashboard(**dashboard.model_dump())
    
    db.add(db_dashboard)
    await db.commit()
    await db.refresh(db_dashboard)
    
    return db_dashboard


@router.get("/", response_model=List[DashboardResponse])
async def list_dashboards(
    skip: int = 0,
    limit: int = 100,
    is_public: bool | None = None,
    db: AsyncSession = Depends(get_db),
):
    """获取驾驶舱列表"""
    query = select(Dashboard)
    
    if is_public is not None:
        query = query.where(Dashboard.is_public == is_public)
    
    query = query.offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()


@router.get("/{dashboard_id}", response_model=DashboardResponse)
async def get_dashboard(
    dashboard_id: int,
    db: AsyncSession = Depends(get_db),
):
    """获取单个驾驶舱详情"""
    result = await db.execute(
        select(Dashboard).where(Dashboard.id == dashboard_id)
    )
    dashboard = result.scalar_one_or_none()
    
    if not dashboard:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Dashboard not found",
        )
    
    return dashboard


@router.put("/{dashboard_id}", response_model=DashboardResponse)
async def update_dashboard(
    dashboard_id: int,
    dashboard_update: DashboardUpdate,
    db: AsyncSession = Depends(get_db),
):
    """更新驾驶舱"""
    result = await db.execute(
        select(Dashboard).where(Dashboard.id == dashboard_id)
    )
    dashboard = result.scalar_one_or_none()
    
    if not dashboard:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Dashboard not found",
        )
    
    update_data = dashboard_update.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(dashboard, field, value)
    
    await db.commit()
    await db.refresh(dashboard)
    
    return dashboard


@router.delete("/{dashboard_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_dashboard(
    dashboard_id: int,
    db: AsyncSession = Depends(get_db),
):
    """删除驾驶舱"""
    result = await db.execute(
        select(Dashboard).where(Dashboard.id == dashboard_id)
    )
    dashboard = result.scalar_one_or_none()
    
    if not dashboard:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Dashboard not found",
        )
    
    await db.delete(dashboard)
    await db.commit()
    
    return None


@router.get("/{dashboard_id}/data")
async def get_dashboard_data(dashboard_id: int, db: AsyncSession = Depends(get_db)):
    """获取驾驶舱实时数据"""
    result = await db.execute(
        select(Dashboard).where(Dashboard.id == dashboard_id)
    )
    dashboard = result.scalar_one_or_none()
    
    if not dashboard:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Dashboard not found",
        )
    
    # 根据 widgets_config 获取各个组件的数据
    # 这里简化实现，实际应该根据 widget 配置查询对应的参数数据
    widgets_data = []
    
    if dashboard.widgets_config:
        for widget in dashboard.widgets_config:
            widget_data = {
                "widget_id": widget.get("id"),
                "type": widget.get("type"),
                "title": widget.get("title"),
                "data": None,  # 实际应查询对应参数的最新数据
            }
            widgets_data.append(widget_data)
    
    return {
        "dashboard_id": dashboard.id,
        "name": dashboard.name,
        "widgets": widgets_data,
    }
