package com.monitor.controller;

import com.monitor.dto.DashboardDTO;
import com.monitor.dto.ParameterDataDTO;
import com.monitor.model.*;
import com.monitor.service.DataSourceService;
import com.monitor.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 监控数据控制器
 */
@RestController
@RequestMapping("/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {
    
    @Autowired
    private MonitoringService monitoringService;
    
    @Autowired
    private DataSourceService dataSourceService;
    
    // ==================== 数据源管理 ====================
    
    @GetMapping("/datasources")
    public ResponseEntity<List<DataSource>> getDataSources() {
        return ResponseEntity.ok(dataSourceService.getAllDataSources());
    }
    
    @GetMapping("/datasources/{id}")
    public ResponseEntity<DataSource> getDataSource(@PathVariable Long id) {
        return dataSourceService.getDataSourceById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/datasources")
    public ResponseEntity<DataSource> createDataSource(@RequestBody DataSource dataSource) {
        try {
            DataSource created = dataSourceService.createDataSource(dataSource);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/datasources/{id}")
    public ResponseEntity<DataSource> updateDataSource(
            @PathVariable Long id, 
            @RequestBody DataSource dataSource) {
        try {
            DataSource updated = dataSourceService.updateDataSource(id, dataSource);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/datasources/{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/datasources/{id}/test")
    public ResponseEntity<Map<String, Boolean>> testDataSource(@PathVariable Long id) {
        Map<String, Boolean> result = new HashMap<>();
        Optional<DataSource> dataSource = dataSourceService.getDataSourceById(id);
        
        if (dataSource.isPresent()) {
            boolean success = dataSourceService.testConnection(dataSource.get());
            result.put("success", success);
            return ResponseEntity.ok(result);
        }
        
        return ResponseEntity.notFound().build();
    }
    
    // ==================== 监测参数管理 ====================
    
    @GetMapping("/parameters")
    public ResponseEntity<List<MonitorParameter>> getParameters() {
        return ResponseEntity.ok(monitoringService.getAllParameters());
    }
    
    @GetMapping("/parameters/{id}")
    public ResponseEntity<MonitorParameter> getParameter(@PathVariable Long id) {
        return monitoringService.getAllParameters().stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/parameters")
    public ResponseEntity<MonitorParameter> createParameter(@RequestBody MonitorParameter parameter) {
        try {
            MonitorParameter created = monitoringService.createParameter(parameter);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/parameters/{id}")
    public ResponseEntity<MonitorParameter> updateParameter(
            @PathVariable Long id, 
            @RequestBody MonitorParameter parameter) {
        try {
            MonitorParameter updated = monitoringService.updateParameter(id, parameter);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/parameters/{id}")
    public ResponseEntity<Void> deleteParameter(@PathVariable Long id) {
        monitoringService.deleteParameter(id);
        return ResponseEntity.ok().build();
    }
    
    // ==================== 历史数据查询 ====================
    
    @GetMapping("/data/{parameterId}")
    public ResponseEntity<List<ParameterData>> getData(
            @PathVariable Long parameterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<ParameterData> data = monitoringService.queryHistory(parameterId, startTime, endTime);
        return ResponseEntity.ok(data);
    }
    
    @GetMapping("/data/{parameterId}/latest")
    public ResponseEntity<ParameterData> getLatestData(@PathVariable Long parameterId) {
        return monitoringService.getLatestData(parameterId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/data/{parameterId}/statistics")
    public ResponseEntity<ParameterDataDTO.Statistics> getStatistics(
            @PathVariable Long parameterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        ParameterDataDTO.Statistics stats = monitoringService.getStatistics(parameterId, startTime, endTime);
        return ResponseEntity.ok(stats);
    }
    
    // ==================== 异常规则管理 ====================
    
    @GetMapping("/rules/parameter/{parameterId}")
    public ResponseEntity<List<AnomalyRule>> getRulesByParameter(@PathVariable Long parameterId) {
        return ResponseEntity.ok(monitoringService.getRulesByParameter(parameterId));
    }
    
    @PostMapping("/rules")
    public ResponseEntity<AnomalyRule> createRule(@RequestBody AnomalyRule rule) {
        AnomalyRule created = monitoringService.createRule(rule);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/rules/{id}")
    public ResponseEntity<AnomalyRule> updateRule(
            @PathVariable Long id, 
            @RequestBody AnomalyRule rule) {
        try {
            AnomalyRule updated = monitoringService.updateRule(id, rule);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        monitoringService.deleteRule(id);
        return ResponseEntity.ok().build();
    }
    
    // ==================== 告警管理 ====================
    
    @GetMapping("/alarms")
    public ResponseEntity<List<AlarmRecord>> getAlarms(
            @RequestParam(required = false) AlarmRecord.AlarmStatus status) {
        return ResponseEntity.ok(monitoringService.getAlarms(status));
    }
    
    @PostMapping("/alarms/{id}/handle")
    public ResponseEntity<Void> handleAlarm(
            @PathVariable Long id,
            @RequestParam String handledBy,
            @RequestParam(required = false) String comment) {
        monitoringService.handleAlarm(id, handledBy, comment);
        return ResponseEntity.ok().build();
    }
    
    // ==================== 趋势预测 ====================
    
    @GetMapping("/prediction/{parameterId}")
    public ResponseEntity<List<Double>> getPrediction(
            @PathVariable Long parameterId,
            @RequestParam(defaultValue = "10") int steps) {
        List<Double> prediction = monitoringService.getPrediction(parameterId, steps);
        return ResponseEntity.ok(prediction);
    }
    
    // ==================== 驾驶舱数据 ====================
    
    @GetMapping("/dashboard/overview")
    public ResponseEntity<DashboardDTO.Overview> getDashboardOverview() {
        DashboardDTO.Overview overview = monitoringService.getDashboardOverview();
        return ResponseEntity.ok(overview);
    }
}
