package com.weather.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AlertHistoryTest {

    @Test
    @DisplayName("constructor sets ruleId, spotId, and score")
    void constructor_setsFields() {
        var history = new AlertHistory(1L, 2L, 85);

        assertEquals(1L, history.getRuleId());
        assertEquals(2L, history.getSpotId());
        assertEquals(85, history.getScore());
        assertNotNull(history.getTriggeredAt());
        assertFalse(history.isSent());
        assertNull(history.getWeatherSnapshotId());
    }

    @Test
    @DisplayName("no-args constructor and setters work")
    void noArgsConstructor_andSetters() {
        var history = new AlertHistory();
        var now = Instant.now();

        history.setId(10L);
        history.setRuleId(5L);
        history.setSpotId(3L);
        history.setScore(60);
        history.setSent(true);
        history.setTriggeredAt(now);
        history.setWeatherSnapshotId(7L);

        assertEquals(10L, history.getId());
        assertEquals(5L, history.getRuleId());
        assertEquals(3L, history.getSpotId());
        assertEquals(60, history.getScore());
        assertEquals(true, history.isSent());
        assertEquals(now, history.getTriggeredAt());
        assertEquals(7L, history.getWeatherSnapshotId());
    }
}
