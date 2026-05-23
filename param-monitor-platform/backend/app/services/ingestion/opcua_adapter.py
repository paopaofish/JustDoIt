from typing import Any, Dict, List, Optional, AsyncGenerator
from datetime import datetime
from .base import BaseIngestion
from loguru import logger

try:
    from asyncua import Client, ua
    OPCUA_AVAILABLE = True
except ImportError:
    OPCUA_AVAILABLE = False
    logger.warning("OPC UA client not installed. Install with: pip install asyncua")


class OPCUAIngestion(BaseIngestion):
    """OPC UA 数据接入服务"""
    
    def __init__(self, config: Dict[str, Any]):
        super().__init__(config)
        self.endpoint = config.get("endpoint", "opc.tcp://localhost:4840")
        self.username = config.get("username")
        self.password = config.get("password")
        self.security_mode = config.get("security_mode", "None")  # None, Sign, SignAndEncrypt
        self.client = None
        self.session = None
    
    async def connect(self) -> bool:
        """连接到 OPC UA 服务器"""
        if not OPCUA_AVAILABLE:
            logger.error("OPC UA client not available")
            return False
        
        try:
            self.client = Client(self.endpoint)
            
            # 配置安全策略
            if self.security_mode == "SignAndEncrypt":
                self.client.set_security_string(
                    f"Basic256Sha256,SignAndEncrypt,certificate.pem,private_key.pem"
                )
            elif self.security_mode == "Sign":
                self.client.set_security_string(
                    f"Basic256Sha256,Sign,certificate.pem,private_key.pem"
                )
            
            # 设置用户凭证
            if self.username and self.password:
                self.client.set_user(self.username)
                self.client.set_password(self.password)
            
            # 连接会话
            self.session = await self.client.connect()
            self.is_connected = True
            logger.info(f"Connected to OPC UA server at {self.endpoint}")
            return True
        except Exception as e:
            logger.error(f"Failed to connect to OPC UA server: {e}")
            return False
    
    async def disconnect(self):
        """断开 OPC UA 连接"""
        if self.session and self.is_connected:
            await self.client.disconnect()
            self.is_connected = False
            logger.info("Disconnected from OPC UA server")
    
    async def read_point(self, parameter_path: str, timestamp: Optional[datetime] = None) -> Optional[float]:
        """读取单个数据点 (节点值)"""
        if not self.is_connected:
            await self.connect()
        
        try:
            node = self.client.get_node(parameter_path)
            value = await node.read_value()
            return float(value) if value is not None else None
        except Exception as e:
            logger.error(f"Error reading OPC UA node {parameter_path}: {e}")
            return None
    
    async def read_range(self, parameter_path: str, start_time: datetime, end_time: datetime) -> List[Dict]:
        """读取时间范围数据 (需要 OPC UA 历史数据访问)"""
        if not self.is_connected:
            await self.connect()
        
        try:
            node = self.client.get_node(parameter_path)
            
            # 读取历史数据
            start_time_dt = ua.DateTime.from_datetime(start_time)
            end_time_dt = ua.DateTime.from_datetime(end_time)
            
            history = await node.read_raw_history_details(
                start_time=start_time_dt,
                end_time=end_time_dt,
                num_values_per_node=1000,
                return_bounds=False
            )
            
            results = []
            for data_value in history:
                results.append({
                    "timestamp": data_value.SourceTimestamp.timestamp(),
                    "value": float(data_value.Value.Value) if data_value.Value.Value else None,
                    "parameter": parameter_path
                })
            return results
        except Exception as e:
            logger.error(f"Error reading OPC UA history: {e}")
            return []
    
    async def subscribe(self, parameter_paths: List[str]) -> AsyncGenerator[Dict, None]:
        """订阅 OPC UA 节点数据变化"""
        if not self.is_connected:
            await self.connect()
        
        class SubscriptionHandler:
            def __init__(self, queue):
                self.queue = queue
            
            async def datachange_notification(self, node, val, data):
                await self.queue.put({
                    "parameter": node.nodeid.to_string(),
                    "value": float(val) if val is not None else None,
                    "timestamp": datetime.utcnow().isoformat()
                })
        
        import asyncio
        queue = asyncio.Queue()
        handler = SubscriptionHandler(queue)
        
        # 创建订阅
        subscription = await self.client.create_subscription(500, handler)
        
        # 订阅所有节点
        nodes = [self.client.get_node(path) for path in parameter_paths]
        await subscription.subscribe_data_change(nodes)
        
        logger.info(f"Subscribed to {len(parameter_paths)} OPC UA nodes")
        
        # 持续接收数据
        while True:
            try:
                data = await queue.get()
                yield data
            except asyncio.CancelledError:
                break
        
        # 取消订阅
        await subscription.delete()
    
    async def test_connection(self) -> bool:
        """测试 OPC UA 连接"""
        try:
            return await self.connect()
        finally:
            await self.disconnect()
