package com.weather.service;

import com.weather.dto.AlertRuleRequest;
import com.weather.exception.SpotNotFoundException;
import com.weather.model.AlertRule;
import com.weather.model.PhotoSpot;
import com.weather.repository.AlertRuleRepository;
import com.weather.repository.PhotoSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertServiceTest {

    private AlertService alertService;
    private AlertRuleRepository alertRuleRepository;
    private PhotoSpotRepository spotRepository;

    private static final GeometryFactory GF = new GeometryFactory();

    @BeforeEach
    void setUp() {
        alertRuleRepository = mock(AlertRuleRepository.class);
        spotRepository = mock(PhotoSpotRepository.class);
        alertService = new AlertService(alertRuleRepository, spotRepository);
    }

    @Test
    @DisplayName("create saves rule when spot exists")
    void create_validSpot_savesRule() {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        var saved = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        when(alertRuleRepository.save(any())).thenReturn(saved);

        var request = new AlertRuleRequest("sunset", 60, null, null, null, null, null, "12:00");
        var result = alertService.create(1L, request);

        assertEquals("sunset", result.getAlertType());
        verify(spotRepository).findById(1L);
        verify(alertRuleRepository).save(any());
    }

    @Test
    @DisplayName("create throws SpotNotFoundException when spot does not exist")
    void create_missingSpot_throws() {
        when(spotRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new AlertRuleRequest("sunset", 60, null, null, null, null, null, "12:00");
        assertThrows(SpotNotFoundException.class, () -> alertService.create(99L, request));
    }

    @Test
    @DisplayName("listBySpot returns rules for spot")
    void listBySpot_returnsList() {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        when(alertRuleRepository.findBySpotIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0))));

        var result = alertService.listBySpot(1L);
        assertEquals(1, result.size());
        assertEquals("sunset", result.get(0).getAlertType());
    }

    @Test
    @DisplayName("update modifies and saves existing rule")
    void update_existing_updatesFields() {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var existing = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        when(alertRuleRepository.findById(1L)).thenReturn(Optional.of(existing));

        var updated = new AlertRule(spot, "sunrise", "{\"glow_probability\":70}", LocalTime.of(6, 0));
        when(alertRuleRepository.save(any())).thenReturn(updated);

        var request = new AlertRuleRequest("sunrise", 70, null, null, null, null, null, "06:00");
        var result = alertService.update(1L, request);

        assertEquals("sunrise", result.getAlertType());
        verify(alertRuleRepository).save(any());
    }

    @Test
    @DisplayName("update throws when rule not found")
    void update_missing_throws() {
        when(alertRuleRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new AlertRuleRequest("sunrise", 70, null, null, null, null, null, "06:00");
        assertThrows(SpotNotFoundException.class, () -> alertService.update(99L, request));
    }

    @Test
    @DisplayName("delete removes rule when exists")
    void delete_existing_removes() {
        when(alertRuleRepository.existsById(1L)).thenReturn(true);

        alertService.delete(1L);
        verify(alertRuleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete throws when rule not found")
    void delete_missing_throws() {
        when(alertRuleRepository.existsById(99L)).thenReturn(false);

        assertThrows(SpotNotFoundException.class, () -> alertService.delete(99L));
    }

    @Test
    @DisplayName("getById returns rule when exists")
    void getById_existing_returnsRule() {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunset", "{}", LocalTime.of(12, 0));
        when(alertRuleRepository.findById(1L)).thenReturn(Optional.of(rule));

        var result = alertService.getById(1L);
        assertNotNull(result);
        assertEquals("sunset", result.getAlertType());
    }

    @Test
    @DisplayName("getById throws when rule not found")
    void getById_missing_throws() {
        when(alertRuleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SpotNotFoundException.class, () -> alertService.getById(99L));
    }
}
