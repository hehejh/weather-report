package com.weather.dto;

import com.weather.model.PhotoSpot;

import java.time.Instant;
import java.util.Set;

public record PhotoSpotResponse(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        Set<String> tags,
        String notes,
        String photoUrl,
        Integer photographyIndex,
        Instant createdAt,
        Instant updatedAt
) {
    public static PhotoSpotResponse from(PhotoSpot spot) {
        return new PhotoSpotResponse(
                spot.getId(),
                spot.getName(),
                spot.getLocation().getY(),
                spot.getLocation().getX(),
                spot.getTags(),
                spot.getNotes(),
                spot.getPhotoUrl(),
                null,
                spot.getCreatedAt(),
                spot.getUpdatedAt()
        );
    }

    public static PhotoSpotResponse from(PhotoSpot spot, Integer photographyIndex) {
        return new PhotoSpotResponse(
                spot.getId(),
                spot.getName(),
                spot.getLocation().getY(),
                spot.getLocation().getX(),
                spot.getTags(),
                spot.getNotes(),
                spot.getPhotoUrl(),
                photographyIndex,
                spot.getCreatedAt(),
                spot.getUpdatedAt()
        );
    }
}
