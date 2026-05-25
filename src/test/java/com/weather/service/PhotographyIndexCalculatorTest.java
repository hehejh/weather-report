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
                    50.0, 30.0, 10.0, 55.0, 20.0, 30, 5.0, 0.0
            );
            assertTrue(index >= 75, "Expected >= 75 for ideal conditions, got " + index);
        }

        @Test
        @DisplayName("returns low score for poor photography conditions")
        void poorConditions() {
            int index = calculator.computePhotographyIndex(
                    90.0, 80.0, 85.0, 95.0, 2.0, 180, 40.0, 80.0
            );
            assertTrue(index <= 30, "Expected <= 30 for poor conditions, got " + index);
        }

        @Test
        @DisplayName("handles null values gracefully with default scores")
        void nullValues() {
            int index = calculator.computePhotographyIndex(
                    null, null, null, null, null, null, null, null
            );
            assertTrue(index >= 70,
                    "Expected moderate score for null (unknown) data, got " + index);
        }

        @Test
        @DisplayName("good high clouds with clear horizon scores well")
        void goodHighCloudsClearHorizon() {
            int index = calculator.computePhotographyIndex(
                    50.0, 20.0, 5.0, 50.0, 15.0, 40, 8.0, 0.0
            );
            assertTrue(index >= 70, "Expected >= 70, got " + index);
        }
    }

    @Nested
    @DisplayName("forecastGlow")
    class ForecastGlow {

        @Test
        @DisplayName("returns structured glow forecast with breakdown")
        void structuredGlowForecast() {
            GlowForecast glow = calculator.forecastGlow(
                    "sunset", 55.0, 25.0, 10.0, 50.0, 20.0, 30, 5.0, 0.0
            );
            assertEquals("sunset", glow.type());
            assertNotNull(glow.probability());
            assertNotNull(glow.quality());
            assertNotNull(glow.breakdown());
            assertNotNull(glow.breakdown().notes());
        }

        @Test
        @DisplayName("returns poor quality when low clouds dominate")
        void poorGlowFromLowClouds() {
            GlowForecast glow = calculator.forecastGlow(
                    "sunrise", 10.0, 20.0, 80.0, 30.0, 10.0, 50, 10.0, 0.0
            );
            assertTrue(glow.probability() < 50,
                    "Expected low probability with heavy low clouds, got " + glow.probability());
        }
    }
}
