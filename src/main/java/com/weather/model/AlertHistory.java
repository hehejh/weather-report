package com.weather.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "alert_history")
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @Column(name = "weather_snapshot_id")
    private Long weatherSnapshotId;

    @Column
    private Integer score;

    @Column(nullable = false)
    private boolean sent = false;

    public AlertHistory() {}

    public AlertHistory(Long ruleId, Long spotId, Integer score) {
        this.ruleId = ruleId;
        this.spotId = spotId;
        this.score = score;
        this.triggeredAt = Instant.now();
        this.sent = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public Long getSpotId() { return spotId; }
    public void setSpotId(Long spotId) { this.spotId = spotId; }

    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }

    public Long getWeatherSnapshotId() { return weatherSnapshotId; }
    public void setWeatherSnapshotId(Long weatherSnapshotId) { this.weatherSnapshotId = weatherSnapshotId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }
}
