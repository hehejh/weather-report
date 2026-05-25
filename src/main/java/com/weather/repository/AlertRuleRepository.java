package com.weather.repository;

import com.weather.model.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    List<AlertRule> findBySpotIdOrderByCreatedAtDesc(Long spotId);

    List<AlertRule> findBySpotIdAndEnabledTrue(Long spotId);

    @Query("SELECT r FROM AlertRule r JOIN FETCH r.spot WHERE r.enabled = true")
    List<AlertRule> findAllEnabledWithSpot();

    @Query("SELECT COUNT(r) FROM AlertRule r WHERE r.spot.id = :spotId AND r.enabled = true")
    long countEnabledBySpotId(@Param("spotId") Long spotId);
}
