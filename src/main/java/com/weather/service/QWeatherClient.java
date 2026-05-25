package com.weather.service;

import com.weather.config.WeatherApiConfig.QWeatherProperties;
import com.weather.dto.QWeatherResponses.AirNow;
import com.weather.dto.QWeatherResponses.ApiResponse;
import com.weather.dto.QWeatherResponses.DailyWeather;
import com.weather.dto.QWeatherResponses.NowWeather;
import com.weather.exception.WeatherApiException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * HTTP client for 和风天气 (QWeather) API v7.
 * All methods are cached via Spring {@link Cacheable} with a TTL defined in the cache manager.
 */
@Service
public class QWeatherClient {

    private static final String SOURCE = "QWeather";

    private final RestTemplate restTemplate;
    private final QWeatherProperties props;

    public QWeatherClient(RestTemplate qWeatherRestTemplate, QWeatherProperties props) {
        this.restTemplate = qWeatherRestTemplate;
        this.props = props;
    }

    /**
     * Fetches real-time weather for a geographic location.
     *
     * @param location longitude,latitude string (e.g. "116.41,39.92")
     * @return the real-time weather data, or null if the API returns a non-200 code
     * @throws WeatherApiException on HTTP transport errors
     */
    @Cacheable(value = "nowWeather", key = "#location")
    public NowWeather getNowWeather(String location) {
        var type = new ParameterizedTypeReference<ApiResponse<NowWeather>>() {};
        ApiResponse<NowWeather> response = execute("/weather/now?location=" + location, type);
        return response.isOk() ? response.now() : null;
    }

    /**
     * Fetches the 7-day daily forecast for a geographic location.
     *
     * @param location longitude,latitude string (e.g. "116.41,39.92")
     * @return list of daily forecast items, or empty list if the API returns a non-200 code
     * @throws WeatherApiException on HTTP transport errors
     */
    @Cacheable(value = "dailyForecast", key = "#location")
    public List<DailyWeather> get7DayForecast(String location) {
        var type = new ParameterizedTypeReference<ApiResponse<DailyWeather>>() {};
        ApiResponse<DailyWeather> response = execute("/weather/7d?location=" + location, type);
        return response.isOk() && response.daily() != null ? response.daily() : Collections.emptyList();
    }

    /**
     * Fetches current air quality for a geographic location.
     *
     * @param location longitude,latitude string (e.g. "116.41,39.92")
     * @return the air quality data, or null if the API returns a non-200 code
     * @throws WeatherApiException on HTTP transport errors
     */
    @Cacheable(value = "airNow", key = "#location")
    public AirNow getAirNow(String location) {
        var type = new ParameterizedTypeReference<ApiResponse<AirNow>>() {};
        ApiResponse<AirNow> response = execute("/air/now?location=" + location, type);
        return response.isOk() ? response.now() : null;
    }

    private <T> ApiResponse<T> execute(String path, ParameterizedTypeReference<ApiResponse<T>> type) {
        String url = path + "&key=" + props.key();
        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, null, type);
            if (response.getStatusCode() == HttpStatus.FORBIDDEN
                    || response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new WeatherApiException(SOURCE, "API key invalid or expired");
            }
            var body = response.getBody();
            if (body == null) {
                throw new WeatherApiException(SOURCE, "Empty response body");
            }
            return body;
        } catch (RestClientException e) {
            throw new WeatherApiException(SOURCE, "HTTP request failed: " + e.getMessage(), e);
        }
    }
}
