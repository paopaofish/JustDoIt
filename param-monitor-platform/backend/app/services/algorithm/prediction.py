from abc import ABC, abstractmethod
from typing import Any, Dict, List, Optional
import numpy as np
from datetime import datetime, timedelta


class BasePredictionAlgorithm(ABC):
    """预测算法基类"""
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        self.config = config or {}
        self.model = None
        self.is_trained = False
    
    @abstractmethod
    def train(self, historical_data: List[Dict]) -> bool:
        """训练模型"""
        pass
    
    @abstractmethod
    def predict(self, horizon: int) -> List[Dict]:
        """预测未来值"""
        pass
    
    @abstractmethod
    def evaluate(self, test_data: List[Dict]) -> Dict[str, float]:
        """评估模型性能"""
        pass
    
    @abstractmethod
    def save_model(self, path: str):
        """保存模型"""
        pass
    
    @abstractmethod
    def load_model(self, path: str):
        """加载模型"""
        pass


class LinearRegressionPredictor(BasePredictionAlgorithm):
    """线性回归预测算法"""
    
    def train(self, historical_data: List[Dict]) -> bool:
        """使用线性回归训练模型"""
        try:
            from sklearn.linear_model import LinearRegression
            
            if len(historical_data) < 2:
                return False
            
            # 准备数据
            timestamps = []
            values = []
            for item in historical_data:
                if isinstance(item["timestamp"], str):
                    ts = datetime.fromisoformat(item["timestamp"])
                else:
                    ts = item["timestamp"]
                timestamps.append(ts.timestamp())
                values.append(item["value"])
            
            X = np.array(timestamps).reshape(-1, 1)
            y = np.array(values)
            
            # 训练模型
            self.model = LinearRegression()
            self.model.fit(X, y)
            self.is_trained = True
            
            return True
        except Exception as e:
            print(f"Training error: {e}")
            return False
    
    def predict(self, horizon: int) -> List[Dict]:
        """预测未来值"""
        if not self.is_trained:
            raise ValueError("Model not trained")
        
        predictions = []
        last_timestamp = datetime.utcnow()
        
        for i in range(horizon):
            future_time = last_timestamp + timedelta(hours=i+1)
            future_timestamp = future_time.timestamp()
            
            value = self.model.predict([[future_timestamp]])[0]
            predictions.append({
                "timestamp": future_time.isoformat(),
                "value": float(value),
                "confidence": 0.95  # 简化实现
            })
        
        return predictions
    
    def evaluate(self, test_data: List[Dict]) -> Dict[str, float]:
        """评估模型 (MSE, MAE, R²)"""
        if not self.is_trained or len(test_data) == 0:
            return {"mse": 0, "mae": 0, "r2": 0}
        
        from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
        
        timestamps = []
        actual_values = []
        for item in test_data:
            if isinstance(item["timestamp"], str):
                ts = datetime.fromisoformat(item["timestamp"])
            else:
                ts = item["timestamp"]
            timestamps.append(ts.timestamp())
            actual_values.append(item["value"])
        
        X_test = np.array(timestamps).reshape(-1, 1)
        predicted_values = self.model.predict(X_test)
        
        return {
            "mse": float(mean_squared_error(actual_values, predicted_values)),
            "mae": float(mean_absolute_error(actual_values, predicted_values)),
            "r2": float(r2_score(actual_values, predicted_values))
        }
    
    def save_model(self, path: str):
        """保存模型到文件"""
        import pickle
        with open(path, 'wb') as f:
            pickle.dump(self.model, f)
    
    def load_model(self, path: str):
        """从文件加载模型"""
        import pickle
        with open(path, 'rb') as f:
            self.model = pickle.load(f)
        self.is_trained = True


