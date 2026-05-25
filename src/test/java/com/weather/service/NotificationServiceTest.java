package com.weather.service;

import com.weather.model.AlertRule;
import com.weather.model.PhotoSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

    private NotificationService notificationService;
    private static final GeometryFactory GF = new GeometryFactory();

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    @DisplayName("buildPayload returns JSON with spot name and condition")
    void buildPayload_returnsJsonWithDetails() {
        var spot = new PhotoSpot("user", "Tower View", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunrise", "{\"glow_probability\":60}", LocalTime.of(20, 0));

        String payload = notificationService.buildPayload(rule, spot, 78,
                "明日可能有高质量朝霞（概率 78%），日出 05:23，建议 04:30 到达");

        assertNotNull(payload);
        assertTrue(payload.contains("Tower View"));
        assertTrue(payload.contains("sunrise"));
        assertTrue(payload.contains("78"));
    }

    @Test
    @DisplayName("buildPayload handles null fields gracefully")
    void buildPayload_nullFields_doesNotThrow() {
        var spot = new PhotoSpot("user", null, GF.createPoint(new Coordinate(0.0, 0.0)), null);
        var rule = new AlertRule(spot, null, "{}", null);

        assertDoesNotThrow(() -> notificationService.buildPayload(rule, spot, 50, null));
    }
}
