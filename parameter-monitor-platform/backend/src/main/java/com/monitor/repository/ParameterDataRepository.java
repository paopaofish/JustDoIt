package com.monitor.repository;

import com.monitor.model.ParameterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 参数数据Repository接口
 */
@Repository
public interface ParameterDataRepository extends JpaRepository<ParameterData, Long> {
    
    List<ParameterData> findByParameterIdOrderByTimestampDesc(Long parameterId);
    
    List<ParameterData> findByParameterIdAndTimestampBetweenOrderByTimestampDesc(
        Long parameterId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    List<ParameterData> findTop10ByParameterIdOrderByTimestampDesc(Long parameterId);
    
    @Query("SELECT p FROM ParameterData p WHERE p.parameterId = :parameterId " +
           "AND p.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY p.timestamp DESC")
    List<ParameterData> findByParameterIdAndTimeRange(
        @Param("parameterId") Long parameterId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT MAX(p.value) FROM ParameterData p WHERE p.parameterId = :parameterId " +
           "AND p.timestamp BETWEEN :startTime AND :endTime")
    Double findMaxValueByParameterIdAndTimeRange(
        @Param("parameterId") Long parameterId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT MIN(p.value) FROM ParameterData p WHERE p.parameterId = :parameterId " +
           "AND p.timestamp BETWEEN :startTime AND :endTime")
    Double findMinValueByParameterIdAndTimeRange(
        @Param("parameterId") Long parameterId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT AVG(p.value) FROM ParameterData p WHERE p.parameterId = :parameterId " +
           "AND p.timestamp BETWEEN :startTime AND :endTime")
    Double findAvgValueByParameterIdAndTimeRange(
        @Param("parameterId") Long parameterId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
