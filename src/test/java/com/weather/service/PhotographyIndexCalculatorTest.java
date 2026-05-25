package com.weather.service;

import com.weather.dto.WeatherDashboard.GlowForecast;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PhotographyIndexCalculator")
class PhotographyIndexCalculatorTest {

    private final PhotographyIndexCalculator calculator = new PhotographyIndexCalculator();

    @Nested
    @DisplayName("computePhotographyIndex")
    class ComputePhotographyIndex {

        @Test
        @DisplayName("returns high score for ideal photography conditions")
        void idealConditions() {
            int index = calculator.computePhotographyIndex(
                    50.0, 55.0, 20.0, 30, 5.0, 0.0
            );
            assertTrue(index >= 75, "Expected >= 75 for ideal conditions, got " + index);
        }

        @Test
        @DisplayName("returns low score for poor photography conditions")
        void poorConditions() {
            int index = calculator.computePhotographyIndex(
                    100.0, 100.0, 1.0, 200, 40.0, 90.0
            );
            assertTrue(index <= 50, "Expected <= 50 for poor conditions, got " + index);
        }

        @Test
        @DisplayName("handles null values gracefully with default scores")
        void nullValues() {
            int index = calculator.computePhotographyIndex(
                    null, null, null, null, null, null
            );
            assertTrue(index >= 70,
                    "Expected moderate-to-high score for null (unknown) data, got " + index);
        }

        @Test
        @DisplayName("slightly suboptimal conditions still score well")
        void slightlySuboptimalConditions() {
            int index = calculator.computePhotographyIndex(
                    65.0, 45.0, 12.0, 60, 12.0, 0.0
            );
            assertTrue(index >= 75, "Expected >= 75 for slightly suboptimal conditions, got " + index);
        }
    }

    @Nested
    @DisplayName("forecastGlow")
    class ForecastGlow {

        @Test
        @DisplayName("returns structured glow forecast with breakdown")
        void structuredGlowForecast() {
            GlowForecast glow = calculator.forecastGlow(
                    "sunset", 40.0, 55.0, 15.0, 30, 5.0, 0.0
            );
            assertEquals("sunset", glow.type());
            assertNotNull(glow.probability());
            assertNotNull(glow.quality());
            assertNotNull(glow.breakdown());
            assertNotNull(glow.breakdown().notes());
        }

        @Test
        @DisplayName("returns poor quality when cloud cover is too low")
        void poorGlowFromLowClouds() {
            GlowForecast glow = calculator.forecastGlow(
                    "sunrise", 5.0, 30.0, 10.0, 50, 10.0, 0.0
            );
            assertTrue(glow.probability() < 50,
                    "Expected low probability with very low cloud cover, got " + glow.probability());
        }
    }
}
