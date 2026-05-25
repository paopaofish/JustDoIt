package com.monitor.repository;

import com.monitor.model.MonitorParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 监测参数Repository接口
 */
@Repository
public interface MonitorParameterRepository extends JpaRepository<MonitorParameter, Long> {
    
    List<MonitorParameter> findByDataSourceId(Long dataSourceId);
    
    List<MonitorParameter> findByEnabledTrue();
    
    List<MonitorParameter> findByDataSourceIdAndEnabledTrue(Long dataSourceId);
    
    Optional<MonitorParameter> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT COUNT(p) FROM MonitorParameter p WHERE p.enabled = true")
    long countEnabledParameters();
}
