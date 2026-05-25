package com.weather.exception;

public class WeatherApiException extends RuntimeException {

    public WeatherApiException(String source, String message) {
        super("Weather API error [" + source + "]: " + message);
    }

    public WeatherApiException(String source, String message, Throwable cause) {
        super("Weather API error [" + source + "]: " + message, cause);
    }
}
