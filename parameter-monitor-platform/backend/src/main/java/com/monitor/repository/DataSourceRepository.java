package com.monitor.repository;

import com.monitor.model.DataSource;
import com.monitor.model.DataSource.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源Repository接口
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {
    
    List<DataSource> findByEnabledTrue();
    
    List<DataSource> findByType(DataSourceType type);
    
    List<DataSource> findByTypeAndEnabledTrue(DataSourceType type);
    
    boolean existsByName(String name);
    
    @Query("SELECT COUNT(d) FROM DataSource d WHERE d.enabled = true")
    long countEnabledDataSources();
}
