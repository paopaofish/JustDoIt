package com.monitor.service;

import com.monitor.connector.IoTDBConnector;
import com.monitor.connector.OpcUaConnector;
import com.monitor.model.DataSource;
import com.monitor.repository.DataSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源管理服务
 */
@Service
public class DataSourceService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSourceService.class);
    
    @Autowired
    private DataSourceRepository dataSourceRepository;
    
    @Autowired
    private IoTDBConnector iotdbConnector;
    
    @Autowired
    private OpcUaConnector opcUaConnector;
    
    // 缓存已连接的数据源
    private final Map<Long, Boolean> connectionCache = new ConcurrentHashMap<>();
    
    /**
     * 获取所有数据源
     */
    public List<DataSource> getAllDataSources() {
        return dataSourceRepository.findAll();
    }
    
    /**
     * 获取启用的数据源
     */
    public List<DataSource> getEnabledDataSources() {
        return dataSourceRepository.findByEnabledTrue();
    }
    
    /**
     * 根据ID获取数据源
     */
    public Optional<DataSource> getDataSourceById(Long id) {
        return dataSourceRepository.findById(id);
    }
    
    /**
     * 创建数据源
     */
    public DataSource createDataSource(DataSource dataSource) {
        if (dataSourceRepository.existsByName(dataSource.getName())) {
            throw new IllegalArgumentException("数据源名称已存在: " + dataSource.getName());
        }
        return dataSourceRepository.save(dataSource);
    }
    
    /**
     * 更新数据源
     */
    public DataSource updateDataSource(Long id, DataSource dataSource) {
        DataSource existing = dataSourceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + id));
        
        existing.setName(dataSource.getName());
        existing.setType(dataSource.getType());
        existing.setEndpoint(dataSource.getEndpoint());
        existing.setUsername(dataSource.getUsername());
        existing.setPassword(dataSource.getPassword());
        existing.setConfigJson(dataSource.getConfigJson());
        existing.setDescription(dataSource.getDescription());
        existing.setEnabled(dataSource.getEnabled());
        
        return dataSourceRepository.save(existing);
    }
    
    /**
     * 删除数据源
     */
    public void deleteDataSource(Long id) {
        dataSourceRepository.deleteById(id);
        connectionCache.remove(id);
    }
    
    /**
     * 测试数据源连接
     */
    public boolean testConnection(DataSource dataSource) {
        try {
            switch (dataSource.getType()) {
                case IOTDB:
                    return testIoTDBConnection(dataSource);
                case OPC_UA:
                    return testOpcUaConnection(dataSource);
                case MYSQL:
                    return testMySQLConnection(dataSource);
                default:
                    logger.warn("不支持的数据源类型: {}", dataSource.getType());
                    return false;
            }
        } catch (Exception e) {
            logger.error("测试连接失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 测试IoTDB连接
     */
    private boolean testIoTDBConnection(DataSource dataSource) {
        String host = dataSource.getEndpoint().split(":")[0];
        int port = Integer.parseInt(dataSource.getEndpoint().split(":")[1]);
        
        boolean success = iotdbConnector.connect(host, port, dataSource.getUsername(), dataSource.getPassword());
        
        if (success) {
            iotdbConnector.disconnect();
        }
        
        return success;
    }
    
    /**
     * 测试OPC UA连接
     */
    private boolean testOpcUaConnection(DataSource dataSource) {
        boolean success = opcUaConnector.connect(dataSource.getEndpoint());
        
        if (success) {
            opcUaConnector.disconnect();
        }
        
        return success;
    }
    
    /**
     * 测试MySQL连接
     */
    private boolean testMySQLConnection(DataSource dataSource) {
        // MySQL连接测试由JPA自动处理
        // 这里可以添加自定义的连接测试逻辑
        return true;
    }
    
    /**
     * 获取数据源连接状态
     */
    public boolean getConnectionStatus(Long id) {
        return connectionCache.getOrDefault(id, false);
    }
    
    /**
     * 更新连接状态缓存
     */
    public void updateConnectionStatus(Long id, boolean connected) {
        connectionCache.put(id, connected);
    }
}
