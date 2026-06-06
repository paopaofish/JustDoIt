package com.monitor.algorithm;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 趋势预测算法
 * 使用线性回归进行时间序列预测
 */
@Component
public class TrendPredictionAlgorithm {
    
    private static final Logger logger = LoggerFactory.getLogger(TrendPredictionAlgorithm.class);
    
    /**
     * 基于历史数据进行趋势预测
     * @param historicalValues 历史数据值列表（按时间顺序）
     * @param predictSteps 预测步数
     * @return 预测值列表
     */
    public List<Double> predict(List<Double> historicalValues, int predictSteps) {
        if (historicalValues == null || historicalValues.size() < 2) {
            logger.warn("历史数据不足，无法进行预测");
            return new ArrayList<>();
        }
        
        SimpleRegression regression = new SimpleRegression();
        
        // 添加数据点（x为时间索引，y为值）
        for (int i = 0; i < historicalValues.size(); i++) {
            regression.addData(i, historicalValues.get(i));
        }
        
        List<Double> predictions = new ArrayList<>();
        int lastX = historicalValues.size() - 1;
        
        // 预测未来值
        for (int i = 1; i <= predictSteps; i++) {
            double predictedValue = regression.predict(lastX + i);
            predictions.add(predictedValue);
        }
        
        return predictions;
    }
    
    /**
     * 带置信区间的预测
     * @param historicalValues 历史数据值列表
     * @param predictSteps 预测步数
     * @return 包含预测值和置信区间的结果
     */
    public PredictionResult predictWithConfidence(List<Double> historicalValues, int predictSteps) {
        if (historicalValues == null || historicalValues.size() < 2) {
            return null;
        }
        
        SimpleRegression regression = new SimpleRegression();
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (int i = 0; i < historicalValues.size(); i++) {
            regression.addData(i, historicalValues.get(i));
            stats.addValue(historicalValues.get(i));
        }
        
        List<Double> predictions = new ArrayList<>();
        List<Double> upperBounds = new ArrayList<>();
        List<Double> lowerBounds = new ArrayList<>();
        
        int lastX = historicalValues.size() - 1;
        double stddev = stats.getStandardDeviation();
        
        for (int i = 1; i <= predictSteps; i++) {
            double predictedValue = regression.predict(lastX + i);
            double margin = 1.96 * stddev * Math.sqrt(1.0 + 1.0 / historicalValues.size() + 
                        Math.pow(i, 2) / getSxx(regression, historicalValues.size()));
            
            predictions.add(predictedValue);
            upperBounds.add(predictedValue + margin);
            lowerBounds.add(predictedValue - margin);
        }
        
        return new PredictionResult(predictions, upperBounds, lowerBounds);
    }
    
    /**
     * 计算Sxx（x的离差平方和）
     */
    private double getSxx(SimpleRegression regression, int n) {
        double sumX = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
        }
        double meanX = sumX / n;
        
        double sxx = 0;
        for (int i = 0; i < n; i++) {
            sxx += Math.pow(i - meanX, 2);
        }
        return sxx;
    }
    
    /**
     * 检测趋势变化
     * @param historicalValues 历史数据
     * @param threshold 变化阈值（斜率变化百分比）
     * @return 是否检测到显著趋势变化
     */
    public boolean detectTrendChange(List<Double> historicalValues, double threshold) {
        if (historicalValues == null || historicalValues.size() < 4) {
            return false;
        }
        
        int midPoint = historicalValues.size() / 2;
        List<Double> firstHalf = historicalValues.subList(0, midPoint);
        List<Double> secondHalf = historicalValues.subList(midPoint, historicalValues.size());
        
        SimpleRegression regression1 = new SimpleRegression();
        SimpleRegression regression2 = new SimpleRegression();
        
        for (int i = 0; i < firstHalf.size(); i++) {
            regression1.addData(i, firstHalf.get(i));
        }
        
        for (int i = 0; i < secondHalf.size(); i++) {
            regression2.addData(i, secondHalf.get(i));
        }
        
        double slope1 = regression1.getSlope();
        double slope2 = regression2.getSlope();
        
        if (Math.abs(slope1) < 0.0001) {
            return Math.abs(slope2) > threshold;
        }
        
        double changePercent = Math.abs((slope2 - slope1) / slope1);
        return changePercent > threshold;
    }
    
    /**
     * 预测结果类
     */
    public static class PredictionResult {
        private final List<Double> predictions;
        private final List<Double> upperBounds;
        private final List<Double> lowerBounds;
        
        public PredictionResult(List<Double> predictions, 
                               List<Double> upperBounds, 
                               List<Double> lowerBounds) {
            this.predictions = predictions;
            this.upperBounds = upperBounds;
            this.lowerBounds = lowerBounds;
        }
        
        public List<Double> getPredictions() {
            return predictions;
        }
        
        public List<Double> getUpperBounds() {
            return upperBounds;
        }
        
        public List<Double> getLowerBounds() {
            return lowerBounds;
        }
    }
}
