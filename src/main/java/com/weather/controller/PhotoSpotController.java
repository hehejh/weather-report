package com.weather.controller;

import com.weather.dto.ApiResponse;
import com.weather.dto.PhotoSpotRequest;
import com.weather.dto.PhotoSpotResponse;
import com.weather.service.PhotoSpotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/spots")
public class PhotoSpotController {

    private static final String USER_ID = "default-user";

    private final PhotoSpotService spotService;

    public PhotoSpotController(PhotoSpotService spotService) {
        this.spotService = spotService;
    }

    @GetMapping
    public ApiResponse<List<PhotoSpotResponse>> listSpots(
            @RequestParam(required = false) Double swLat,
            @RequestParam(required = false) Double swLng,
            @RequestParam(required = false) Double neLat,
            @RequestParam(required = false) Double neLng,
            @RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return ApiResponse.ok(spotService.search(USER_ID, q));
        }
        if (swLat != null && swLng != null && neLat != null && neLng != null) {
            return ApiResponse.ok(spotService.listByBounds(USER_ID, swLat, swLng, neLat, neLng));
        }
        return ApiResponse.ok(spotService.listByUser(USER_ID));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PhotoSpotResponse> createSpot(@Valid @RequestBody PhotoSpotRequest request) {
        return ApiResponse.ok(spotService.create(USER_ID, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PhotoSpotResponse> getSpot(@PathVariable Long id) {
        return ApiResponse.ok(spotService.getById(USER_ID, id, false));
    }

    @PutMapping("/{id}")
    public ApiResponse<PhotoSpotResponse> updateSpot(@PathVariable Long id,
                                                      @Valid @RequestBody PhotoSpotRequest request) {
        return ApiResponse.ok(spotService.update(USER_ID, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSpot(@PathVariable Long id) {
        spotService.delete(USER_ID, id);
    }
}
