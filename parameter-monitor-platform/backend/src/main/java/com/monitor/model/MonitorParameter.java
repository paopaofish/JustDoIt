package com.monitor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 监测参数实体类
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monitor_parameters")
public class MonitorParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    @Column(length = 500)
    private String path;  // 数据路径，如IoTDB的测点路径或OPC UA的节点ID

    @Column(length = 100)
    private String unit;  // 单位

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @Column(precision = 10, scale = 2)
    private Double minValue;

    @Column(precision = 10, scale = 2)
    private Double maxValue;

    @Column(columnDefinition = "TEXT")
    private String description;

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
     * 数据类型枚举
     */
    public enum DataType {
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN,
        STRING,
        DATETIME
    }
}
