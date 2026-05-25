package com.monitor.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据传输对象 - 驾驶舱展示数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    
    /**
     * 概览统计信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Overview {
        private Long totalParameters;      // 总参数数量
        private Long activeParameters;     // 活跃参数数量
        private Long alarmCount;           // 当前告警数量
        private Long criticalAlarmCount;   // 严重告警数量
        private Long dataSourceCount;      // 数据源数量
        private Double healthScore;        // 健康评分
    }
    
    /**
     * 实时数据卡片
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RealtimeCard {
        private Long parameterId;
        private String parameterName;
        private String parameterCode;
        private Double currentValue;
        private String unit;
        private LocalDateTime lastUpdateTime;
        private String status;  // NORMAL, WARNING, ERROR
        private Double changeRate;  // 变化率
    }
    
    /**
     * 趋势图表数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendChart {
        private Long parameterId;
        private String parameterName;
        private List<DataPoint> dataPoints;
        private List<DataPoint> predictedPoints;  // 预测值
        private String unit;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataPoint {
        private LocalDateTime timestamp;
        private Double value;
    }
    
    /**
     * 告警列表
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlarmList {
        private Long total;
        private List<AlarmItem> items;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlarmItem {
        private Long id;
        private String parameterName;
        private String ruleName;
        private String alertLevel;
        private String message;
        private LocalDateTime alarmTime;
        private String status;
    }
    
    /**
     * 数据源状态
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataSourceStatus {
        private Long id;
        private String name;
        private String type;
        private Boolean connected;
        private LocalDateTime lastHeartbeat;
        private Integer parameterCount;
    }
}
