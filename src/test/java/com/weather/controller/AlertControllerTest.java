package com.weather.controller;

import com.weather.dto.AlertRuleRequest;
import com.weather.exception.SpotNotFoundException;
import com.weather.model.AlertHistory;
import com.weather.model.AlertRule;
import com.weather.model.PhotoSpot;
import com.weather.repository.AlertHistoryRepository;
import com.weather.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlertControllerTest {

    private MockMvc mockMvc;
    private AlertService alertService;
    private AlertHistoryRepository alertHistoryRepository;

    private static final GeometryFactory GF = new GeometryFactory();

    @BeforeEach
    void setUp() {
        alertService = mock(AlertService.class);
        alertHistoryRepository = mock(AlertHistoryRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AlertController(alertService, alertHistoryRepository))
                .setControllerAdvice(new com.weather.advice.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/spots/{spotId}/alerts returns alert list")
    void listAlerts_returnsList() throws Exception {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));

        when(alertService.listBySpot(1L)).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/spots/1/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].alertType").value("sunset"));
    }

    @Test
    @DisplayName("POST /api/spots/{spotId}/alerts creates alert and returns 201")
    void createAlert_valid_returns201() throws Exception {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));

        when(alertService.create(eq(1L), any(AlertRuleRequest.class))).thenReturn(rule);

        mockMvc.perform(post("/api/spots/1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alertType":"sunset","glowProbability":60,"pushTime":"12:00"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.alertType").value("sunset"));
    }

    @Test
    @DisplayName("POST /api/spots/{spotId}/alerts with invalid data returns 400")
    void createAlert_invalid_returns400() throws Exception {
        mockMvc.perform(post("/api/spots/1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alertType":"","glowProbability":150,"pushTime":""}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/alerts/{id} updates alert")
    void updateAlert_valid_returnsUpdated() throws Exception {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunrise", "{}", LocalTime.of(20, 0));

        when(alertService.update(eq(1L), any(AlertRuleRequest.class))).thenReturn(rule);

        mockMvc.perform(put("/api/alerts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alertType":"sunrise","glowProbability":70,"pushTime":"20:00"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alertType").value("sunrise"));
    }

    @Test
    @DisplayName("DELETE /api/alerts/{id} returns 204")
    void deleteAlert_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/alerts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/alerts/{id} returns 404 for unknown alert")
    void getAlert_notFound_returns404() throws Exception {
        when(alertService.getById(99L)).thenThrow(new SpotNotFoundException(99L));

        mockMvc.perform(get("/api/alerts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/alerts/{id}/history returns trigger history")
    void listHistory_returnsHistory() throws Exception {
        var history1 = new AlertHistory(1L, 1L, 80);
        var history2 = new AlertHistory(1L, 1L, 85);
        when(alertHistoryRepository.findByRuleIdOrderByTriggeredAtDesc(1L))
                .thenReturn(List.of(history2, history1));

        mockMvc.perform(get("/api/alerts/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/alerts/{id}/history returns empty list when no history")
    void listHistory_empty_returnsEmptyList() throws Exception {
        when(alertHistoryRepository.findByRuleIdOrderByTriggeredAtDesc(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/alerts/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/alerts/{id}/test triggers evaluation and returns result")
    void testTrigger_valid_returnsResult() throws Exception {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        rule.setId(1L);
        var history = new AlertHistory(1L, 1L, 85);

        when(alertService.getById(1L)).thenReturn(rule);
        when(alertService.evaluateAndRecord(rule)).thenReturn(history);

        mockMvc.perform(post("/api/alerts/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(85));
    }

    @Test
    @DisplayName("POST /api/alerts/{id}/test returns null data when not triggered")
    void testTrigger_notTriggered_returnsNullData() throws Exception {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        rule.setId(1L);

        when(alertService.getById(1L)).thenReturn(rule);
        when(alertService.evaluateAndRecord(rule)).thenReturn(null);

        mockMvc.perform(post("/api/alerts/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/spots/{spotId}/alerts with recipientEmail creates alert")
    void createAlert_withEmail_returns201() throws Exception {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        when(alertService.create(eq(1L), any(AlertRuleRequest.class))).thenReturn(rule);

        mockMvc.perform(post("/api/spots/1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alertType":"sunset","glowProbability":60,"pushTime":"12:00","recipientEmail":"user@example.com"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/spots/{spotId}/alerts with invalid email returns 400")
    void createAlert_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/spots/1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alertType":"sunset","glowProbability":60,"pushTime":"12:00","recipientEmail":"not-an-email"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/alerts/{id}/test returns 404 for unknown alert")
    void testTrigger_notFound_returns404() throws Exception {
        when(alertService.getById(99L)).thenThrow(new SpotNotFoundException(99L));

        mockMvc.perform(post("/api/alerts/99/test"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
