from typing import Any, Dict, List, Optional, AsyncGenerator
from datetime import datetime
from .base import BaseIngestion
from loguru import logger

try:
    from iotdb.Session import Session
    from iotdb.tsfile_utils import TsFileUtils
    IOTDB_AVAILABLE = True
except ImportError:
    IOTDB_AVAILABLE = False
    logger.warning("IoTDB client not installed. Install with: pip install apache-iotdb")


class IoTDBIngestion(BaseIngestion):
    """IoTDB 时序数据库接入服务"""
    
    def __init__(self, config: Dict[str, Any]):
        super().__init__(config)
        self.host = config.get("host", "localhost")
        self.port = config.get("port", 6667)
        self.username = config.get("username", "root")
        self.password = config.get("password", "root")
        self.session = None
    
    async def connect(self) -> bool:
        """连接到 IoTDB"""
        if not IOTDB_AVAILABLE:
            logger.error("IoTDB client not available")
            return False
        
        try:
            self.session = Session(self.host, self.port, self.username, self.password)
            self.session.open(False)
            self.is_connected = True
            logger.info(f"Connected to IoTDB at {self.host}:{self.port}")
            return True
        except Exception as e:
            logger.error(f"Failed to connect to IoTDB: {e}")
            return False
    
    async def disconnect(self):
        """断开 IoTDB 连接"""
        if self.session and self.is_connected:
            self.session.close()
            self.is_connected = False
            logger.info("Disconnected from IoTDB")
    
    async def read_point(self, parameter_path: str, timestamp: Optional[datetime] = None) -> Optional[float]:
        """读取单个数据点"""
        if not self.is_connected:
            await self.connect()
        
        try:
            if timestamp:
                sql = f"SELECT {parameter_path} FROM root.** WHERE time = {int(timestamp.timestamp() * 1000)}"
            else:
                sql = f"SELECT LAST {parameter_path} FROM root.**"
            
            session_result = self.session.execute_query_statement(sql)
            if session_result.has_next():
                record = session_result.next()
                return record.get_float(0)
            return None
        except Exception as e:
            logger.error(f"Error reading point from IoTDB: {e}")
            return None
    
    async def read_range(self, parameter_path: str, start_time: datetime, end_time: datetime) -> List[Dict]:
        """读取时间范围数据"""
        if not self.is_connected:
            await self.connect()
        
        try:
            start_ms = int(start_time.timestamp() * 1000)
            end_ms = int(end_time.timestamp() * 1000)
            sql = f"SELECT {parameter_path} FROM root.** WHERE time >= {start_ms} AND time <= {end_ms}"
            
            results = []
            session_result = self.session.execute_query_statement(sql)
            while session_result.has_next():
                record = session_result.next()
                results.append({
                    "timestamp": record.get_timestamp(0),
                    "value": record.get_float(1),
                    "parameter": parameter_path
                })
            return results
        except Exception as e:
            logger.error(f"Error reading range from IoTDB: {e}")
            return []
    
    async def subscribe(self, parameter_paths: List[str]) -> AsyncGenerator[Dict, None]:
        """订阅实时数据流 (简化实现，实际应使用 IoTDB 订阅功能)"""
        if not self.is_connected:
            await self.connect()
        
        # 简化实现：轮询最新数据
        import asyncio
        while True:
            for path in parameter_paths:
                try:
                    value = await self.read_point(path)
                    if value is not None:
                        yield {
                            "parameter": path,
                            "value": value,
                            "timestamp": datetime.utcnow().isoformat()
                        }
                except Exception as e:
                    logger.error(f"Error subscribing to {path}: {e}")
            await asyncio.sleep(1)  # 1 秒轮询间隔
    
    async def test_connection(self) -> bool:
        """测试 IoTDB 连接"""
        try:
            return await self.connect()
        finally:
            await self.disconnect()
