package com.monitor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 参数数据记录实体类
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parameter_data")
@Index(name = "idx_param_time", columnList = "parameterId, timestamp")
public class ParameterData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long parameterId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(precision = 20, scale = 6)
    private Double value;

    @Column(length = 500)
    private String stringValue;

    @Column(length = 100)
    private String quality;  // 数据质量标识

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
