package com.weather.controller;

import com.weather.dto.PhotoSpotResponse;
import com.weather.exception.SpotNotFoundException;
import com.weather.service.PhotoSpotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PhotoSpotControllerTest {

    private MockMvc mockMvc;
    private PhotoSpotService spotService;

    private static final String BASE = "/api/spots";

    @BeforeEach
    void setUp() {
        spotService = mock(PhotoSpotService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PhotoSpotController(spotService))
                .setControllerAdvice(new com.weather.advice.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/spots returns spot list")
    void listSpots_returnsList() throws Exception {
        var spot = new PhotoSpotResponse(1L, "Test", 39.9, 116.4, Set.of(), null, null, 75,
                Instant.now(), Instant.now());
        when(spotService.listByUser("default-user")).thenReturn(List.of(spot));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Test"));
    }

    @Test
    @DisplayName("GET /api/spots with q param triggers search")
    void listSpots_withQuery_callsSearch() throws Exception {
        when(spotService.search(eq("default-user"), eq("tower"))).thenReturn(List.of());

        mockMvc.perform(get(BASE).param("q", "tower"))
                .andExpect(status().isOk());

        verify(spotService).search("default-user", "tower");
    }

    @Test
    @DisplayName("GET /api/spots with bounds params triggers bounds query")
    void listSpots_withBounds_callsBoundsQuery() throws Exception {
        when(spotService.listByBounds("default-user", 39.0, 116.0, 40.0, 117.0))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE)
                        .param("swLat", "39.0")
                        .param("swLng", "116.0")
                        .param("neLat", "40.0")
                        .param("neLng", "117.0"))
                .andExpect(status().isOk());

        verify(spotService).listByBounds("default-user", 39.0, 116.0, 40.0, 117.0);
    }

    @Test
    @DisplayName("POST /api/spots creates spot and returns 201")
    void createSpot_validRequest_returns201() throws Exception {
        var spot = new PhotoSpotResponse(1L, "New", 39.9, 116.4, Set.of("风光"), null, null, null,
                Instant.now(), Instant.now());
        when(spotService.create(eq("default-user"), any())).thenReturn(spot);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New","latitude":39.9,"longitude":116.4,"tags":["风光"]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("New"));
    }

    @Test
    @DisplayName("POST /api/spots with invalid request returns 400")
    void createSpot_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","latitude":null,"longitude":null}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/spots/{id} returns spot")
    void getSpot_existing_returnsSpot() throws Exception {
        var spot = new PhotoSpotResponse(1L, "Test", 39.9, 116.4, Set.of(), null, null, null,
                Instant.now(), Instant.now());
        when(spotService.getById("default-user", 1L, false)).thenReturn(spot);

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /api/spots/{id} returns 404 when not found")
    void getSpot_notFound_returns404() throws Exception {
        when(spotService.getById("default-user", 99L, false))
                .thenThrow(new SpotNotFoundException(99L));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/spots/{id} updates spot")
    void updateSpot_valid_returnsUpdated() throws Exception {
        var spot = new PhotoSpotResponse(1L, "Updated", 40.0, 117.0, Set.of(), null, null, null,
                Instant.now(), Instant.now());
        when(spotService.update(eq("default-user"), eq(1L), any())).thenReturn(spot);

        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated","latitude":40.0,"longitude":117.0}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }

    @Test
    @DisplayName("DELETE /api/spots/{id} returns 204")
    void deleteSpot_existing_returns204() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());
    }
}
