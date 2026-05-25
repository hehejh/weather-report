package com.weather.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record PhotoSpotRequest(
        @NotBlank String name,
        @NotNull Double latitude,
        @NotNull Double longitude,
        Set<String> tags,
        String notes
) {}
