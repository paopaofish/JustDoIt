package com.monitor.repository;

import com.monitor.model.AlarmRecord;
import com.monitor.model.AlarmRecord.AlarmStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警记录Repository接口
 */
@Repository
public interface AlarmRecordRepository extends JpaRepository<AlarmRecord, Long> {
    
    List<AlarmRecord> findByParameterId(Long parameterId);
    
    List<AlarmRecord> findByRuleId(Long ruleId);
    
    List<AlarmRecord> findByStatus(AlarmStatus status);
    
    List<AlarmRecord> findByStatusOrderByAlarmTimeDesc(AlarmStatus status);
    
    List<AlarmRecord> findByAlarmTimeBetweenOrderByAlarmTimeDesc(
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    @Query("SELECT COUNT(a) FROM AlarmRecord a WHERE a.status = 'NEW'")
    long countNewAlarms();
    
    @Query("SELECT COUNT(a) FROM AlarmRecord a WHERE a.status = 'NEW' AND a.alertLevel = 'CRITICAL'")
    long countCriticalNewAlarms();
}
