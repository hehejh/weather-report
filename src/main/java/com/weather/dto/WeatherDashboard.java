package com.weather.dto;

import java.time.Instant;
import java.util.List;

public record WeatherDashboard(
        Long spotId,
        String spotName,
        Integer photographyIndex,
        String indexLabel,
        CurrentWeather current,
        SolarTimes solar,
        GlowForecast glow,
        List<DailyForecast> weekForecast
) {
    public record CurrentWeather(
            Double temperature,
            Double feelsLike,
            Integer humidity,
            Double windSpeed,
            String windDirection,
            Integer visibility,
            Integer aqi,
            Integer totalCloud,
            Double precipitationProbability
    ) {}

    public record SolarTimes(
            Instant sunrise,
            Instant sunset,
            Instant goldenHourMorning,
            Instant goldenHourEvening,
            Instant blueHourMorning,
            Instant blueHourEvening
    ) {}

    public record GlowForecast(
            String type,
            Integer probability,
            String quality,
            PhotographyIndexBreakdown breakdown
    ) {}

    public record PhotographyIndexBreakdown(
            Integer cloudScore,
            Integer humidityScore,
            Integer visibilityScore,
            Integer aqiScore,
            Integer windScore,
            String notes
    ) {}

    public record DailyForecast(
            Instant date,
            Integer photographyIndex,
            Integer tempMax,
            Integer tempMin,
            String weatherIcon,
            Integer precipitationProbability,
            GlowForecast morningGlow,
            GlowForecast eveningGlow
    ) {}
}
