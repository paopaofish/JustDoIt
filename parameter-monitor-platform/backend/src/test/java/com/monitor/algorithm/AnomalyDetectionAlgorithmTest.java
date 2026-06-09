package com.monitor.algorithm;

import com.monitor.model.AnomalyRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 异常检测算法单元测试
 */
@DisplayName("AnomalyDetectionAlgorithm 单元测试")
class AnomalyDetectionAlgorithmTest {

    private AnomalyDetectionAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new AnomalyDetectionAlgorithm();
    }

    @Nested
    @DisplayName("checkAnomaly 方法测试")
    class CheckAnomalyTests {

        @Test
        @DisplayName("当值为 null 时应返回 false")
        void shouldReturnFalseWhenValueIsNull() {
            AnomalyRule rule = createRule(AnomalyRule.RuleType.THRESHOLD_HIGH, 100.0);
            assertFalse(algorithm.checkAnomaly(null, rule));
        }

        @Test
        @DisplayName("当规则为 null 时应返回 false")
        void shouldReturnFalseWhenRuleIsNull() {
            assertFalse(algorithm.checkAnomaly(50.0, null));
        }

        @Test
        @DisplayName("当规则未启用时应返回 false")
        void shouldReturnFalseWhenRuleIsDisabled() {
            AnomalyRule rule = createRule(AnomalyRule.RuleType.THRESHOLD_HIGH, 100.0);
            rule.setEnabled(false);
            assertFalse(algorithm.checkAnomaly(150.0, rule));
        }

        @Test
        @DisplayName("THRESHOLD_HIGH 类型 - 值高于阈值时应返回 true")
        void shouldReturnTrueWhenValueExceedsHighThreshold() {
            AnomalyRule rule = createRule(AnomalyRule.RuleType.THRESHOLD_HIGH, 100.0);
            assertTrue(algorithm.checkAnomaly(100.01, rule));
            assertTrue(algorithm.checkAnomaly(150.0, rule));
        }

        @Test
        @DisplayName("THRESHOLD_HIGH 类型 - 值等于或低于阈值时应返回 false")
        void shouldReturnFalseWhenValueBelowOrEqualToHighThreshold() {
            AnomalyRule rule = createRule(AnomalyRule.RuleType.THRESHOLD_HIGH, 100.0);
            assertFalse(algorithm.checkAnomaly(100.0, rule));
            assertFalse(algorithm.checkAnomaly(99.99, rule));
        }

        @Test
        @DisplayName("THRESHOLD_LOW 类型 - 值低于阈值时应返回 true")
        void shouldReturnTrueWhenValueBelowLowThreshold() {
            AnomalyRule rule = createRule(AnomalyRule.RuleType.THRESHOLD_LOW, 50.0);
            assertTrue(algorithm.checkAnomaly(49.99, rule));
            assertTrue(algorithm.checkAnomaly(0.0, rule));
        }

        @Test
        @DisplayName("THRESHOLD_LOW 类型 - 值等于或高于阈值时应返回 false")
        void shouldReturnFalseWhenValueAboveOrEqualToLowThreshold() {
            AnomalyRule rule = createRule(AnomalyRule.RuleType.THRESHOLD_LOW, 50.0);
            assertFalse(algorithm.checkAnomaly(50.0, rule));
            assertFalse(algorithm.checkAnomaly(50.01, rule));
        }

        @Test
        @DisplayName("RANGE 类型 - 值超出范围时应返回 true")
        void shouldReturnTrueWhenValueOutOfRange() {
            AnomalyRule rule = createRangeRule(10.0, 90.0);
            assertTrue(algorithm.checkAnomaly(5.0, rule));
            assertTrue(algorithm.checkAnomaly(95.0, rule));
        }

        @Test
        @DisplayName("RANGE 类型 - 值在范围内时应返回 false")
        void shouldReturnFalseWhenValueInRange() {
            AnomalyRule rule = createRangeRule(10.0, 90.0);
            assertFalse(algorithm.checkAnomaly(10.0, rule));
            assertFalse(algorithm.checkAnomaly(50.0, rule));
            assertFalse(algorithm.checkAnomaly(90.0, rule));
        }

        @Test
        @DisplayName("RANGE 类型 - 只有最小值限制时应正确判断")
        void shouldWorkWithOnlyMinValue() {
            AnomalyRule rule = createRangeRule(10.0, null);
            assertTrue(algorithm.checkAnomaly(5.0, rule));
            assertFalse(algorithm.checkAnomaly(10.0, rule));
            assertFalse(algorithm.checkAnomaly(100.0, rule));
        }

        @Test
        @DisplayName("RANGE 类型 - 只有最大值限制时应正确判断")
        void shouldWorkWithOnlyMaxValue() {
            AnomalyRule rule = createRangeRule(null, 90.0);
            assertFalse(algorithm.checkAnomaly(5.0, rule));
            assertFalse(algorithm.checkAnomaly(90.0, rule));
            assertTrue(algorithm.checkAnomaly(95.0, rule));
        }

        @Test
        @DisplayName("CUSTOM_EXPRESSION 类型 - 表达式 value > 100 时应正确判断")
        void shouldEvaluateCustomExpressionGreaterThan() {
            AnomalyRule rule = createExpressionRule("value > 100");
            assertTrue(algorithm.checkAnomaly(101.0, rule));
            assertFalse(algorithm.checkAnomaly(100.0, rule));
            assertFalse(algorithm.checkAnomaly(99.0, rule));
        }

        @Test
        @DisplayName("CUSTOM_EXPRESSION 类型 - 表达式 value < 50 时应正确判断")
        void shouldEvaluateCustomExpressionLessThan() {
            AnomalyRule rule = createExpressionRule("value < 50");
            assertTrue(algorithm.checkAnomaly(49.0, rule));
            assertFalse(algorithm.checkAnomaly(50.0, rule));
            assertFalse(algorithm.checkAnomaly(51.0, rule));
        }

        @Test
        @DisplayName("CUSTOM_EXPRESSION 类型 - 表达式 value >= 100 时应正确判断")
        void shouldEvaluateCustomExpressionGreaterThanOrEqual() {
            AnomalyRule rule = createExpressionRule("value >= 100");
            assertTrue(algorithm.checkAnomaly(100.0, rule));
            assertTrue(algorithm.checkAnomaly(101.0, rule));
            assertFalse(algorithm.checkAnomaly(99.9, rule));
        }

        @Test
        @DisplayName("CUSTOM_EXPRESSION 类型 - 表达式 value <= 50 时应正确判断")
        void shouldEvaluateCustomExpressionLessThanOrEqual() {
            AnomalyRule rule = createExpressionRule("value <= 50");
            assertTrue(algorithm.checkAnomaly(50.0, rule));
            assertTrue(algorithm.checkAnomaly(49.0, rule));
            assertFalse(algorithm.checkAnomaly(50.1, rule));
        }

        @Test
        @DisplayName("CUSTOM_EXPRESSION 类型 - 表达式 value == 100 时应正确判断")
        void shouldEvaluateCustomExpressionEqual() {
            AnomalyRule rule = createExpressionRule("value == 100");
            assertTrue(algorithm.checkAnomaly(100.0, rule));
            assertFalse(algorithm.checkAnomaly(99.9999, rule));
            assertFalse(algorithm.checkAnomaly(100.0001, rule));
        }

        @Test
        @DisplayName("CUSTOM_EXPRESSION 类型 - 无效表达式应返回 false")
        void shouldReturnFalseForInvalidExpression() {
            AnomalyRule rule = createExpressionRule("invalid expression");
            assertFalse(algorithm.checkAnomaly(100.0, rule));
        }

        @Test
        @DisplayName("CUSTOM_EXPRESSION 类型 - 空表达式应返回 false")
        void shouldReturnFalseForEmptyExpression() {
            AnomalyRule rule = createExpressionRule("");
            assertFalse(algorithm.checkAnomaly(100.0, rule));
        }

        @Test
        @DisplayName("RATE_OF_CHANGE 类型应返回 false（由服务层处理）")
        void shouldReturnFalseForRateOfChangeType() {
            AnomalyRule rule = createRule(AnomalyRule.RuleType.RATE_OF_CHANGE, 10.0);
            assertFalse(algorithm.checkAnomaly(50.0, rule));
        }

        @Test
        @DisplayName("TREND_PREDICTION 类型应返回 false（由专门算法处理）")
        void shouldReturnFalseForTrendPredictionType() {
            AnomalyRule rule = createRule(AnomalyRule.RuleType.TREND_PREDICTION, 10.0);
            assertFalse(algorithm.checkAnomaly(50.0, rule));
        }

        @Test
        @DisplayName("未知规则类型应返回 false")
        void shouldReturnFalseForUnknownRuleType() {
            AnomalyRule rule = new AnomalyRule();
            rule.setEnabled(true);
            // 通过反射设置一个不存在的规则类型，或者直接测试默认分支
            // 这里我们测试 null 情况，因为 RuleType 是枚举
            rule.setRuleType(null);
            assertFalse(algorithm.checkAnomaly(50.0, rule));
        }
    }

    @Nested
    @DisplayName("calculateRateOfChange 方法测试")
    class CalculateRateOfChangeTests {

        @Test
        @DisplayName("正常计算变化率")
        void shouldCalculateRateOfChangeCorrectly() {
            Double result = algorithm.calculateRateOfChange(100.0, 80.0, 2);
            assertEquals(10.0, result);
        }

        @Test
        @DisplayName("变化率为负数时应正确计算")
        void shouldCalculateNegativeRateOfChange() {
            Double result = algorithm.calculateRateOfChange(80.0, 100.0, 2);
            assertEquals(-10.0, result);
        }

        @Test
        @DisplayName("当当前值为 null 时应返回 null")
        void shouldReturnNullWhenCurrentValueIsNull() {
            assertNull(algorithm.calculateRateOfChange(null, 80.0, 2));
        }

        @Test
        @DisplayName("当历史值为 null 时应返回 null")
        void shouldReturnNullWhenPreviousValueIsNull() {
            assertNull(algorithm.calculateRateOfChange(100.0, null, 2));
        }

        @Test
        @DisplayName("当时间间隔为 0 时应返回 null")
        void shouldReturnNullWhenTimeIntervalIsZero() {
            assertNull(algorithm.calculateRateOfChange(100.0, 80.0, 0));
        }

        @Test
        @DisplayName("当时间间隔为负数时应返回 null")
        void shouldReturnNullWhenTimeIntervalIsNegative() {
            assertNull(algorithm.calculateRateOfChange(100.0, 80.0, -1));
        }
    }

    @Nested
    @DisplayName("checkRateOfChangeAnomaly 方法测试")
    class CheckRateOfChangeAnomalyTests {

        @Test
        @DisplayName("变化率超过阈值时应返回 true")
        void shouldReturnTrueWhenRateExceedsThreshold() {
            assertTrue(algorithm.checkRateOfChangeAnomaly(15.0, 10.0));
            assertTrue(algorithm.checkRateOfChangeAnomaly(-15.0, 10.0));
        }

        @Test
        @DisplayName("变化率等于阈值时应返回 false")
        void shouldReturnFalseWhenRateEqualsThreshold() {
            assertFalse(algorithm.checkRateOfChangeAnomaly(10.0, 10.0));
            assertFalse(algorithm.checkRateOfChangeAnomaly(-10.0, 10.0));
        }

        @Test
        @DisplayName("变化率低于阈值时应返回 false")
        void shouldReturnFalseWhenRateBelowThreshold() {
            assertFalse(algorithm.checkRateOfChangeAnomaly(5.0, 10.0));
            assertFalse(algorithm.checkRateOfChangeAnomaly(-5.0, 10.0));
        }

        @Test
        @DisplayName("当变化率为 null 时应返回 false")
        void shouldReturnFalseWhenRateOfChangeIsNull() {
            assertFalse(algorithm.checkRateOfChangeAnomaly(null, 10.0));
        }

        @Test
        @DisplayName("当阈值为 null 时应返回 false")
        void shouldReturnFalseWhenThresholdIsNull() {
            assertFalse(algorithm.checkRateOfChangeAnomaly(15.0, null));
        }
    }

    @Nested
    @DisplayName("checkStatisticalAnomaly 方法测试")
    class CheckStatisticalAnomalyTests {

        @Test
        @DisplayName("值超过 3-sigma 时应返回 true")
        void shouldReturnTrueWhenValueExceedsThreeSigma() {
            // 平均值=100, 标准差=10, 值=131 (z-score = 3.1 > 3)
            assertTrue(algorithm.checkStatisticalAnomaly(131.0, 100.0, 10.0, 3.0));
        }

        @Test
        @DisplayName("值在 3-sigma 范围内时应返回 false")
        void shouldReturnFalseWhenValueWithinThreeSigma() {
            // 平均值=100, 标准差=10, 值=125 (z-score = 2.5 < 3)
            assertFalse(algorithm.checkStatisticalAnomaly(125.0, 100.0, 10.0, 3.0));
        }

        @Test
        @DisplayName("自定义 sigma 倍数应正确判断")
        void shouldUseCustomSigmaMultiplier() {
            // 使用 2-sigma
            assertTrue(algorithm.checkStatisticalAnomaly(121.0, 100.0, 10.0, 2.0));
            assertFalse(algorithm.checkStatisticalAnomaly(119.0, 100.0, 10.0, 2.0));
        }

        @Test
        @DisplayName("当值为 null 时应返回 false")
        void shouldReturnFalseWhenValueIsNull() {
            assertFalse(algorithm.checkStatisticalAnomaly(null, 100.0, 10.0, 3.0));
        }

        @Test
        @DisplayName("当平均值为 null 时应返回 false")
        void shouldReturnFalseWhenMeanIsNull() {
            assertFalse(algorithm.checkStatisticalAnomaly(131.0, null, 10.0, 3.0));
        }

        @Test
        @DisplayName("当标准差为 null 时应返回 false")
        void shouldReturnFalseWhenStdDevIsNull() {
            assertFalse(algorithm.checkStatisticalAnomaly(131.0, 100.0, null, 3.0));
        }

        @Test
        @DisplayName("当标准差为 0 时应返回 false")
        void shouldReturnFalseWhenStdDevIsZero() {
            assertFalse(algorithm.checkStatisticalAnomaly(131.0, 100.0, 0.0, 3.0));
        }

        @Test
        @DisplayName("当标准差为负数时应返回 false")
        void shouldReturnFalseWhenStdDevIsNegative() {
            assertFalse(algorithm.checkStatisticalAnomaly(131.0, 100.0, -10.0, 3.0));
        }

        @Test
        @DisplayName("低于平均值的异常也应检测到")
        void shouldDetectAnomalyBelowMean() {
            // 值=69 (z-score = 3.1 > 3)
            assertTrue(algorithm.checkStatisticalAnomaly(69.0, 100.0, 10.0, 3.0));
        }
    }

    // 辅助方法：创建阈值规则
    private AnomalyRule createRule(AnomalyRule.RuleType type, Double threshold) {
        AnomalyRule rule = new AnomalyRule();
        rule.setRuleType(type);
        rule.setThresholdValue(threshold);
        rule.setEnabled(true);
        return rule;
    }

    // 辅助方法：创建范围规则
    private AnomalyRule createRangeRule(Double minValue, Double maxValue) {
        AnomalyRule rule = new AnomalyRule();
        rule.setRuleType(AnomalyRule.RuleType.RANGE);
        rule.setMinValue(minValue);
        rule.setMaxValue(maxValue);
        rule.setEnabled(true);
        return rule;
    }

    // 辅助方法：创建表达式规则
    private AnomalyRule createExpressionRule(String expression) {
        AnomalyRule rule = new AnomalyRule();
        rule.setRuleType(AnomalyRule.RuleType.CUSTOM_EXPRESSION);
        rule.setExpression(expression);
        rule.setEnabled(true);
        return rule;
    }
}
