package com.weather.controller;

import com.weather.dto.AlertRuleRequest;
import com.weather.dto.ApiResponse;
import com.weather.model.AlertHistory;
import com.weather.model.AlertRule;
import com.weather.repository.AlertHistoryRepository;
import com.weather.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AlertController {

    private final AlertService alertService;
    private final AlertHistoryRepository alertHistoryRepository;

    public AlertController(AlertService alertService,
                          AlertHistoryRepository alertHistoryRepository) {
        this.alertService = alertService;
        this.alertHistoryRepository = alertHistoryRepository;
    }

    @GetMapping("/spots/{spotId}/alerts")
    public ApiResponse<List<AlertRule>> listAlerts(@PathVariable Long spotId) {
        return ApiResponse.ok(alertService.listBySpot(spotId));
    }

    @PostMapping("/spots/{spotId}/alerts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AlertRule> createAlert(@PathVariable Long spotId,
                                               @Valid @RequestBody AlertRuleRequest request) {
        return ApiResponse.ok(alertService.create(spotId, request));
    }

    @GetMapping("/alerts/{id}")
    public ApiResponse<AlertRule> getAlert(@PathVariable Long id) {
        return ApiResponse.ok(alertService.getById(id));
    }

    @PutMapping("/alerts/{id}")
    public ApiResponse<AlertRule> updateAlert(@PathVariable Long id,
                                               @Valid @RequestBody AlertRuleRequest request) {
        return ApiResponse.ok(alertService.update(id, request));
    }

    @DeleteMapping("/alerts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlert(@PathVariable Long id) {
        alertService.delete(id);
    }

    @GetMapping("/alerts/{id}/history")
    public ApiResponse<List<AlertHistory>> listHistory(@PathVariable Long id) {
        return ApiResponse.ok(alertHistoryRepository.findByRuleIdOrderByTriggeredAtDesc(id));
    }

    @PostMapping("/alerts/{id}/test")
    public ApiResponse<AlertHistory> testTrigger(@PathVariable Long id) {
        var rule = alertService.getById(id);
        var result = alertService.evaluateAndRecord(rule);
        return result != null ? ApiResponse.ok(result)
                : ApiResponse.ok(null);
    }
}
