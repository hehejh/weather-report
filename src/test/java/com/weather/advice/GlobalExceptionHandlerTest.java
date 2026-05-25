package com.weather.advice;

import com.weather.dto.ApiResponse;
import com.weather.exception.SpotNotFoundException;
import com.weather.exception.WeatherApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("SpotNotFoundException maps to 404 with error message")
    void handleSpotNotFound_returns404() {
        var ex = new SpotNotFoundException(42L);
        var response = handler.handleSpotNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("Photo spot not found: id=42", response.getBody().error());
    }

    @Test
    @DisplayName("WeatherApiException maps to 502 with error message")
    void handleWeatherApi_returns502() {
        var ex = new WeatherApiException("QWeather", "timeout");
        var response = handler.handleWeatherApi(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertFalse(response.getBody().success());
    }

    @Test
    @DisplayName("IllegalArgumentException maps to 400")
    void handleIllegalArgument_returns400() {
        var ex = new IllegalArgumentException("invalid bounds");
        var response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid bounds", response.getBody().error());
    }

    @Test
    @DisplayName("Generic Exception maps to 500 with safe message")
    void handleGeneral_returns500() {
        var ex = new RuntimeException("sensitive internal detail");
        var response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().error());
    }

    @Test
    @DisplayName("ok response has success=true and no error")
    void apiResponse_ok_hasSuccessTrue() {
        var response = ApiResponse.ok("hello");

        assertEquals(true, response.success());
        assertEquals("hello", response.data());
        assertNull(response.error());
    }

    @Test
    @DisplayName("error response has success=false and message")
    void apiResponse_error_hasSuccessFalse() {
        var response = ApiResponse.error("something went wrong");

        assertEquals(false, response.success());
        assertEquals("something went wrong", response.error());
    }
}
