package com.monitor.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据传输对象 - 参数数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParameterDataDTO {
    
    private Long id;
    private Long parameterId;
    private String parameterName;
    private String parameterCode;
    private LocalDateTime timestamp;
    private Double value;
    private String stringValue;
    private String unit;
    private String quality;
    
    /**
     * 数据统计信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Statistics {
        private Long parameterId;
        private Integer count;
        private Double min;
        private Double max;
        private Double avg;
        private Double sum;
        private Double stddev;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
    
    /**
     * 时间范围查询参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeRangeQuery {
        private Long parameterId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer limit;
        private Boolean aggregate;  // 是否聚合
        private String aggregateInterval;  // 聚合间隔：1m, 5m, 1h等
    }
}
