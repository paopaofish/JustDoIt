from abc import ABC, abstractmethod
from typing import Any, Dict, List, Optional, AsyncGenerator
from datetime import datetime
import asyncio


class BaseIngestion(ABC):
    """数据接入服务基类"""
    
    def __init__(self, config: Dict[str, Any]):
        self.config = config
        self.is_connected = False
    
    @abstractmethod
    async def connect(self) -> bool:
        """建立连接"""
        pass
    
    @abstractmethod
    async def disconnect(self):
        """断开连接"""
        pass
    
    @abstractmethod
    async def read_point(self, parameter_path: str, timestamp: Optional[datetime] = None) -> Optional[float]:
        """读取单个数据点"""
        pass
    
    @abstractmethod
    async def read_range(self, parameter_path: str, start_time: datetime, end_time: datetime) -> List[Dict]:
        """读取时间范围数据"""
        pass
    
    @abstractmethod
    async def subscribe(self, parameter_paths: List[str]) -> AsyncGenerator[Dict, None]:
        """订阅实时数据流"""
        pass
    
    @abstractmethod
    async def test_connection(self) -> bool:
        """测试连接"""
        pass


class IngestionFactory:
    """数据接入工厂"""
    
    _adapters = {}
    
    @classmethod
    def register(cls, source_type: str, adapter_class):
        """注册数据源适配器"""
        cls._adapters[source_type.lower()] = adapter_class
    
    @classmethod
    def create(cls, source_type: str, config: Dict[str, Any]) -> BaseIngestion:
        """创建数据接入实例"""
        adapter_class = cls._adapters.get(source_type.lower())
        if not adapter_class:
            raise ValueError(f"Unsupported data source type: {source_type}")
        return adapter_class(config)
    
    @classmethod
    def get_supported_types(cls) -> List[str]:
        """获取支持的数据源类型"""
        return list(cls._adapters.keys())
