package com.weather.service;

import com.weather.dto.QWeatherResponses.AirNow;
import com.weather.dto.QWeatherResponses.DailyWeather;
import com.weather.dto.QWeatherResponses.NowWeather;
import com.weather.dto.WeatherDashboard;
import com.weather.dto.WeatherDashboard.CurrentWeather;
import com.weather.dto.WeatherDashboard.DailyForecast;
import com.weather.dto.WeatherDashboard.GlowForecast;
import com.weather.model.PhotoSpot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Orchestrates weather data from multiple sources into a {@link WeatherDashboard} for a photo spot.
 * Combines QWeather API data, sun position calculations, and photography index scoring.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final QWeatherClient qWeatherClient;
    private final PhotographyIndexCalculator indexCalculator;
    private final SunCalcService sunCalcService;

    public WeatherService(QWeatherClient qWeatherClient,
                          PhotographyIndexCalculator indexCalculator,
                          SunCalcService sunCalcService) {
        this.qWeatherClient = qWeatherClient;
        this.indexCalculator = indexCalculator;
        this.sunCalcService = sunCalcService;
    }

    /**
     * Builds a complete photography weather dashboard for a given spot.
     *
     * @param spot the photo spot with location data
     * @return fully assembled WeatherDashboard with current conditions, solar times,
     *         glow forecast, and 7-day forecast
     */
    public WeatherDashboard getDashboard(PhotoSpot spot) {
        String location = pointToLocation(spot.getLocation());
        ZoneId zoneId = estimateZone(spot.getLocation());

        var nowWeather = qWeatherClient.getNowWeather(location);
        var dailyWeather = qWeatherClient.get7DayForecast(location);
        var airNow = getAirNowSafe(location);
        var solar = sunCalcService.calculateSolarTimes(
                LocalDate.now(zoneId), spot.getLocation().getY(), spot.getLocation().getX(), zoneId);

        var current = buildCurrentWeather(nowWeather, airNow);
        var index = computeIndex(nowWeather, airNow);
        var glow = computeGlow("sunset", nowWeather, airNow);
        var weekForecast = buildWeekForecast(dailyWeather, spot, zoneId);

        return new WeatherDashboard(
                spot.getId(), spot.getName(), index, indexLabel(index),
                current, solar, glow, weekForecast);
    }

    /**
     * Converts a PostGIS Point to a "longitude,latitude" string for the QWeather API.
     *
     * @param point the PostGIS geometry point (Point.getX() = longitude, Point.getY() = latitude)
     * @return formatted location string
     */
    /**
     * Fetches air quality data, returning null if the API call fails or the
     * subscription does not include air quality data.
     */
    private AirNow getAirNowSafe(String location) {
        try {
            return qWeatherClient.getAirNow(location);
        } catch (Exception e) {
            log.warn("Air quality data unavailable: {}", e.getMessage());
            return null;
        }
    }

    private String pointToLocation(Point point) {
        return String.format("%.2f,%.2f", point.getX(), point.getY());
    }

    /**
     * Estimates a timezone ID from geographic coordinates.
     * Uses a longitude-based offset approximation (every 15° = 1 hour).
     */
    private ZoneId estimateZone(Point point) {
        int offsetSeconds = (int) Math.round(point.getX() / 15.0 * 3600);
        return ZoneOffset.ofTotalSeconds(offsetSeconds);
    }

    private CurrentWeather buildCurrentWeather(NowWeather now, AirNow air) {
        return new CurrentWeather(
                parseDouble(now != null ? now.temp() : null),
                parseDouble(now != null ? now.feelsLike() : null),
                parseInteger(now != null ? now.humidity() : null),
                parseDouble(now != null ? now.windSpeed() : null),
                now != null ? now.windDir() : null,
                parseInteger(now != null ? now.vis() : null),
                parseInteger(air != null ? air.aqi() : null),
                parseInteger(now != null ? now.cloud() : null),
                parseDouble(now != null ? now.precip() : null)
        );
    }

    private int computeIndex(NowWeather now, AirNow air) {
        Double totalCloud = parseDouble(now != null ? now.cloud() : null);
        Double humidity = parseDouble(now != null ? now.humidity() : null);
        Double visibility = parseDouble(now != null ? now.vis() : null);
        Integer aqi = parseInteger(air != null ? air.aqi() : null);
        Double windSpeed = parseDouble(now != null ? now.windSpeed() : null);
        Double precip = parseDouble(now != null ? now.precip() : null);
        return indexCalculator.computePhotographyIndex(totalCloud, humidity, visibility, aqi, windSpeed, precip);
    }

    private GlowForecast computeGlow(String type, NowWeather now, AirNow air) {
        double totalCloud = now != null && now.cloud() != null ? Double.parseDouble(now.cloud()) : 50;
        double humidity = now != null && now.humidity() != null ? Double.parseDouble(now.humidity()) : 50;
        double visibility = now != null && now.vis() != null ? Double.parseDouble(now.vis()) : 10;
        int aqi = air != null && air.aqi() != null ? Integer.parseInt(air.aqi()) : 50;
        double windSpeed = now != null && now.windSpeed() != null ? Double.parseDouble(now.windSpeed()) : 10;
        double precip = now != null && now.precip() != null ? Double.parseDouble(now.precip()) : 0;
        return indexCalculator.forecastGlow(type, totalCloud, humidity, visibility, aqi, windSpeed, precip);
    }

    private List<DailyForecast> buildWeekForecast(List<DailyWeather> dailyList, PhotoSpot spot, ZoneId zoneId) {
        if (dailyList == null || dailyList.isEmpty()) {
            return List.of();
        }
        return dailyList.stream()
                .map(d -> toDailyForecast(d, spot, zoneId))
                .toList();
    }

    private DailyForecast toDailyForecast(DailyWeather daily, PhotoSpot spot, ZoneId zoneId) {
        Instant date = parseDate(daily.fxDate(), zoneId);
        double totalCloud = parseDouble(daily.cloud());
        double humidity = parseDouble(daily.humidity());
        double visibility = parseDouble(daily.vis());
        int aqi = 50; // daily forecast does not include AQI; use neutral default
        double windSpeed = parseDouble(daily.windSpeedDay());
        double precip = parseDouble(daily.precip());

        int photoIndex = indexCalculator.computePhotographyIndex(totalCloud, humidity, visibility, aqi, windSpeed, precip);

        var morningGlow = indexCalculator.forecastGlow("sunrise", totalCloud, humidity, visibility, aqi, windSpeed, precip);
        var eveningGlow = indexCalculator.forecastGlow("sunset", totalCloud, humidity, visibility, aqi, windSpeed, precip);

        return new DailyForecast(
                date, photoIndex,
                parseInteger(daily.tempMax()), parseInteger(daily.tempMin()),
                daily.iconDay(), parseInteger(daily.precip()),
                morningGlow, eveningGlow);
    }

    private static Instant parseDate(String fxDate, ZoneId zoneId) {
        if (fxDate == null) return null;
        return LocalDate.parse(fxDate, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(zoneId)
                .toInstant();
    }

    private static String indexLabel(int index) {
        if (index >= 80) return "excellent";
        if (index >= 60) return "good";
        if (index >= 40) return "fair";
        return "poor";
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static Integer parseInteger(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
