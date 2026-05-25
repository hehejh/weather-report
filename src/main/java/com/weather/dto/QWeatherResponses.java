package com.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTOs matching the 和风天气 (QWeather) API v7 JSON response structures.
 */
public final class QWeatherResponses {

    private QWeatherResponses() {}

    /**
     * Wrapper for all QWeather API responses. Every endpoint returns this envelope.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiResponse<T>(
            @JsonProperty("code") String code,
            @JsonProperty("updateTime") String updateTime,
            @JsonProperty("fxLink") String fxLink,
            @JsonProperty("now") T now,
            @JsonProperty("daily") List<T> daily,
            @JsonProperty("hourly") List<T> hourly,
            @JsonProperty("refer") Refer refer
    ) {
        public boolean isOk() {
            return "200".equals(code);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Refer(
            @JsonProperty("sources") List<String> sources,
            @JsonProperty("license") List<String> license
    ) {}

    /**
     * Real-time weather: /v7/weather/now
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NowWeather(
            @JsonProperty("obsTime") String obsTime,
            @JsonProperty("temp") String temp,
            @JsonProperty("feelsLike") String feelsLike,
            @JsonProperty("icon") String icon,
            @JsonProperty("text") String text,
            @JsonProperty("wind360") String wind360,
            @JsonProperty("windDir") String windDir,
            @JsonProperty("windScale") String windScale,
            @JsonProperty("windSpeed") String windSpeed,
            @JsonProperty("humidity") String humidity,
            @JsonProperty("precip") String precip,
            @JsonProperty("pressure") String pressure,
            @JsonProperty("vis") String vis,
            @JsonProperty("cloud") String cloud,
            @JsonProperty("dew") String dew
    ) {}

    /**
     * Daily forecast item: /v7/weather/7d
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DailyWeather(
            @JsonProperty("fxDate") String fxDate,
            @JsonProperty("sunrise") String sunrise,
            @JsonProperty("sunset") String sunset,
            @JsonProperty("moonrise") String moonrise,
            @JsonProperty("moonset") String moonset,
            @JsonProperty("moonPhase") String moonPhase,
            @JsonProperty("tempMax") String tempMax,
            @JsonProperty("tempMin") String tempMin,
            @JsonProperty("iconDay") String iconDay,
            @JsonProperty("textDay") String textDay,
            @JsonProperty("iconNight") String iconNight,
            @JsonProperty("textNight") String textNight,
            @JsonProperty("wind360Day") String wind360Day,
            @JsonProperty("windDirDay") String windDirDay,
            @JsonProperty("windScaleDay") String windScaleDay,
            @JsonProperty("windSpeedDay") String windSpeedDay,
            @JsonProperty("wind360Night") String wind360Night,
            @JsonProperty("windDirNight") String windDirNight,
            @JsonProperty("windScaleNight") String windScaleNight,
            @JsonProperty("windSpeedNight") String windSpeedNight,
            @JsonProperty("humidity") String humidity,
            @JsonProperty("precip") String precip,
            @JsonProperty("pressure") String pressure,
            @JsonProperty("vis") String vis,
            @JsonProperty("cloud") String cloud,
            @JsonProperty("uvIndex") String uvIndex
    ) {}

    /**
     * Air quality: /v7/air/now
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AirNow(
            @JsonProperty("pubTime") String pubTime,
            @JsonProperty("aqi") String aqi,
            @JsonProperty("level") String level,
            @JsonProperty("category") String category,
            @JsonProperty("primary") String primary,
            @JsonProperty("pm10") String pm10,
            @JsonProperty("pm2p5") String pm2p5,
            @JsonProperty("no2") String no2,
            @JsonProperty("so2") String so2,
            @JsonProperty("co") String co,
            @JsonProperty("o3") String o3
    ) {}
}
