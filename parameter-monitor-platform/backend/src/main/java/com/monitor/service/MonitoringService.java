package com.monitor.service;

import com.monitor.algorithm.AnomalyDetectionAlgorithm;
import com.monitor.algorithm.TrendPredictionAlgorithm;
import com.monitor.dto.DashboardDTO;
import com.monitor.dto.ParameterDataDTO;
import com.monitor.model.*;
import com.monitor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 监控数据服务
 */
@Service
public class MonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);
    
    @Autowired
    private MonitorParameterRepository parameterRepository;
    
    @Autowired
    private ParameterDataRepository dataRepository;
    
    @Autowired
    private AnomalyRuleRepository ruleRepository;
    
    @Autowired
    private AlarmRecordRepository alarmRepository;
    
    @Autowired
    private DataSourceRepository dataSourceRepository;
    
    @Autowired
    private AnomalyDetectionAlgorithm anomalyDetection;
    
    @Autowired
    private TrendPredictionAlgorithm trendPrediction;
    
    /**
     * 获取所有监测参数
     */
    public List<MonitorParameter> getAllParameters() {
        return parameterRepository.findAll();
    }
    
    /**
     * 获取启用的监测参数
     */
    public List<MonitorParameter> getEnabledParameters() {
        return parameterRepository.findByEnabledTrue();
    }
    
    /**
     * 创建监测参数
     */
    public MonitorParameter createParameter(MonitorParameter parameter) {
        if (parameterRepository.existsByCode(parameter.getCode())) {
            throw new IllegalArgumentException("参数编码已存在: " + parameter.getCode());
        }
        return parameterRepository.save(parameter);
    }
    
    /**
     * 更新监测参数
     */
    public MonitorParameter updateParameter(Long id, MonitorParameter parameter) {
        MonitorParameter existing = parameterRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("参数不存在: " + id));
        
        existing.setName(parameter.getName());
        existing.setCode(parameter.getCode());
        existing.setPath(parameter.getPath());
        existing.setUnit(parameter.getUnit());
        existing.setDataType(parameter.getDataType());
        existing.setMinValue(parameter.getMinValue());
        existing.setMaxValue(parameter.getMaxValue());
        existing.setDescription(parameter.getDescription());
        existing.setEnabled(parameter.getEnabled());
        
        return parameterRepository.save(existing);
    }
    
    /**
     * 删除监测参数
     */
    public void deleteParameter(Long id) {
        parameterRepository.deleteById(id);
    }
    
    /**
     * 记录参数数据
     */
    public void recordData(Long parameterId, Double value, String quality) {
        ParameterData data = new ParameterData();
        data.setParameterId(parameterId);
        data.setValue(value);
        data.setTimestamp(LocalDateTime.now());
        data.setQuality(quality != null ? quality : "GOOD");
        
        dataRepository.save(data);
        
        // 检查异常规则
        checkAnomalyRules(parameterId, value);
    }
    
    /**
     * 查询参数历史数据
     */
    public List<ParameterData> queryHistory(Long parameterId, LocalDateTime startTime, LocalDateTime endTime) {
        return dataRepository.findByParameterIdAndTimestampBetweenOrderByTimestampDesc(
            parameterId, startTime, endTime);
    }
    
    /**
     * 获取最新数据
     */
    public Optional<ParameterData> getLatestData(Long parameterId) {
        List<ParameterData> dataList = dataRepository.findTop10ByParameterIdOrderByTimestampDesc(parameterId);
        return dataList.isEmpty() ? Optional.empty() : Optional.of(dataList.get(0));
    }
    
    /**
     * 获取数据统计信息
     */
    public ParameterDataDTO.Statistics getStatistics(Long parameterId, LocalDateTime startTime, LocalDateTime endTime) {
        Double max = dataRepository.findMaxValueByParameterIdAndTimeRange(parameterId, startTime, endTime);
        Double min = dataRepository.findMinValueByParameterIdAndTimeRange(parameterId, startTime, endTime);
        Double avg = dataRepository.findAvgValueByParameterIdAndTimeRange(parameterId, startTime, endTime);
        
        return ParameterDataDTO.Statistics.builder()
            .parameterId(parameterId)
            .max(max)
            .min(min)
            .avg(avg)
            .startTime(startTime)
            .endTime(endTime)
            .build();
    }
    
    /**
     * 创建异常规则
     */
    public AnomalyRule createRule(AnomalyRule rule) {
        return ruleRepository.save(rule);
    }
    
    /**
     * 更新异常规则
     */
    public AnomalyRule updateRule(Long id, AnomalyRule rule) {
        AnomalyRule existing = ruleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));
        
        existing.setName(rule.getName());
        existing.setRuleType(rule.getRuleType());
        existing.setThresholdValue(rule.getThresholdValue());
        existing.setMinValue(rule.getMinValue());
        existing.setMaxValue(rule.getMaxValue());
        existing.setExpression(rule.getExpression());
        existing.setDurationSeconds(rule.getDurationSeconds());
        existing.setAlertLevel(rule.getAlertLevel());
        existing.setAlertMessage(rule.getAlertMessage());
        existing.setEnabled(rule.getEnabled());
        
        return ruleRepository.save(existing);
    }
    
    /**
     * 删除异常规则
     */
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }
    
    /**
     * 获取参数的规则列表
     */
    public List<AnomalyRule> getRulesByParameter(Long parameterId) {
        return ruleRepository.findByParameterId(parameterId);
    }
    
    /**
     * 检查异常规则
     */
    private void checkAnomalyRules(Long parameterId, Double value) {
        List<AnomalyRule> rules = ruleRepository.findByParameterIdAndEnabledTrue(parameterId);
        
        for (AnomalyRule rule : rules) {
            if (anomalyDetection.checkAnomaly(value, rule)) {
                createAlarm(rule, parameterId, value);
            }
        }
    }
    
    /**
     * 创建告警记录
     */
    private void createAlarm(AnomalyRule rule, Long parameterId, Double value) {
        AlarmRecord alarm = new AlarmRecord();
        alarm.setRule(rule);
        alarm.setParameter(parameterRepository.findById(parameterId).orElse(null));
        alarm.setAlarmValue(value);
        alarm.setAlertLevel(rule.getAlertLevel());
        alarm.setMessage(rule.getAlertMessage() != null ? 
            rule.getAlertMessage() : 
            "参数异常: " + rule.getName());
        
        alarmRepository.save(alarm);
        logger.warn("产生告警: {} - {}", rule.getName(), value);
    }
    
    /**
     * 获取告警列表
     */
    public List<AlarmRecord> getAlarms(AlarmRecord.AlarmStatus status) {
        if (status != null) {
            return alarmRepository.findByStatusOrderByAlarmTimeDesc(status);
        }
        return alarmRepository.findAll();
    }
    
    /**
     * 处理告警
     */
    public void handleAlarm(Long alarmId, String handledBy, String comment) {
        AlarmRecord alarm = alarmRepository.findById(alarmId)
            .orElseThrow(() -> new IllegalArgumentException("告警不存在: " + alarmId));
        
        alarm.setStatus(AlarmRecord.AlarmStatus.HANDLED);
        alarm.setHandledBy(handledBy);
        alarm.setHandledAt(LocalDateTime.now());
        alarm.setHandleComment(comment);
        
        alarmRepository.save(alarm);
    }
    
    /**
     * 获取趋势预测
     */
    public List<Double> getPrediction(Long parameterId, int steps) {
        List<ParameterData> history = dataRepository.findTop10ByParameterIdOrderByTimestampDesc(parameterId);
        
        if (history.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 反转顺序（从旧到新）
        Collections.reverse(history);
        List<Double> values = history.stream()
            .map(ParameterData::getValue)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        return trendPrediction.predict(values, steps);
    }
    
    /**
     * 获取驾驶舱概览数据
     */
    public DashboardDTO.Overview getDashboardOverview() {
        long totalParams = parameterRepository.count();
        long activeParams = parameterRepository.countEnabledParameters();
        long alarmCount = alarmRepository.countNewAlarms();
        long criticalAlarmCount = alarmRepository.countCriticalNewAlarms();
        long dataSourceCount = dataSourceRepository.countEnabledDataSources();
        
        // 计算健康评分（简单算法：100 - 告警影响）
        double healthScore = Math.max(0, 100 - (alarmCount * 5) - (criticalAlarmCount * 10));
        
        return DashboardDTO.Overview.builder()
            .totalParameters(totalParams)
            .activeParameters(activeParams)
            .alarmCount(alarmCount)
            .criticalAlarmCount(criticalAlarmCount)
            .dataSourceCount(dataSourceCount)
            .healthScore(healthScore)
            .build();
    }
}
