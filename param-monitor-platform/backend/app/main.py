from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from loguru import logger
import sys

from app.core.config import settings
from app.core.models.database import Base
from app.api import data_sources, parameters, monitoring, dashboard


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动时执行
    logger.info(f"Starting {settings.APP_NAME} v{settings.APP_VERSION}")
    
    # 初始化数据库
    from app.core.database import engine
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    logger.info("Database initialized")
    
    yield
    
    # 关闭时执行
    logger.info("Shutting down application")


# 创建 FastAPI 应用
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="参数监测平台 API - 支持多源数据接入、实时监控、异常检测、趋势预测和驾驶舱展示",
    lifespan=lifespan,
)

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应限制具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(data_sources.router, prefix="/api/v1/data-sources", tags=["数据源管理"])
app.include_router(parameters.router, prefix="/api/v1/parameters", tags=["参数管理"])
app.include_router(monitoring.router, prefix="/api/v1/monitoring", tags=["监控管理"])
app.include_router(dashboard.router, prefix="/api/v1/dashboard", tags=["驾驶舱管理"])


@app.get("/")
async def root():
    """根路径"""
    return {
        "name": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "description": "参数监测平台",
        "docs": "/docs",
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.API_HOST,
        port=settings.API_PORT,
        reload=settings.DEBUG,
    )
