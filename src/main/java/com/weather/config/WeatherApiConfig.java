package com.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configures HTTP clients for weather API integrations.
 */
@Configuration
public class WeatherApiConfig {

    @Bean
    @ConfigurationProperties(prefix = "weather.api.qweather")
    public QWeatherProperties qWeatherProperties() {
        return new QWeatherProperties();
    }

    /**
     * RestTemplate pre-configured with base URL and timeouts for 和风天气 API.
     */
    @Bean
    public RestTemplate qWeatherRestTemplate(QWeatherProperties props) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return new RestTemplateBuilder()
                .rootUri(props.baseUrl())
                .defaultHeader("User-Agent", "PhotoWeatherApp/1.0")
                .detectRequestFactory(false)
                .requestFactory(() -> factory)
                .build();
    }

    /**
     * Properties bound to weather.api.qweather in application.yml.
     */
    public record QWeatherProperties(String baseUrl, String key) {
        public QWeatherProperties() {
            this("https://devapi.qweather.com/v7", "");
        }
    }
}