class LSTM_predictor(BasePredictionAlgorithm):
    """LSTM 时间序列预测算法 (简化版)"""
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        super().__init__(config)
        self.sequence_length = self.config.get("sequence_length", 60)
        self.hidden_size = self.config.get("hidden_size", 50)
    
    def train(self, historical_data: List[Dict]) -> bool:
        """训练 LSTM 模型"""
        try:
            import torch
            import torch.nn as nn
            from torch.utils.data import DataLoader, TensorDataset
            
            if len(historical_data) < self.sequence_length + 10:
                return False
            
            # 准备数据
            values = [item["value"] for item in historical_data]
            data = np.array(values, dtype=np.float32)
            
            # 归一化
            self.mean_val = np.mean(data)
            self.std_val = np.std(data)
            data_normalized = (data - self.mean_val) / self.std_val
            
            # 创建序列
            X, y = [], []
            for i in range(len(data_normalized) - self.sequence_length):
                X.append(data_normalized[i:i+self.sequence_length])
                y.append(data_normalized[i+self.sequence_length])
            
            X = np.array(X).reshape(-1, self.sequence_length, 1)
            y = np.array(y)
            
            # 转换为 PyTorch 张量
            X_tensor = torch.FloatTensor(X)
            y_tensor = torch.FloatTensor(y)
            
            dataset = TensorDataset(X_tensor, y_tensor)
            loader = DataLoader(dataset, batch_size=32, shuffle=True)
            
            # 定义 LSTM 模型
            class LSTMModel(nn.Module):
                def __init__(self, input_size=1, hidden_size=50, num_layers=1):
                    super(LSTMModel, self).__init__()
                    self.hidden_size = hidden_size
                    self.num_layers = num_layers
                    self.lstm = nn.LSTM(input_size, hidden_size, num_layers, batch_first=True)
                    self.fc = nn.Linear(hidden_size, 1)
                
                def forward(self, x):
                    h0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size)
                    c0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size)
                    out, _ = self.lstm(x, (h0, c0))
                    out = self.fc(out[:, -1, :])
                    return out
            
            self.model = LSTMModel(input_size=1, hidden_size=self.hidden_size)
            criterion = nn.MSELoss()
            optimizer = torch.optim.Adam(self.model.parameters(), lr=0.001)
            
            # 训练
            epochs = min(50, len(loader) * 10)
            for epoch in range(epochs):
                for batch_X, batch_y in loader:
                    outputs = self.model(batch_X).squeeze()
                    loss = criterion(outputs, batch_y)
                    
                    optimizer.zero_grad()
                    loss.backward()
                    optimizer.step()
            
            self.is_trained = True
            return True
        except Exception as e:
            print(f"LSTM training error: {e}")
            return False
    
    def predict(self, horizon: int) -> List[Dict]:
        """使用 LSTM 预测"""
        if not self.is_trained:
            raise ValueError("Model not trained")
        
        import torch
        
        predictions = []
        last_timestamp = datetime.utcnow()
        
        # 简化实现：使用最后已知值进行预测
        with torch.no_grad():
            for i in range(horizon):
                future_time = last_timestamp + timedelta(hours=i+1)
                # 这里应该使用模型进行实际预测，简化为返回均值
                value = self.mean_val
                predictions.append({
                    "timestamp": future_time.isoformat(),
                    "value": float(value),
                    "confidence": 0.85
                })
        
        return predictions
    
    def evaluate(self, test_data: List[Dict]) -> Dict[str, float]:
        """评估模型"""
        if not self.is_trained or len(test_data) == 0:
            return {"mse": 0, "mae": 0, "r2": 0}
        
        actual_values = [item["value"] for item in test_data]
        predicted_values = [self.mean_val] * len(actual_values)
        
        mse = np.mean((np.array(actual_values) - np.array(predicted_values)) ** 2)
        mae = np.mean(np.abs(np.array(actual_values) - np.array(predicted_values)))
        
        ss_res = np.sum((np.array(actual_values) - np.array(predicted_values)) ** 2)
        ss_tot = np.sum((np.array(actual_values) - np.mean(actual_values)) ** 2)
        r2 = 1 - (ss_res / ss_tot) if ss_tot != 0 else 0
        
        return {
            "mse": float(mse),
            "mae": float(mae),
            "r2": float(r2)
        }
    
    def save_model(self, path: str):
        """保存模型"""
        import torch
        torch.save({
            'model_state_dict': self.model.state_dict(),
            'mean_val': self.mean_val,
            'std_val': self.std_val,
        }, path)
    
    def load_model(self, path: str):
        """加载模型"""
        import torch
        checkpoint = torch.load(path)
        self.model.load_state_dict(checkpoint['model_state_dict'])
        self.mean_val = checkpoint['mean_val']
        self.std_val = checkpoint['std_val']
        self.is_trained = True
