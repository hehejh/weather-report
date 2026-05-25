package com.weather.service;

import com.weather.dto.QWeatherResponses.AirNow;
import com.weather.dto.QWeatherResponses.DailyWeather;
import com.weather.dto.QWeatherResponses.NowWeather;
import com.weather.dto.WeatherDashboard;
import com.weather.dto.WeatherDashboard.GlowForecast;
import com.weather.dto.WeatherDashboard.SolarTimes;
import com.weather.model.PhotoSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeatherServiceTest {

    private WeatherService weatherService;
    private QWeatherClient qWeatherClient;
    private PhotographyIndexCalculator indexCalculator;
    private SunCalcService sunCalcService;

    private static final GeometryFactory GF = new GeometryFactory();

    @BeforeEach
    void setUp() {
        qWeatherClient = mock(QWeatherClient.class);
        indexCalculator = mock(PhotographyIndexCalculator.class);
        sunCalcService = mock(SunCalcService.class);
        weatherService = new WeatherService(qWeatherClient, indexCalculator, sunCalcService);
    }

    @Test
    @DisplayName("getDashboard returns complete dashboard with all sections")
    void getDashboard_returnsCompleteDashboard() {
        var spot = new PhotoSpot("user", "Test Spot", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);

        when(qWeatherClient.getNowWeather("116.40,39.90"))
                .thenReturn(new NowWeather("2025-01-01T12:00", "22.5", "20.0", "100", "Sunny",
                        "180", "S", "3", "15.0", "55", "0.0", "1013", "10", "45", "10"));
        when(qWeatherClient.get7DayForecast("116.40,39.90"))
                .thenReturn(List.of(
                        new DailyWeather("2025-01-01", "07:30", "17:00", null, null, null,
                                "25", "15", "100", "Sunny", "150", "Cloudy",
                                "180", "S", "3", "12.0", "90", "N", "2", "8.0",
                                "50", "10", "1010", "10", "40", "5")));
        when(qWeatherClient.getAirNow("116.40,39.90"))
                .thenReturn(new AirNow("2025-01-01T12:00", "45", "2", "Good", "PM10",
                        "30", "20", "10", "5", "0.5", "60"));
        when(sunCalcService.calculateSolarTimes(any(), anyDouble(), anyDouble(), any()))
                .thenReturn(new SolarTimes(Instant.now(), Instant.now().plusSeconds(36000),
                        null, null, null, null));
        when(indexCalculator.computePhotographyIndex(anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble())).thenReturn(78);
        when(indexCalculator.forecastGlow(anyString(), anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble()))
                .thenReturn(new GlowForecast("sunset", 65, "good", null));

        var result = weatherService.getDashboard(spot);

        assertEquals(1L, result.spotId());
        assertEquals("Test Spot", result.spotName());
        assertEquals(78, result.photographyIndex());
        assertEquals("good", result.indexLabel());
        assertNotNull(result.current());
        assertEquals(22.5, result.current().temperature());
        assertEquals(20.0, result.current().feelsLike());
        assertEquals(55, result.current().humidity());
        assertEquals(15.0, result.current().windSpeed());
        assertEquals("S", result.current().windDirection());
        assertEquals(10, result.current().visibility());
        assertEquals(45, result.current().aqi());
        assertEquals(45, result.current().totalCloud());
        assertEquals(0.0, result.current().precipitationProbability());
        assertNotNull(result.solar());
        assertNotNull(result.glow());
        assertEquals(1, result.weekForecast().size());
    }

    @Test
    @DisplayName("getDashboard handles null API responses gracefully")
    void getDashboard_nullApiResponses_usesDefaults() {
        var spot = new PhotoSpot("user", "Spot", GF.createPoint(new Coordinate(0.0, 0.0)), null);
        spot.setId(2L);

        when(qWeatherClient.getNowWeather(anyString())).thenReturn(null);
        when(qWeatherClient.get7DayForecast(anyString())).thenReturn(null);
        when(qWeatherClient.getAirNow(anyString())).thenReturn(null);
        when(sunCalcService.calculateSolarTimes(any(), anyDouble(), anyDouble(), any()))
                .thenReturn(new SolarTimes(null, null, null, null, null, null));
        when(indexCalculator.computePhotographyIndex(anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble())).thenReturn(30);
        when(indexCalculator.forecastGlow(anyString(), anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble()))
                .thenReturn(new GlowForecast("sunset", 20, "poor", null));

        var result = weatherService.getDashboard(spot);

        assertEquals("poor", result.indexLabel());
        assertEquals(0.0, result.current().temperature());
        assertEquals(0, result.current().humidity());
        assertTrue(result.weekForecast().isEmpty());
    }

    @Test
    @DisplayName("getDashboard returns excellent label for index >= 80")
    void getDashboard_highIndex_excellentLabel() {
        var spot = new PhotoSpot("user", "Spot", GF.createPoint(new Coordinate(120.0, 30.0)), null);
        spot.setId(3L);

        when(qWeatherClient.getNowWeather(anyString())).thenReturn(null);
        when(qWeatherClient.get7DayForecast(anyString())).thenReturn(List.of());
        when(qWeatherClient.getAirNow(anyString())).thenReturn(null);
        when(sunCalcService.calculateSolarTimes(any(), anyDouble(), anyDouble(), any()))
                .thenReturn(new SolarTimes(null, null, null, null, null, null));
        when(indexCalculator.computePhotographyIndex(anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble())).thenReturn(85);
        when(indexCalculator.forecastGlow(anyString(), anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble()))
                .thenReturn(new GlowForecast("sunset", 85, "excellent", null));

        var result = weatherService.getDashboard(spot);

        assertEquals("excellent", result.indexLabel());
        assertEquals(85, result.photographyIndex());
    }

    @Test
    @DisplayName("getDashboard returns fair label for index 40-59")
    void getDashboard_midIndex_fairLabel() {
        var spot = new PhotoSpot("user", "Spot", GF.createPoint(new Coordinate(120.0, 30.0)), null);
        spot.setId(4L);

        when(qWeatherClient.getNowWeather(anyString())).thenReturn(null);
        when(qWeatherClient.get7DayForecast(anyString())).thenReturn(List.of());
        when(qWeatherClient.getAirNow(anyString())).thenReturn(null);
        when(sunCalcService.calculateSolarTimes(any(), anyDouble(), anyDouble(), any()))
                .thenReturn(new SolarTimes(null, null, null, null, null, null));
        when(indexCalculator.computePhotographyIndex(anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble())).thenReturn(50);
        when(indexCalculator.forecastGlow(anyString(), anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble()))
                .thenReturn(new GlowForecast("sunset", 50, "fair", null));

        var result = weatherService.getDashboard(spot);

        assertEquals("fair", result.indexLabel());
    }

    @Test
    @DisplayName("getDashboard handles daily forecast with null fields")
    void getDashboard_dailyNullFields_defaultsToZero() {
        var spot = new PhotoSpot("user", "Spot", GF.createPoint(new Coordinate(120.0, 30.0)), null);
        spot.setId(5L);

        when(qWeatherClient.getNowWeather(anyString())).thenReturn(null);
        when(qWeatherClient.get7DayForecast(anyString())).thenReturn(List.of(
                new DailyWeather("2025-06-01", null, null, null, null, null,
                        null, null, "100", "Sunny", null, null,
                        null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null)));
        when(qWeatherClient.getAirNow(anyString())).thenReturn(null);
        when(sunCalcService.calculateSolarTimes(any(), anyDouble(), anyDouble(), any()))
                .thenReturn(new SolarTimes(null, null, null, null, null, null));
        when(indexCalculator.computePhotographyIndex(anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble())).thenReturn(60);
        when(indexCalculator.forecastGlow(anyString(), anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyDouble(), anyDouble()))
                .thenReturn(new GlowForecast("sunset", 60, "good", null));

        var result = weatherService.getDashboard(spot);

        assertEquals(1, result.weekForecast().size());
        var day = result.weekForecast().get(0);
        assertEquals("100", day.weatherIcon());
        assertEquals(0, day.tempMax());
        assertEquals(0, day.precipitationProbability());
    }
}
