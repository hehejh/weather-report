package com.weather.service;

import com.weather.dto.WeatherDashboard.SolarTimes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SunCalcServiceTest {

    private final SunCalcService sunCalcService = new SunCalcService();

    @Test
    @DisplayName("calculateSolarTimes returns times for Beijing")
    void calculateSolarTimes_beijing_returnsTimes() {
        var result = sunCalcService.calculateSolarTimes(
                LocalDate.of(2025, 6, 15),
                39.9, 116.4,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(result);
        assertNotNull(result.sunrise());
        assertNotNull(result.sunset());
        assertNotNull(result.goldenHourMorning());
        assertNotNull(result.goldenHourEvening());
        assertNotNull(result.blueHourMorning());
        assertNotNull(result.blueHourEvening());
    }

    @Test
    @DisplayName("calculateSolarTimes returns times for New York")
    void calculateSolarTimes_newYork_returnsTimes() {
        var result = sunCalcService.calculateSolarTimes(
                LocalDate.of(2025, 12, 21),
                40.7, -74.0,
                ZoneId.of("America/New_York"));

        assertNotNull(result);
        assertNotNull(result.sunrise());
        assertNotNull(result.sunset());
    }

    @Test
    @DisplayName("calculateSolarTimes winter vs summer difference")
    void calculateSolarTimes_winterShorterDay() {
        var summer = sunCalcService.calculateSolarTimes(
                LocalDate.of(2025, 6, 21), 39.9, 116.4, ZoneId.of("Asia/Shanghai"));
        var winter = sunCalcService.calculateSolarTimes(
                LocalDate.of(2025, 12, 21), 39.9, 116.4, ZoneId.of("Asia/Shanghai"));

        // Summer day length (sunset - sunrise) should be longer than winter
        long summerDay = summer.sunset().toEpochMilli() - summer.sunrise().toEpochMilli();
        long winterDay = winter.sunset().toEpochMilli() - winter.sunrise().toEpochMilli();
        assert summerDay > winterDay : "Summer day should be longer than winter day in Beijing";
    }
}
