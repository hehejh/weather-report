package com.weather.service;

import com.weather.dto.WeatherDashboard.GlowForecast;
import com.weather.dto.WeatherDashboard.PhotographyIndexBreakdown;
import org.springframework.stereotype.Service;

/**
 * Computes a photography suitability index (0–100) and glow probability from weather parameters.
 * Uses a weighted multi-factor scoring model — no external API required, all calculations are algorithmic.
 *
 * <p>Scoring model:</p>
 * <ul>
 *   <li>Total cloud (35%): ideal 20–60% (enough for drama, not overcast)</li>
 *   <li>Humidity (20%): ideal 40–70% (Mie scattering sweet spot)</li>
 *   <li>Visibility (20%): ideal &gt;15 km</li>
 *   <li>AQI (15%): ideal &lt;50 (clean air)</li>
 *   <li>Wind (10%): ideal 0–10 km/h, light rain bonus</li>
 * </ul>
 */
@Service
public class PhotographyIndexCalculator {

    private static final int MAX_SCORE = 100;

    /**
     * @param totalCloud total cloud cover percentage (0–100), ideal 20–60%
     * @param humidity   relative humidity percentage (0–100), ideal 40–70%
     * @param visibility visibility in km, ideal &gt;15
     * @param aqi        air quality index, ideal &lt;50
     * @param windSpeed  wind speed in km/h, ideal 0–10
     * @param precipProbability precipitation probability 0–100, light rain (&lt;30) gives bonus
     * @return photography index 0–100 (0 = worst, 100 = ideal conditions)
     */
    public Integer computePhotographyIndex(
            Double totalCloud, Double humidity, Double visibility, Integer aqi,
            Double windSpeed, Double precipProbability) {

        int cloudScore = scoreCloud(totalCloud);
        int humidityScore = scoreHumidity(humidity);
        int visibilityScore = scoreVisibility(visibility);
        int aqiScore = scoreAqi(aqi);
        int windScore = scoreWind(windSpeed, precipProbability);

        double weighted = cloudScore * 0.35
                + humidityScore * 0.20
                + visibilityScore * 0.20
                + aqiScore * 0.15
                + windScore * 0.10;

        return (int) Math.round(weighted);
    }

    /**
     * @param type       "sunrise" or "sunset"
     * @param totalCloud total cloud cover percentage 0–100
     * @param humidity   relative humidity percentage 0–100
     * @param visibility visibility in km
     * @param aqi        air quality index
     * @param windSpeed  wind speed in km/h
     * @param precipProbability precipitation probability 0–100
     * @return GlowForecast with probability, quality label, and per-factor breakdown
     */
    public GlowForecast forecastGlow(String type, double totalCloud,
                                     double humidity, double visibility, int aqi,
                                     double windSpeed, double precipProbability) {
        int probability = computeGlowProbability(totalCloud, humidity);
        String quality = probability >= 70 ? "excellent" : probability >= 40 ? "good" : "poor";

        var breakdown = new PhotographyIndexBreakdown(
                scoreCloud(totalCloud),
                scoreHumidity(humidity),
                scoreVisibility(visibility),
                scoreAqi(aqi),
                scoreWind(windSpeed, precipProbability),
                buildNotes(totalCloud, humidity, aqi)
        );

        return new GlowForecast(type, probability, quality, breakdown);
    }

    /**
     * Glow probability model: moderate cloud at right altitude scatters light for colorful sunrises/sunsets.
     * Too little cloud = no color; too much = sun obscured.
     */
    private int computeGlowProbability(double totalCloud, double humidity) {
        double cloudBonus;
        if (totalCloud >= 20 && totalCloud <= 60) {
            cloudBonus = 50;
        } else if (totalCloud < 20) {
            cloudBonus = totalCloud * 2.5;
        } else if (totalCloud <= 80) {
            cloudBonus = 50 - (totalCloud - 60) * 2.0;
        } else {
            cloudBonus = 10;
        }
        double humidityBonus = scoreBand(humidity, 40, 70, 10) / 100.0 * 20;
        return clamp((int) Math.round(cloudBonus + humidityBonus));
    }

    /**
     * Cloud scoring: 20–60% is ideal for photography — enough texture without blocking light.
     * Below 20% = too clear; above 60% = increasingly overcast.
     */
    private int scoreCloud(Double totalCloud) {
        double tc = totalCloud != null ? totalCloud : 50;
        return clamp((int) Math.round(scoreBand(tc, 20, 60, 20)));
    }

    /**
     * Humidity scoring: 40–70% is ideal for Mie scattering, producing vibrant colors.
     */
    private int scoreHumidity(Double humidity) {
        if (humidity == null) return 50;
        return clamp((int) Math.round(scoreBand(humidity, 40, 70, 10)));
    }

    /**
     * Visibility scoring: linear scale, 15 km or better = 100.
     */
    private int scoreVisibility(Double visibility) {
        if (visibility == null) return 50;
        return clamp((int) Math.round(Math.min(visibility / 15.0, 1.0) * MAX_SCORE));
    }

    /**
     * AQI scoring: &le;50 = excellent (100), &le;100 = good (70), &le;150 = fair (40), &gt;150 = poor (10).
     */
    private int scoreAqi(Integer aqi) {
        if (aqi == null) return 50;
        if (aqi <= 50) return 100;
        if (aqi <= 100) return 70;
        if (aqi <= 150) return 40;
        return 10;
    }

    /**
     * Wind scoring: calm (&le;10 km/h) = 100. Light rain (&lt;30% prob) after calm wind gives bonus.
     */
    private int scoreWind(Double windSpeed, Double precip) {
        double ws = windSpeed != null ? windSpeed : 10;
        double windScore;
        if (ws <= 10) windScore = 100;
        else if (ws <= 20) windScore = 70;
        else if (ws <= 30) windScore = 40;
        else windScore = 5;

        double p = precip != null ? precip : 0;
        if (p > 0 && p < 30) windScore = Math.min(100, windScore + 10);
        return clamp((int) Math.round(windScore));
    }

    /**
     * Builds human-readable notes explaining the key factors affecting the score.
     */
    private String buildNotes(Double totalCloud, Double humidity, Integer aqi) {
        StringBuilder sb = new StringBuilder();
        double tc = totalCloud != null ? totalCloud : 50;

        if (tc >= 20 && tc <= 60) {
            sb.append("Favorable cloud cover for photography. ");
        } else if (tc > 80) {
            sb.append("Heavy cloud cover may block sunlight. ");
        } else if (tc < 10) {
            sb.append("Clear sky — limited color potential. ");
        }
        if (humidity != null && humidity >= 40 && humidity <= 70) {
            sb.append("Humidity in ideal range for color. ");
        }
        if (aqi != null && aqi <= 50) {
            sb.append("Clean air. ");
        } else if (aqi != null && aqi > 150) {
            sb.append("Poor air quality reduces clarity. ");
        }
        return sb.toString().trim();
    }

    /**
     * Scores a value on a band: 100 inside [idealMin, idealMax], decreasing with distance outside.
     */
    private double scoreBand(double value, double idealMin, double idealMax, double tolerance) {
        if (value >= idealMin && value <= idealMax) return MAX_SCORE;
        double distBelow = (idealMin - value) / tolerance;
        double distAbove = (value - idealMax) / tolerance;
        double worst = Math.max(distBelow, distAbove);
        return Math.max(0, MAX_SCORE - worst * 10);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(MAX_SCORE, value));
    }
}
