package com.weather.scheduler;

import com.weather.model.AlertRule;
import com.weather.model.PhotoSpot;
import com.weather.repository.AlertRuleRepository;
import com.weather.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertSchedulerTest {

    private AlertScheduler alertScheduler;
    private AlertRuleRepository alertRuleRepository;
    private AlertService alertService;

    private static final GeometryFactory GF = new GeometryFactory();

    @BeforeEach
    void setUp() {
        alertRuleRepository = mock(AlertRuleRepository.class);
        alertService = mock(AlertService.class);
        alertScheduler = new AlertScheduler(alertRuleRepository, alertService);
    }

    @Test
    @DisplayName("evaluateAlerts processes all enabled rules")
    void evaluateAlerts_enabledRules_evaluatesEach() {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule1 = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        var rule2 = new AlertRule(spot, "sunrise", "{}", LocalTime.of(6, 0));
        when(alertRuleRepository.findAllEnabledWithSpot()).thenReturn(List.of(rule1, rule2));

        alertScheduler.evaluateAlerts();

        verify(alertService).evaluateAndRecord(rule1);
        verify(alertService).evaluateAndRecord(rule2);
    }

    @Test
    @DisplayName("evaluateAlerts handles empty rules list")
    void evaluateAlerts_emptyList_doesNothing() {
        when(alertRuleRepository.findAllEnabledWithSpot()).thenReturn(Collections.emptyList());

        alertScheduler.evaluateAlerts();

        verify(alertService, never()).evaluateAndRecord(any());
    }

    @Test
    @DisplayName("evaluateAlerts isolates errors per rule")
    void evaluateAlerts_ruleThrows_continuesNextRule() {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule1 = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        var rule2 = new AlertRule(spot, "sunrise", "{}", LocalTime.of(6, 0));
        when(alertRuleRepository.findAllEnabledWithSpot()).thenReturn(List.of(rule1, rule2));
        when(alertService.evaluateAndRecord(rule1)).thenThrow(new RuntimeException("API error"));
        when(alertService.evaluateAndRecord(rule2)).thenReturn(null);

        alertScheduler.evaluateAlerts();

        verify(alertService).evaluateAndRecord(rule1);
        verify(alertService).evaluateAndRecord(rule2);
    }

    @Test
    @DisplayName("evaluateAlerts counts triggered alerts")
    void evaluateAlerts_triggeredAlerts_evaluatesAll() {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule1 = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        var rule2 = new AlertRule(spot, "sunrise", "{}", LocalTime.of(6, 0));
        var rule3 = new AlertRule(spot, "milky_way", "{}", LocalTime.of(2, 0));
        when(alertRuleRepository.findAllEnabledWithSpot()).thenReturn(List.of(rule1, rule2, rule3));

        alertScheduler.evaluateAlerts();

        verify(alertService).evaluateAndRecord(rule1);
        verify(alertService).evaluateAndRecord(rule2);
        verify(alertService).evaluateAndRecord(rule3);
    }
}
