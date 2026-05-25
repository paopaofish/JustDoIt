package com.monitor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 告警记录实体类
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alarm_records")
public class AlarmRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private AnomalyRule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private MonitorParameter parameter;

    @Column(nullable = false)
    private LocalDateTime alarmTime;

    @Column(precision = 20, scale = 6)
    private Double alarmValue;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnomalyRule.AlertLevel alertLevel;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlarmStatus status;

    @Column(length = 200)
    private String handledBy;

    @Column
    private LocalDateTime handledAt;

    @Column(length = 500)
    private String handleComment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        alarmTime = LocalDateTime.now();
        status = AlarmStatus.NEW;
    }

    /**
     * 告警状态枚举
     */
    public enum AlarmStatus {
        NEW,            // 新告警
        ACKNOWLEDGED,   // 已确认
        HANDLED,        // 已处理
        CLOSED          // 已关闭
    }
}
