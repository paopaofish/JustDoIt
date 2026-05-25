package com.monitor.repository;

import com.monitor.model.AnomalyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 异常规则Repository接口
 */
@Repository
public interface AnomalyRuleRepository extends JpaRepository<AnomalyRule, Long> {
    
    List<AnomalyRule> findByParameterId(Long parameterId);
    
    List<AnomalyRule> findByParameterIdAndEnabledTrue(Long parameterId);
    
    List<AnomalyRule> findByEnabledTrue();
}
