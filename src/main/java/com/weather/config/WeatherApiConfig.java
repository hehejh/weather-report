package com.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Configures HTTP clients for weather API integrations.
 */
@Configuration
@EnableConfigurationProperties(WeatherApiConfig.QWeatherProperties.class)
public class WeatherApiConfig {

    /**
     * RestTemplate pre-configured with base URL and timeouts for 和风天气 API.
     */
    @Bean
    public RestTemplate qWeatherRestTemplate(QWeatherProperties props) {
        var factory = new org.springframework.http.client.JdkClientHttpRequestFactory();
        factory.setReadTimeout(java.time.Duration.ofSeconds(10));
        var template = new RestTemplateBuilder()
                .rootUri(props.baseUrl())
                .defaultHeader("User-Agent", "PhotoWeatherApp/1.0")
                .detectRequestFactory(false)
                .requestFactory(() -> factory)
                .build();
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // no-op: let QWeatherClient handle status codes
            }
        });
        return template;
    }

    /**
     * Properties bound to weather.api.qweather in application.yml.
     */
    @ConfigurationProperties(prefix = "weather.api.qweather")
    public record QWeatherProperties(String baseUrl, String key) {}
}
