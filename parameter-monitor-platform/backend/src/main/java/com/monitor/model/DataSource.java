package com.monitor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据源实体类
 * 支持IoTDB、OPC UA、MySQL等多种数据源类型
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "data_sources")
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DataSourceType type;

    @Column(length = 500)
    private String endpoint;

    @Column(length = 200)
    private String username;

    @Column(length = 200)
    private String password;

    @Column(length = 1000)
    private String configJson;

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
     * 数据源类型枚举
     */
    public enum DataSourceType {
        IOTDB,      // IoTDB时序数据库
        OSI_PI,     // OSI PI实时数据库
        MYSQL,      // MySQL关系数据库
        OPC_UA,     // OPC UA工业协议
        MQTT,       // MQTT消息队列
        MODBUS,     // Modbus协议
        OTHER       // 其他类型
    }
}
