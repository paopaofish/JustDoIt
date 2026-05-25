package com.monitor.algorithm;

import com.monitor.model.AnomalyRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 异常检测算法
 */
@Component
public class AnomalyDetectionAlgorithm {
    
    private static final Logger logger = LoggerFactory.getLogger(AnomalyDetectionAlgorithm.class);
    
    /**
     * 检查数据是否违反规则
     * @param value 当前值
     * @param rule 异常规则
     * @return 是否异常
     */
    public boolean checkAnomaly(Double value, AnomalyRule rule) {
        if (value == null || rule == null || !rule.getEnabled()) {
            return false;
        }
        
        switch (rule.getRuleType()) {
            case THRESHOLD_HIGH:
                return checkThresholdHigh(value, rule.getThresholdValue());
                
            case THRESHOLD_LOW:
                return checkThresholdLow(value, rule.getThresholdValue());
                
            case RANGE:
                return checkRange(value, rule.getMinValue(), rule.getMaxValue());
                
            case RATE_OF_CHANGE:
                // 变化率检查需要历史数据，由服务层处理
                return false;
                
            case CUSTOM_EXPRESSION:
                return checkCustomExpression(value, rule.getExpression());
                
            case TREND_PREDICTION:
                // 趋势预测异常由专门的算法处理
                return false;
                
            default:
                logger.warn("未知的规则类型: {}", rule.getRuleType());
                return false;
        }
    }
    
    /**
     * 检查是否高于阈值
     */
    private boolean checkThresholdHigh(Double value, Double threshold) {
        if (threshold == null) return false;
        return value > threshold;
    }
    
    /**
     * 检查是否低于阈值
     */
    private boolean checkThresholdLow(Double value, Double threshold) {
        if (threshold == null) return false;
        return value < threshold;
    }
    
    /**
     * 检查是否超出范围
     */
    private boolean checkRange(Double value, Double minValue, Double maxValue) {
        if (minValue == null && maxValue == null) return false;
        
        if (minValue != null && value < minValue) {
            return true;
        }
        
        if (maxValue != null && value > maxValue) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查自定义表达式
     * 支持简单的表达式如: "value > 100", "value < 50"
     */
    private boolean checkCustomExpression(Double value, String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 简单的表达式解析（生产环境建议使用更安全的表达式引擎）
            String expr = expression.replace("value", value.toString());
            
            // 支持基本的比较运算
            if (expr.contains(">=")) {
                String[] parts = expr.split(">=");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left >= right;
            } else if (expr.contains("<=")) {
                String[] parts = expr.split("<=");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left <= right;
            } else if (expr.contains(">")) {
                String[] parts = expr.split(">");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left > right;
            } else if (expr.contains("<")) {
                String[] parts = expr.split("<");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left < right;
            } else if (expr.contains("==")) {
                String[] parts = expr.split("==");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return Math.abs(left - right) < 0.0001;
            }
            
        } catch (Exception e) {
            logger.error("解析表达式失败: {}, 错误：{}", expression, e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 计算变化率
     * @param currentValue 当前值
     * @param previousValue 上一个值
     * @param timeIntervalSeconds 时间间隔（秒）
     * @return 变化率（单位/秒）
     */
    public Double calculateRateOfChange(Double currentValue, Double previousValue, int timeIntervalSeconds) {
        if (currentValue == null || previousValue == null || timeIntervalSeconds <= 0) {
            return null;
        }
        
        return (currentValue - previousValue) / timeIntervalSeconds;
    }
    
    /**
     * 检查变化率是否异常
     * @param rateOfChange 变化率
     * @param threshold 变化率阈值
     * @return 是否异常
     */
    public boolean checkRateOfChangeAnomaly(Double rateOfChange, Double threshold) {
        if (rateOfChange == null || threshold == null) {
            return false;
        }
        
        return Math.abs(rateOfChange) > threshold;
    }
    
    /**
     * 基于统计的异常检测（3-sigma原则）
     * @param value 当前值
     * @param mean 平均值
     * @param stddev 标准差
     * @param sigmaMultiplier Sigma倍数（通常为3）
     * @return 是否异常
     */
    public boolean checkStatisticalAnomaly(Double value, Double mean, Double stddev, double sigmaMultiplier) {
        if (value == null || mean == null || stddev == null || stddev <= 0) {
            return false;
        }
        
        double zScore = Math.abs((value - mean) / stddev);
        return zScore > sigmaMultiplier;
    }
}
