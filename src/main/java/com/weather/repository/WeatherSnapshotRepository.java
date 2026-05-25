package com.weather.repository;

import com.weather.model.WeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshot, Long> {

    Optional<WeatherSnapshot> findTopBySpotIdAndForecastForOrderByFetchedAtDesc(
            Long spotId, Instant forecastFor);

    @Query("""
            SELECT ws FROM WeatherSnapshot ws
            WHERE ws.spot.id = :spotId
              AND ws.forecastFor BETWEEN :from AND :to
            ORDER BY ws.forecastFor ASC
            """)
    List<WeatherSnapshot> findBySpotIdAndForecastForBetween(
            @Param("spotId") Long spotId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    void deleteBySpotIdAndFetchedAtBefore(Long spotId, Instant cutoff);
}
