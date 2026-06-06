package com.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 参数监测平台主启动类
 * 支持IoTDB、OPC UA、MySQL等数据源接入
 * 具备异常规则配置和趋势预测功能
 */
@SpringBootApplication
@EnableScheduling
public class ParameterMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParameterMonitorApplication.class, args);
    }
}
