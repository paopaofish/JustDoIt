from .base import BaseIngestion, IngestionFactory
from .iotdb_adapter import IoTDBIngestion
from .opcua_adapter import OPCUAIngestion

# 注册数据源适配器
IngestionFactory.register("iotdb", IoTDBIngestion)
IngestionFactory.register("opcua", OPCUAIngestion)

__all__ = ["BaseIngestion", "IngestionFactory", "IoTDBIngestion", "OPCUAIngestion"]
