package com.weather.service;

import com.weather.dto.WeatherDashboard.GlowForecast;
import com.weather.dto.WeatherDashboard.PhotographyIndexBreakdown;
import org.springframework.stereotype.Service;

/**
 * Computes a photography suitability index (0–100) and glow probability from weather parameters.
 * Uses a weighted multi-factor scoring model based on cloud layers, humidity, visibility, AQI, and wind.
 * No external API required — all calculations are algorithmic.
 */
@Service
public class PhotographyIndexCalculator {

    private static final int MAX_SCORE = 100;

    /**
     * @param highCloud  high cloud cover percentage (0–100), ideal 30–70%
     * @param midCloud   mid cloud cover percentage (0–100), ideal 10–50%
     * @param lowCloud   low cloud cover percentage (0–100), ideal 0–20%
     * @param humidity   relative humidity percentage (0–100), ideal 40–70%
     * @param visibility visibility in km, ideal &gt;15
     * @param aqi        air quality index, ideal &lt;50
     * @param windSpeed  wind speed in km/h, ideal 0–10
     * @param precipProbability precipitation probability 0–100, light rain (&lt;30) gives bonus
     * @return photography index 0–100 (0 = worst, 100 = ideal conditions)
     */
    public Integer computePhotographyIndex(
            Double highCloud, Double midCloud, Double lowCloud,
            Double humidity, Double visibility, Integer aqi,
            Double windSpeed, Double precipProbability) {

        int cloudScore = scoreCloud(highCloud, midCloud, lowCloud);
        int humidityScore = scoreHumidity(humidity);
        int visibilityScore = scoreVisibility(visibility);
        int aqiScore = scoreAqi(aqi);
        int windScore = scoreWind(windSpeed, precipProbability);

        double weighted = cloudScore * 0.30
                + humidityScore * 0.20
                + visibilityScore * 0.20
                + aqiScore * 0.15
                + windScore * 0.15;

        return (int) Math.round(weighted);
    }

    /**
     * @param type       "sunrise" or "sunset"
     * @param highCloud  high cloud cover percentage 0–100
     * @param midCloud   mid cloud cover percentage 0–100
     * @param lowCloud   low cloud cover percentage 0–100 (most critical for glow)
     * @param humidity   relative humidity percentage 0–100
     * @param visibility visibility in km
     * @param aqi        air quality index
     * @param windSpeed  wind speed in km/h
     * @param precipProbability precipitation probability 0–100
     * @return GlowForecast with probability, quality label ("excellent"/"good"/"poor"), and per-factor breakdown
     */
    public GlowForecast forecastGlow(String type, double highCloud, double midCloud, double lowCloud,
                                     double humidity, double visibility, int aqi,
                                     double windSpeed, double precipProbability) {
        int probability = computeGlowProbability(type, highCloud, midCloud, lowCloud, humidity);
        String quality = probability >= 70 ? "excellent" : probability >= 40 ? "good" : "poor";

        var breakdown = new PhotographyIndexBreakdown(
                scoreCloud(highCloud, midCloud, lowCloud),
                scoreHumidity(humidity),
                scoreVisibility(visibility),
                scoreAqi(aqi),
                scoreWind(windSpeed, precipProbability),
                buildNotes(highCloud, lowCloud, humidity, aqi)
        );

        return new GlowForecast(type, probability, quality, breakdown);
    }

    private int computeGlowProbability(String type, double highCloud, double midCloud,
                                       double lowCloud, double humidity) {
        double baseProb;
        if (lowCloud > 70) {
            baseProb = 10;
        } else if (lowCloud > 50) {
            baseProb = 100 - (lowCloud - 50) * 3.0;
        } else if (lowCloud > 30) {
            baseProb = 60;
        } else {
            baseProb = 80;
        }
        double highBoost = scoreBand(highCloud, 30, 70, 15) / 100.0 * 20;
        double humidityBonus = scoreBand(humidity, 40, 70, 10) / 100.0 * 10;

        return clamp((int) Math.round(baseProb + highBoost + humidityBonus));
    }

    private int scoreCloud(Double highCloud, Double midCloud, Double lowCloud) {
        double hc = highCloud != null ? highCloud : 50;
        double mc = midCloud != null ? midCloud : 50;
        double lc = lowCloud != null ? lowCloud : 50;

        double highScore = scoreBand(hc, 30, 70, 15);
        double midScore = scoreBand(mc, 10, 50, 10);
        double lowPenalty = lc > 20 ? (lc - 20) * 2.0 : 0;

        return clamp((int) Math.round(highScore + midScore - lowPenalty));
    }

    private int scoreHumidity(Double humidity) {
        if (humidity == null) return 50;
        return clamp((int) Math.round(scoreBand(humidity, 40, 70, 10)));
    }

    private int scoreVisibility(Double visibility) {
        if (visibility == null) return 50;
        return clamp((int) Math.round(Math.min(visibility / 15.0, 1.0) * MAX_SCORE));
    }

    private int scoreAqi(Integer aqi) {
        if (aqi == null) return 50;
        if (aqi <= 50) return 100;
        if (aqi <= 100) return 70;
        if (aqi <= 150) return 40;
        return 10;
    }

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

    private String buildNotes(Double highCloud, Double lowCloud, Double humidity, Integer aqi) {
        StringBuilder sb = new StringBuilder();
        double hc = highCloud != null ? highCloud : 50;
        double lc = lowCloud != null ? lowCloud : 50;

        if (hc >= 30 && hc <= 70 && lc < 30) {
            sb.append("Good high clouds with clear horizon. ");
        } else if (lc > 60) {
            sb.append("Low clouds may block the view. ");
        }
        if (humidity != null && humidity >= 40 && humidity <= 70) {
            sb.append("Favorable humidity for light scattering. ");
        }
        if (aqi != null && aqi <= 50) {
            sb.append("Clean air. ");
        } else if (aqi != null && aqi > 150) {
            sb.append("Poor air quality may reduce visibility. ");
        }
        return sb.toString().trim();
    }

    private double scoreBand(double value, double idealMin, double idealMax, double tolerance) {
        if (value >= idealMin && value <= idealMax) {
            return MAX_SCORE;
        }
        double distBelow = (idealMin - value) / tolerance;
        double distAbove = (value - idealMax) / tolerance;
        double worst = Math.max(distBelow, distAbove);
        return Math.max(0, MAX_SCORE - worst * 10);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(MAX_SCORE, value));
    }
}
