package com.weather.service;

import com.weather.dto.PhotoSpotRequest;
import com.weather.dto.PhotoSpotResponse;
import com.weather.exception.SpotNotFoundException;
import com.weather.model.PhotoSpot;
import com.weather.repository.PhotoSpotRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PhotoSpotService {

    private static final GeometryFactory GF = new GeometryFactory();

    private final PhotoSpotRepository spotRepository;
    private final WeatherService weatherService;

    public PhotoSpotService(PhotoSpotRepository spotRepository, WeatherService weatherService) {
        this.spotRepository = spotRepository;
        this.weatherService = weatherService;
    }

    public PhotoSpotResponse create(String userId, PhotoSpotRequest request) {
        var spot = new PhotoSpot(userId, request.name(),
                toPoint(request.latitude(), request.longitude()), request.tags());
        spot.setNotes(request.notes());
        var saved = spotRepository.save(spot);
        return PhotoSpotResponse.from(saved);
    }

    public PhotoSpotResponse update(String userId, Long spotId, PhotoSpotRequest request) {
        var spot = findOwned(userId, spotId);
        spot.setName(request.name());
        spot.setLocation(toPoint(request.latitude(), request.longitude()));
        spot.setTags(request.tags());
        spot.setNotes(request.notes());
        spot.setUpdatedAt(Instant.now());
        var saved = spotRepository.save(spot);
        return PhotoSpotResponse.from(saved);
    }

    public void delete(String userId, Long spotId) {
        var spot = findOwned(userId, spotId);
        spotRepository.delete(spot);
    }

    @Transactional(readOnly = true)
    public PhotoSpotResponse getById(String userId, Long spotId, boolean includeWeather) {
        var spot = findOwned(userId, spotId);
        if (includeWeather) {
            try {
                var dashboard = weatherService.getDashboard(spot);
                return PhotoSpotResponse.from(spot, dashboard.photographyIndex());
            } catch (Exception e) {
                return PhotoSpotResponse.from(spot);
            }
        }
        return PhotoSpotResponse.from(spot);
    }

    @Transactional(readOnly = true)
    public List<PhotoSpotResponse> listByUser(String userId) {
        return spotRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PhotoSpotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PhotoSpotResponse> listByBounds(String userId,
                                                 double swLat, double swLng,
                                                 double neLat, double neLng) {
        var envelope = GF.createPolygon(new Coordinate[]{
                new Coordinate(swLng, swLat),
                new Coordinate(neLng, swLat),
                new Coordinate(neLng, neLat),
                new Coordinate(swLng, neLat),
                new Coordinate(swLng, swLat)
        });
        envelope.setSRID(4326);
        return spotRepository.findByUserIdWithinBounds(userId, envelope).stream()
                .map(PhotoSpotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PhotoSpotResponse> search(String userId, String query) {
        return spotRepository.searchByUserIdAndName(userId, query).stream()
                .map(PhotoSpotResponse::from)
                .toList();
    }

    private PhotoSpot findOwned(String userId, Long spotId) {
        var spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException(spotId));
        if (!spot.getUserId().equals(userId)) {
            throw new SpotNotFoundException(spotId);
        }
        return spot;
    }

    private static Point toPoint(double latitude, double longitude) {
        return GF.createPoint(new Coordinate(longitude, latitude));
    }
}
