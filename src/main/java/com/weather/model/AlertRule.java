package com.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "alert_rules")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private PhotoSpot spot;

    @Column(name = "alert_type", nullable = false, length = 32)
    private String alertType;

    @Column(columnDefinition = "JSONB NOT NULL DEFAULT '{}'")
    private String thresholds;

    @Column(name = "push_time", nullable = false)
    private LocalTime pushTime;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at")
    private Instant createdAt;

    public AlertRule() {}

    public AlertRule(PhotoSpot spot, String alertType, String thresholds, LocalTime pushTime) {
        this.spot = spot;
        this.alertType = alertType;
        this.thresholds = thresholds;
        this.pushTime = pushTime;
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PhotoSpot getSpot() { return spot; }
    public void setSpot(PhotoSpot spot) { this.spot = spot; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getThresholds() { return thresholds; }
    public void setThresholds(String thresholds) { this.thresholds = thresholds; }

    public LocalTime getPushTime() { return pushTime; }
    public void setPushTime(LocalTime pushTime) { this.pushTime = pushTime; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
