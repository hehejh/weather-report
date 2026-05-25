package com.weather.service;

import com.weather.dto.PhotoSpotRequest;
import com.weather.dto.PhotoSpotResponse;
import com.weather.dto.WeatherDashboard;
import com.weather.exception.SpotNotFoundException;
import com.weather.model.PhotoSpot;
import com.weather.repository.PhotoSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PhotoSpotServiceTest {

    private PhotoSpotService spotService;
    private PhotoSpotRepository spotRepository;
    private WeatherService weatherService;

    private static final GeometryFactory GF = new GeometryFactory();
    private static final String USER = "default-user";

    @BeforeEach
    void setUp() {
        spotRepository = mock(PhotoSpotRepository.class);
        weatherService = mock(WeatherService.class);
        spotService = new PhotoSpotService(spotRepository, weatherService);
    }

    @Test
    @DisplayName("create saves new spot")
    void create_savesSpot() {
        var request = new PhotoSpotRequest("Test", 39.9, 116.4, Set.of("风光"), null);
        var saved = new PhotoSpot(USER, "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of("风光"));
        saved.setId(1L);
        when(spotRepository.save(any())).thenReturn(saved);

        var result = spotService.create(USER, request);

        assertEquals(1L, result.id());
        assertEquals("Test", result.name());
        verify(spotRepository).save(any());
    }

    @Test
    @DisplayName("update modifies existing spot")
    void update_modifiesSpot() {
        var existing = new PhotoSpot(USER, "Old", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        existing.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(spotRepository.save(any())).thenReturn(existing);

        var request = new PhotoSpotRequest("Updated", 40.0, 117.0, Set.of("new"), null);
        var result = spotService.update(USER, 1L, request);

        assertEquals("Updated", result.name());
        verify(spotRepository).save(any());
    }

    @Test
    @DisplayName("update throws when user does not own spot")
    void update_notOwner_throws() {
        var existing = new PhotoSpot("other-user", "Old", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        existing.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(existing));

        var request = new PhotoSpotRequest("Updated", 40.0, 117.0, Set.of(), null);
        assertThrows(SpotNotFoundException.class, () -> spotService.update(USER, 1L, request));
    }

    @Test
    @DisplayName("delete removes spot when user owns it")
    void delete_owner_removes() {
        var existing = new PhotoSpot(USER, "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        existing.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(existing));

        spotService.delete(USER, 1L);
        verify(spotRepository).delete(existing);
    }

    @Test
    @DisplayName("delete throws when user does not own spot")
    void delete_notOwner_throws() {
        var existing = new PhotoSpot("other-user", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        existing.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(SpotNotFoundException.class, () -> spotService.delete(USER, 1L));
    }

    @Test
    @DisplayName("getById returns spot when exists")
    void getById_existing_returnsSpot() {
        var spot = new PhotoSpot(USER, "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        spot.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        var result = spotService.getById(USER, 1L, false);
        assertEquals(1L, result.id());
        assertEquals("Test", result.name());
    }

    @Test
    @DisplayName("getById throws when spot not found")
    void getById_missing_throws() {
        when(spotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SpotNotFoundException.class, () -> spotService.getById(USER, 99L, false));
    }

    @Test
    @DisplayName("getById throws when user does not own spot")
    void getById_notOwner_throws() {
        var spot = new PhotoSpot("other", "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        spot.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

        assertThrows(SpotNotFoundException.class, () -> spotService.getById(USER, 1L, false));
    }

    @Test
    @DisplayName("listByUser returns spots for user")
    void listByUser_returnsList() {
        var spot = new PhotoSpot(USER, "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        spot.setId(1L);
        when(spotRepository.findByUserIdOrderByCreatedAtDesc(USER)).thenReturn(List.of(spot));

        var result = spotService.listByUser(USER);
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).name());
    }

    @Test
    @DisplayName("listByBounds queries with polygon filter")
    void listByBounds_queriesWithinBounds() {
        var spot = new PhotoSpot(USER, "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        spot.setId(1L);
        when(spotRepository.findByUserIdWithinBounds(any(), any(Polygon.class))).thenReturn(List.of(spot));

        var result = spotService.listByBounds(USER, 39.0, 116.0, 40.0, 117.0);
        assertEquals(1, result.size());
        verify(spotRepository).findByUserIdWithinBounds(any(), any(Polygon.class));
    }

    @Test
    @DisplayName("listByBounds returns empty when bounds are invalid")
    void listByBounds_invalidBounds_returnsEmpty() {
        var result = spotService.listByBounds(USER, 91.0, 116.0, 92.0, 117.0);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("search queries by name")
    void search_queriesByName() {
        var spot = new PhotoSpot(USER, "Tower View", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        spot.setId(1L);
        when(spotRepository.searchByUserIdAndName(USER, "tower")).thenReturn(List.of(spot));

        var result = spotService.search(USER, "tower");
        assertEquals(1, result.size());
        assertEquals("Tower View", result.get(0).name());
    }

    @Test
    @DisplayName("search returns empty for blank query")
    void search_blankQuery_returnsEmpty() {
        var result = spotService.search(USER, "");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getById with includeWeather returns spot with photography index")
    void getById_withWeather_includesIndex() {
        var spot = new PhotoSpot(USER, "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of());
        spot.setId(1L);
        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));
        when(weatherService.getDashboard(any())).thenReturn(
                new WeatherDashboard(1L, "Test", 75, "good", null, null, null, List.of()));

        var result = spotService.getById(USER, 1L, true);
        assertNotNull(result.photographyIndex());
        assertEquals(75, result.photographyIndex());
    }

    @Test
    @DisplayName("PhotoSpotResponse.from maps entity to dto")
    void photoSpotResponseFrom_mapsCorrectly() {
        var spot = new PhotoSpot(USER, "Test", GF.createPoint(new Coordinate(116.4, 39.9)), Set.of("风光"));
        spot.setId(1L);

        var result = PhotoSpotResponse.from(spot);
        assertEquals(1L, result.id());
        assertEquals("Test", result.name());
        assertEquals(39.9, result.latitude());
        assertEquals(116.4, result.longitude());
        assertEquals(Set.of("风光"), result.tags());
    }
}
