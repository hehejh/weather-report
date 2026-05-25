package com.weather.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WeatherSnapshotTest {

    private static final GeometryFactory GF = new GeometryFactory();

    @Test
    @DisplayName("constructor sets fields correctly")
    void constructor_setsFields() {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        var forecastFor = Instant.now();
        var snapshot = new WeatherSnapshot(spot, forecastFor, "{}", "QWeather");

        assertEquals(spot, snapshot.getSpot());
        assertEquals(forecastFor, snapshot.getForecastFor());
        assertEquals("{}", snapshot.getData());
        assertEquals("QWeather", snapshot.getSource());
        assertNotNull(snapshot.getFetchedAt());
    }

    @Test
    @DisplayName("setters update fields correctly")
    void setters_updateFields() {
        var snapshot = new WeatherSnapshot();
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        var now = Instant.now();

        snapshot.setId(1L);
        snapshot.setSpot(spot);
        snapshot.setData("{\"test\":true}");
        snapshot.setSource("TestSource");
        snapshot.setFetchedAt(now);
        snapshot.setForecastFor(now);

        assertEquals(1L, snapshot.getId());
        assertEquals(spot, snapshot.getSpot());
        assertEquals("{\"test\":true}", snapshot.getData());
        assertEquals("TestSource", snapshot.getSource());
        assertEquals(now, snapshot.getFetchedAt());
        assertEquals(now, snapshot.getForecastFor());
    }
}
