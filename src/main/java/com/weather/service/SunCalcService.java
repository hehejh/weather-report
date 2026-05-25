package com.weather.service;

import com.weather.dto.WeatherDashboard.SolarTimes;
import org.shredzone.commons.suncalc.SunTimes;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Computes sunrise, sunset, golden hour, and blue hour times for any location and date.
 * Uses commons-suncalc for astronomical calculations — pure math, no API dependency.
 */
@Service
public class SunCalcService {

    /**
     * @param date      the date to calculate solar times for
     * @param latitude  latitude in decimal degrees (-90 to 90)
     * @param longitude longitude in decimal degrees (-180 to 180)
     * @param zoneId    the timezone for the location
     * @return SolarTimes containing sunrise, sunset, golden hour (morning/evening),
     *         and blue hour (morning/evening) as Instant values; may be null
     *         for polar day/night edges
     */
    public SolarTimes calculateSolarTimes(LocalDate date, double latitude, double longitude, ZoneId zoneId) {
        var sunriseSet = SunTimes.compute()
                .on(date)
                .at(latitude, longitude)
                .timezone(zoneId.getId())
                .execute();

        var goldenMorning = SunTimes.compute()
                .on(date)
                .at(latitude, longitude)
                .timezone(zoneId.getId())
                .twilight(SunTimes.Twilight.GOLDEN_HOUR)
                .execute();

        var goldenEvening = SunTimes.compute()
                .on(date)
                .at(latitude, longitude)
                .timezone(zoneId.getId())
                .twilight(SunTimes.Twilight.GOLDEN_HOUR)
                .execute();

        var blueMorning = SunTimes.compute()
                .on(date)
                .at(latitude, longitude)
                .timezone(zoneId.getId())
                .twilight(SunTimes.Twilight.BLUE_HOUR)
                .execute();

        var blueEvening = SunTimes.compute()
                .on(date)
                .at(latitude, longitude)
                .timezone(zoneId.getId())
                .twilight(SunTimes.Twilight.BLUE_HOUR)
                .execute();

        return new SolarTimes(
                toInstant(sunriseSet.getRise()),
                toInstant(sunriseSet.getSet()),
                toInstant(goldenMorning.getRise()),
                toInstant(goldenEvening.getSet()),
                toInstant(blueMorning.getRise()),
                toInstant(blueEvening.getSet())
        );
    }

    private static Instant toInstant(ZonedDateTime dt) {
        return dt != null ? dt.toInstant() : null;
    }
}
