package com.weather.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlertRuleRequest(
        @NotBlank String alertType,
        @NotNull @Min(0) @Max(100) Integer glowProbability,
        @Min(0) @Max(100) Integer maxCloud,
        @Min(0) @Max(12) Integer maxWind,
        @Min(0) @Max(100) Integer minVisibility,
        Integer minTemp,
        Integer maxTemp,
        @NotBlank String pushTime,
        @Email String recipientEmail
) {
    public String toThresholdsJson() {
        return String.format(
                "{\"glow_probability\":%d,\"max_cloud\":%s,\"max_wind\":%s,\"min_visibility\":%s,\"min_temp\":%s,\"max_temp\":%s}",
                glowProbability,
                maxCloud != null ? maxCloud : "null",
                maxWind != null ? maxWind : "null",
                minVisibility != null ? minVisibility : "null",
                minTemp != null ? minTemp : "null",
                maxTemp != null ? maxTemp : "null"
        );
    }
}
