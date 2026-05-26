package com.weather.service;

import com.weather.config.WeatherApiConfig.QWeatherProperties;
import com.weather.dto.QWeatherResponses.AirNow;
import com.weather.dto.QWeatherResponses.ApiResponse;
import com.weather.dto.QWeatherResponses.DailyWeather;
import com.weather.dto.QWeatherResponses.NowWeather;
import com.weather.exception.WeatherApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * HTTP client for 和风天气 (QWeather) API v7.
 * All methods are cached via Spring {@link Cacheable} with a TTL defined in the cache manager.
 */
@Service
public class QWeatherClient {

    private static final String SOURCE = "QWeather";

    private final RestTemplate restTemplate;
    private final QWeatherProperties props;
    private final ObjectMapper objectMapper;

    public QWeatherClient(RestTemplate qWeatherRestTemplate, QWeatherProperties props, ObjectMapper objectMapper) {
        this.restTemplate = qWeatherRestTemplate;
        this.props = props;
        this.objectMapper = objectMapper;
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
        var headers = new HttpHeaders();
        headers.set("X-QW-Api-Key", props.key());
        var entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(path, HttpMethod.GET, entity, byte[].class);
            HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
            byte[] bodyBytes = response.getBody();
            String rawBody = decompressIfGzip(response, bodyBytes);

            if (status == HttpStatus.FORBIDDEN || status == HttpStatus.UNAUTHORIZED) {
                String detail = extractDetail(rawBody);
                throw new WeatherApiException(SOURCE,
                        "HTTP " + status.value() + (detail != null ? " — " + detail : " — API key invalid or expired"));
            }
            if (!status.is2xxSuccessful()) {
                String detail = extractDetail(rawBody);
                throw new WeatherApiException(SOURCE,
                        "HTTP " + status.value() + (detail != null ? " — " + detail : ""));
            }
            if (rawBody == null || rawBody.isBlank()) {
                throw new WeatherApiException(SOURCE, "Empty response body");
            }
            return objectMapper.readValue(rawBody, objectMapper.getTypeFactory()
                    .constructType(type.getType()));
        } catch (RestClientException e) {
            throw new WeatherApiException(SOURCE, "HTTP request failed: " + e.getMessage(), e);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new WeatherApiException(SOURCE, "Failed to parse response: " + e.getMessage(), e);
        }
    }

    /**
     * Decompresses the response body if the server returned it gzip-encoded.
     */
    private String decompressIfGzip(ResponseEntity<byte[]> response, byte[] bodyBytes) {
        if (bodyBytes == null || bodyBytes.length == 0) return null;
        boolean isGzip = "gzip".equals(response.getHeaders().getFirst("Content-Encoding"));
        if (!isGzip && bodyBytes.length >= 2) {
            isGzip = (bodyBytes[0] == (byte) 0x1F && bodyBytes[1] == (byte) 0x8B);
        }
        if (!isGzip) return new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
        try (var bais = new ByteArrayInputStream(bodyBytes);
             var gzis = new GZIPInputStream(bais);
             var baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = gzis.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Extracts a human-readable detail message from a problem+json or
     * QWeather-style error response body.
     */
    private String extractDetail(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) return null;
        try {
            var node = objectMapper.readTree(rawBody);
            if (node.has("detail")) return node.get("detail").asText();
            if (node.has("title")) return node.get("title").asText();
            if (node.has("message")) return node.get("message").asText();
        } catch (Exception ignored) {
            // fall through — raw body is not parseable JSON
        }
        return rawBody.length() > 200 ? rawBody.substring(0, 200) : rawBody;
    }
}
