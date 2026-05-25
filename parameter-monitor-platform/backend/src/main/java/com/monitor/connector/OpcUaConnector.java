package com.monitor.connector;

import org.eclipse.milo.opcua.stack.client.UaClient;
import org.eclipse.milo.opcua.stack.client.config.UaClientConfig;
import org.eclipse.milo.opcua.stack.client.config.uasc.UaClientTcpConfig;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * OPC UA数据连接器
 * 支持通过OPC UA协议连接PLC、传感器等工业设备
 */
@Component
public class OpcUaConnector {
    
    private static final Logger logger = LoggerFactory.getLogger(OpcUaConnector.class);
    
    private UaClient client;
    private String endpointUrl;
    private boolean connected = false;
    
    /**
     * 连接到OPC UA服务器
     */
    public boolean connect(String endpointUrl) {
        this.endpointUrl = endpointUrl;
        
        try {
            // 创建OPC UA客户端配置
            UaClientConfig config = UaClientTcpConfig.builder()
                .setEndpointUrl(endpointUrl)
                .build();
            
            client = new UaClient(config);
            
            // 异步连接
            CompletableFuture<Void> future = client.connect();
            future.join();  // 等待连接完成
            
            connected = true;
            logger.info("成功连接到OPC UA服务器: {}", endpointUrl);
            return true;
            
        } catch (Exception e) {
            logger.error("连接OPC UA服务器失败: {}", e.getMessage(), e);
            connected = false;
            return false;
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (client != null && connected) {
            try {
                CompletableFuture<Void> future = client.disconnect();
                future.join();
                connected = false;
                logger.info("已断开OPC UA连接");
            } catch (Exception e) {
                logger.error("断开OPC UA连接失败: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * 读取单个节点的值
     */
    public Double readNodeValue(String nodeIdString) {
        if (!connected || client == null) {
            logger.warn("OPC UA未连接");
            return null;
        }
        
        try {
            NodeId nodeId = parseNodeId(nodeIdString);
            CompletableFuture<DataValue> future = client.readNode(nodeId);
            DataValue dataValue = future.get();
            
            if (dataValue.getValue().isGood()) {
                Object value = dataValue.getValue().getValue();
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }
        } catch (Exception e) {
            logger.error("读取OPC UA节点失败: {}, 错误: {}", nodeIdString, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 批量读取多个节点的值
     */
    public Map<String, Double> readMultipleNodes(List<String> nodeIds) {
        Map<String, Double> results = new HashMap<>();
        
        if (!connected || client == null) {
            logger.warn("OPC UA未连接");
            return results;
        }
        
        for (String nodeIdString : nodeIds) {
            Double value = readNodeValue(nodeIdString);
            if (value != null) {
                results.put(nodeIdString, value);
            }
        }
        
        return results;
    }
    
    /**
     * 解析NodeId字符串
     * 支持格式: "ns=2;s=Machine1.Temperature" 或 "i=1234"
     */
    private NodeId parseNodeId(String nodeIdString) {
        if (nodeIdString.startsWith("ns=")) {
            // 格式: ns=2;s=MyNode
            String[] parts = nodeIdString.substring(3).split(";");
            int namespaceIndex = Integer.parseInt(parts[0]);
            String identifier = parts[1].substring(2);  // 去掉"s="
            return new NodeId(namespaceIndex, identifier);
        } else if (nodeIdString.startsWith("i=")) {
            // 格式: i=1234
            int numericId = Integer.parseInt(nodeIdString.substring(2));
            return new NodeId(0, numericId);
        } else {
            // 默认为字符串标识符
            return new NodeId(2, nodeIdString);
        }
    }
    
    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return connected && client != null;
    }
    
    /**
     * 获取端点URL
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }
}
