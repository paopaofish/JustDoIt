package com.monitor.connector;

import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoTDB数据连接器
 * 支持Apache IoTDB时序数据库的连接和数据读取
 */
@Component
public class IoTDBConnector {
    
    private static final Logger logger = LoggerFactory.getLogger(IoTDBConnector.class);
    
    private Session session;
    private String host;
    private int port;
    private String username;
    private String password;
    
    /**
     * 连接IoTDB
     */
    public boolean connect(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        
        try {
            session = new Session(host, port, username, password);
            session.open(false);
            logger.info("成功连接到IoTDB: {}:{}", host, port);
            return true;
        } catch (IoTDBConnectionException e) {
            logger.error("连接IoTDB失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (session != null && session.isClosed()) {
            try {
                session.close();
                logger.info("已断开IoTDB连接");
            } catch (IoTDBConnectionException e) {
                logger.error("断开IoTDB连接失败: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * 查询时序数据
     * @param paths IoTDB测点路径列表，如 ["root.sg1.d1.temperature"]
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询结果
     */
    public List<Map<String, Object>> queryData(List<String> paths, long startTime, long endTime) {
        if (session == null || session.isClosed()) {
            logger.warn("IoTDB未连接");
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            StringBuilder sql = new StringBuilder("SELECT ");
            for (int i = 0; i < paths.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append(paths.get(i));
            }
            sql.append(" FROM root.** WHERE Time >= ").append(startTime)
               .append(" AND Time <= ").append(endTime);
            
            SessionDataSet dataSet = session.executeQueryStatement(sql.toString());
            List<String> columnNames = dataSet.getColumnNames();
            
            while (dataSet.hasNext()) {
                Map<String, Object> row = new HashMap<>();
                org.apache.tsfile.read.common.RowRecord record = dataSet.next();
                
                for (int i = 0; i < columnNames.size(); i++) {
                    row.put(columnNames.get(i), record.getValues().get(i).getStringValue());
                }
                
                results.add(row);
            }
            
            dataSet.closeOperationHandle();
            
        } catch (StatementExecutionException | IoTDBConnectionException e) {
            logger.error("查询IoTDB数据失败: {}", e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * 获取最新数据点
     */
    public Map<String, Double> getLatestValues(List<String> paths) {
        long now = System.currentTimeMillis();
        long fiveMinutesAgo = now - 5 * 60 * 1000;
        
        List<Map<String, Object>> data = queryData(paths, fiveMinutesAgo, now);
        Map<String, Double> latestValues = new HashMap<>();
        
        if (!data.isEmpty()) {
            // 取最后一条记录
            Map<String, Object> lastRow = data.get(data.size() - 1);
            for (Map.Entry<String, Object> entry : lastRow.entrySet()) {
                if (!"Time".equals(entry.getKey())) {
                    try {
                        latestValues.put(entry.getKey(), Double.parseDouble(entry.getValue().toString()));
                    } catch (NumberFormatException e) {
                        logger.warn("解析数值失败: {}", entry.getValue());
                    }
                }
            }
        }
        
        return latestValues;
    }
    
    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return session != null && !session.isClosed();
    }
}
