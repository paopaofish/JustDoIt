package com.monitor.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 趋势预测算法单元测试
 */
@DisplayName("TrendPredictionAlgorithm 单元测试")
class TrendPredictionAlgorithmTest {

    private TrendPredictionAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new TrendPredictionAlgorithm();
    }

    @Nested
    @DisplayName("predict 方法测试")
    class PredictTests {

        @Test
        @DisplayName("当历史数据为 null 时应返回空列表")
        void shouldReturnEmptyListWhenHistoricalValuesIsNull() {
            List<Double> result = algorithm.predict(null, 5);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("当历史数据少于 2 个时应返回空列表")
        void shouldReturnEmptyListWhenHistoricalValuesHasLessThanTwoElements() {
            List<Double> result = algorithm.predict(Arrays.asList(100.0), 5);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("正常线性趋势预测")
        void shouldPredictLinearTrendCorrectly() {
            // 线性增长：10, 20, 30, 40, 50
            List<Double> historicalValues = Arrays.asList(10.0, 20.0, 30.0, 40.0, 50.0);
            List<Double> predictions = algorithm.predict(historicalValues, 3);

            assertEquals(3, predictions.size());
            // 预测应该继续线性增长：60, 70, 80
            assertEquals(60.0, predictions.get(0), 0.01);
            assertEquals(70.0, predictions.get(1), 0.01);
            assertEquals(80.0, predictions.get(2), 0.01);
        }

        @Test
        @DisplayName("正常下降趋势预测")
        void shouldPredictDecreasingTrendCorrectly() {
            // 线性下降：100, 90, 80, 70, 60
            List<Double> historicalValues = Arrays.asList(100.0, 90.0, 80.0, 70.0, 60.0);
            List<Double> predictions = algorithm.predict(historicalValues, 2);

            assertEquals(2, predictions.size());
            // 预测应该继续下降：50, 40
            assertEquals(50.0, predictions.get(0), 0.01);
            assertEquals(40.0, predictions.get(1), 0.01);
        }

        @Test
        @DisplayName("波动数据的趋势预测")
        void shouldPredictWithFluctuatingData() {
            // 波动数据：100, 105, 98, 107, 102
            List<Double> historicalValues = Arrays.asList(100.0, 105.0, 98.0, 107.0, 102.0);
            List<Double> predictions = algorithm.predict(historicalValues, 2);

            assertEquals(2, predictions.size());
            assertNotNull(predictions.get(0));
            assertNotNull(predictions.get(1));
        }

        @Test
        @DisplayName("预测步数为 0 时应返回空列表")
        void shouldReturnEmptyListWhenPredictStepsIsZero() {
            List<Double> historicalValues = Arrays.asList(10.0, 20.0, 30.0);
            List<Double> predictions = algorithm.predict(historicalValues, 0);
            assertTrue(predictions.isEmpty());
        }

        @Test
        @DisplayName("预测步数为负数时应返回空列表")
        void shouldReturnEmptyListWhenPredictStepsIsNegative() {
            List<Double> historicalValues = Arrays.asList(10.0, 20.0, 30.0);
            List<Double> predictions = algorithm.predict(historicalValues, -1);
            assertTrue(predictions.isEmpty());
        }
    }

    @Nested
    @DisplayName("predictWithConfidence 方法测试")
    class PredictWithConfidenceTests {

        @Test
        @DisplayName("当历史数据为 null 时应返回 null")
        void shouldReturnNullWhenHistoricalValuesIsNull() {
            TrendPredictionAlgorithm.PredictionResult result = 
                algorithm.predictWithConfidence(null, 5);
            assertNull(result);
        }

        @Test
        @DisplayName("当历史数据少于 2 个时应返回 null")
        void shouldReturnNullWhenHistoricalValuesHasLessThanTwoElements() {
            TrendPredictionAlgorithm.PredictionResult result = 
                algorithm.predictWithConfidence(Arrays.asList(100.0), 5);
            assertNull(result);
        }

        @Test
        @DisplayName("正常预测应返回包含预测值和置信区间的结果")
        void shouldReturnPredictionResultWithConfidenceIntervals() {
            // 线性增长：10, 20, 30, 40, 50
            List<Double> historicalValues = Arrays.asList(10.0, 20.0, 30.0, 40.0, 50.0);
            TrendPredictionAlgorithm.PredictionResult result = 
                algorithm.predictWithConfidence(historicalValues, 3);

            assertNotNull(result);
            assertEquals(3, result.getPredictions().size());
            assertEquals(3, result.getUpperBounds().size());
            assertEquals(3, result.getLowerBounds().size());

            // 上界应该大于预测值
            for (int i = 0; i < 3; i++) {
                assertTrue(result.getUpperBounds().get(i) > result.getPredictions().get(i));
                assertTrue(result.getLowerBounds().get(i) < result.getPredictions().get(i));
            }
        }

        @Test
        @DisplayName("预测值应与普通 predict 方法一致")
        void shouldMatchPredictionsWithRegularPredictMethod() {
            List<Double> historicalValues = Arrays.asList(10.0, 20.0, 30.0, 40.0, 50.0);
            
            List<Double> regularPredictions = algorithm.predict(historicalValues, 3);
            TrendPredictionAlgorithm.PredictionResult confidenceResult = 
                algorithm.predictWithConfidence(historicalValues, 3);

            assertEquals(regularPredictions.size(), confidenceResult.getPredictions().size());
            for (int i = 0; i < regularPredictions.size(); i++) {
                assertEquals(regularPredictions.get(i), 
                    confidenceResult.getPredictions().get(i), 0.01);
            }
        }
    }

    @Nested
    @DisplayName("detectTrendChange 方法测试")
    class DetectTrendChangeTests {

        @Test
        @DisplayName("当历史数据为 null 时应返回 false")
        void shouldReturnFalseWhenHistoricalValuesIsNull() {
            assertFalse(algorithm.detectTrendChange(null, 0.5));
        }

        @Test
        @DisplayName("当历史数据少于 4 个时应返回 false")
        void shouldReturnFalseWhenHistoricalValuesHasLessThanFourElements() {
            assertFalse(algorithm.detectTrendChange(Arrays.asList(10.0, 20.0, 30.0), 0.5));
        }

        @Test
        @DisplayName("稳定趋势应不检测到变化")
        void shouldNotDetectChangeForStableTrend() {
            // 稳定的线性增长：10, 20, 30, 40, 50, 60
            List<Double> historicalValues = Arrays.asList(10.0, 20.0, 30.0, 40.0, 50.0, 60.0);
            assertFalse(algorithm.detectTrendChange(historicalValues, 0.5));
        }

        @Test
        @DisplayName("显著趋势变化应被检测到")
        void shouldDetectSignificantTrendChange() {
            // 前半段平缓，后半段陡峭：10, 11, 12, 13, 50, 90, 130, 170
            List<Double> historicalValues = Arrays.asList(10.0, 11.0, 12.0, 13.0, 50.0, 90.0, 130.0, 170.0);
            assertTrue(algorithm.detectTrendChange(historicalValues, 0.5));
        }

        @Test
        @DisplayName("趋势从上升到下降应被检测到")
        void shouldDetectTrendReversal() {
            // 上升后下降：10, 20, 30, 40, 35, 30, 25, 20
            List<Double> historicalValues = Arrays.asList(10.0, 20.0, 30.0, 40.0, 35.0, 30.0, 25.0, 20.0);
            assertTrue(algorithm.detectTrendChange(historicalValues, 0.5));
        }

        @Test
        @DisplayName("小阈值应更容易检测到变化")
        void shouldDetectMoreChangesWithSmallThreshold() {
            List<Double> historicalValues = Arrays.asList(10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0);
            
            // 小阈值应该检测到变化
            boolean detectedWithSmallThreshold = algorithm.detectTrendChange(historicalValues, 0.1);
            // 大阈值可能检测不到变化
            boolean detectedWithLargeThreshold = algorithm.detectTrendChange(historicalValues, 0.9);
            
            assertTrue(detectedWithSmallThreshold || !detectedWithLargeThreshold);
        }

        @Test
        @DisplayName("刚好 4 个数据点时应能处理")
        void shouldHandleExactlyFourDataPoints() {
            List<Double> historicalValues = Arrays.asList(10.0, 20.0, 30.0, 40.0);
            // 不应该抛出异常
            assertDoesNotThrow(() -> algorithm.detectTrendChange(historicalValues, 0.5));
        }
    }

    @Nested
    @DisplayName("PredictionResult 内部类测试")
    class PredictionResultTests {

        @Test
        @DisplayName("PredictionResult 应正确存储和返回数据")
        void shouldStoreAndReturnDataCorrectly() {
            List<Double> predictions = Arrays.asList(100.0, 110.0, 120.0);
            List<Double> upperBounds = Arrays.asList(110.0, 120.0, 130.0);
            List<Double> lowerBounds = Arrays.asList(90.0, 100.0, 110.0);

            TrendPredictionAlgorithm.PredictionResult result = 
                new TrendPredictionAlgorithm.PredictionResult(predictions, upperBounds, lowerBounds);

            assertEquals(predictions, result.getPredictions());
            assertEquals(upperBounds, result.getUpperBounds());
            assertEquals(lowerBounds, result.getLowerBounds());
        }

        @Test
        @DisplayName("PredictionResult 应允许空列表")
        void shouldAllowEmptyLists() {
            TrendPredictionAlgorithm.PredictionResult result = 
                new TrendPredictionAlgorithm.PredictionResult(
                    Arrays.asList(), 
                    Arrays.asList(), 
                    Arrays.asList()
                );

            assertTrue(result.getPredictions().isEmpty());
            assertTrue(result.getUpperBounds().isEmpty());
            assertTrue(result.getLowerBounds().isEmpty());
        }
    }
}
