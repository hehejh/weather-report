package com.weather.controller;

import com.weather.dto.ApiResponse;
import com.weather.dto.WeatherDashboard;
import com.weather.exception.SpotNotFoundException;
import com.weather.model.PhotoSpot;
import com.weather.repository.PhotoSpotRepository;
import com.weather.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spots/{id}/weather")
public class WeatherController {

    private static final String USER_ID = "default-user";

    private final WeatherService weatherService;
    private final PhotoSpotRepository spotRepository;

    public WeatherController(WeatherService weatherService, PhotoSpotRepository spotRepository) {
        this.weatherService = weatherService;
        this.spotRepository = spotRepository;
    }

    @GetMapping
    public ApiResponse<WeatherDashboard> getDashboard(@PathVariable Long id) {
        var spot = findSpot(id);
        return ApiResponse.ok(weatherService.getDashboard(spot));
    }

    @GetMapping("/forecast")
    public ApiResponse<WeatherDashboard> getForecast(@PathVariable Long id) {
        var spot = findSpot(id);
        var dashboard = weatherService.getDashboard(spot);
        return ApiResponse.ok(new WeatherDashboard(
                dashboard.spotId(), dashboard.spotName(),
                dashboard.photographyIndex(), dashboard.indexLabel(),
                null, null, null,
                dashboard.weekForecast()));
    }

    private PhotoSpot findSpot(Long id) {
        return spotRepository.findById(id)
                .orElseThrow(() -> new SpotNotFoundException(id));
    }
}
