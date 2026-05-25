package com.weather.controller;

import com.weather.dto.WeatherDashboard;
import com.weather.dto.WeatherDashboard.CurrentWeather;
import com.weather.dto.WeatherDashboard.GlowForecast;
import com.weather.dto.WeatherDashboard.SolarTimes;
import com.weather.exception.SpotNotFoundException;
import com.weather.model.PhotoSpot;
import com.weather.repository.PhotoSpotRepository;
import com.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WeatherControllerTest {

    private MockMvc mockMvc;
    private WeatherService weatherService;
    private PhotoSpotRepository spotRepository;

    private static final GeometryFactory GF = new GeometryFactory();
    private static final String BASE = "/api/spots/1/weather";

    @BeforeEach
    void setUp() {
        weatherService = mock(WeatherService.class);
        spotRepository = mock(PhotoSpotRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new WeatherController(weatherService, spotRepository))
                .setControllerAdvice(new com.weather.advice.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/spots/{id}/weather returns dashboard")
    void getDashboard_returnsDashboard() throws Exception {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        var dashboard = new WeatherDashboard(1L, "Test", 78, "good",
                new CurrentWeather(22.0, 20.0, 55, 5.0, "N", 10, 45, 30, 10.0),
                new SolarTimes(null, null, null, null, null, null),
                new GlowForecast("sunset", 65, "good", null),
                List.of());
        when(weatherService.getDashboard(any())).thenReturn(dashboard);

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.spotId").value(1))
                .andExpect(jsonPath("$.data.photographyIndex").value(78))
                .andExpect(jsonPath("$.data.indexLabel").value("good"))
                .andExpect(jsonPath("$.data.current.temperature").value(22.0));
    }

    @Test
    @DisplayName("GET /api/spots/{id}/weather returns 404 for unknown spot")
    void getDashboard_unknownSpot_returns404() throws Exception {
        when(spotRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/spots/99/weather"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/spots/{id}/weather/forecast returns forecast-only view")
    void getForecast_returnsForecast() throws Exception {
        var spot = new PhotoSpot("user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        var dashboard = new WeatherDashboard(1L, "Test", 78, "good",
                new CurrentWeather(22.0, 20.0, 55, 5.0, "N", 10, 45, 30, 10.0),
                new SolarTimes(null, null, null, null, null, null),
                new GlowForecast("sunset", 65, "good", null),
                List.of());
        when(weatherService.getDashboard(any())).thenReturn(dashboard);

        mockMvc.perform(get(BASE + "/forecast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").doesNotExist())
                .andExpect(jsonPath("$.data.weekForecast").isArray());
    }
}
