package com.weather.model;

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

@Entity
@Table(name = "weather_snapshots")
public class WeatherSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private PhotoSpot spot;

    @Column(name = "fetched_at")
    private Instant fetchedAt;

    @Column(name = "forecast_for", nullable = false)
    private Instant forecastFor;

    @Column(columnDefinition = "JSONB NOT NULL")
    private String data;

    @Column(length = 64)
    private String source;

    public WeatherSnapshot() {}

    public WeatherSnapshot(PhotoSpot spot, Instant forecastFor, String data, String source) {
        this.spot = spot;
        this.forecastFor = forecastFor;
        this.data = data;
        this.source = source;
        this.fetchedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PhotoSpot getSpot() { return spot; }
    public void setSpot(PhotoSpot spot) { this.spot = spot; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }

    public Instant getForecastFor() { return forecastFor; }
    public void setForecastFor(Instant forecastFor) { this.forecastFor = forecastFor; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
