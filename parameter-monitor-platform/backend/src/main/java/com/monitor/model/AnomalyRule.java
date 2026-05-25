package com.monitor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 异常规则实体类
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "anomaly_rules")
public class AnomalyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private MonitorParameter parameter;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    @Column(precision = 10, scale = 2)
    private Double thresholdValue;

    @Column(precision = 10, scale = 2)
    private Double minValue;

    @Column(precision = 10, scale = 2)
    private Double maxValue;

    @Column(length = 500)
    private String expression;  // 自定义表达式

    @Column(nullable = false)
    private Integer durationSeconds = 0;  // 持续时间（秒），0表示立即触发

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;

    @Column(length = 500)
    private String alertMessage;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 规则类型枚举
     */
    public enum RuleType {
        THRESHOLD_HIGH,      // 高于阈值
        THRESHOLD_LOW,       // 低于阈值
        RANGE,              // 范围检查
        RATE_OF_CHANGE,     // 变化率
        CUSTOM_EXPRESSION,  // 自定义表达式
        TREND_PREDICTION    // 趋势预测异常
    }

    /**
     * 告警级别枚举
     */
    public enum AlertLevel {
        INFO,       // 信息
        WARNING,    // 警告
        ERROR,      // 错误
        CRITICAL    // 严重
    }
}
