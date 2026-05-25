CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE photo_spots (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(64) NOT NULL,
    name        VARCHAR(128) NOT NULL,
    location    GEOMETRY(POINT, 4326) NOT NULL,
    tags        TEXT[] DEFAULT '{}',
    notes       TEXT,
    photo_url   VARCHAR(512),
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_photo_spots_user ON photo_spots(user_id);
CREATE INDEX idx_photo_spots_location ON photo_spots USING GIST(location);

CREATE TABLE alert_rules (
    id          BIGSERIAL PRIMARY KEY,
    spot_id     BIGINT REFERENCES photo_spots(id) ON DELETE CASCADE,
    alert_type  VARCHAR(32) NOT NULL,
    thresholds  JSONB NOT NULL DEFAULT '{}',
    push_time   TIME NOT NULL,
    enabled     BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_alert_rules_spot ON alert_rules(spot_id);
CREATE INDEX idx_alert_rules_enabled ON alert_rules(enabled) WHERE enabled = TRUE;

CREATE TABLE weather_snapshots (
    id              BIGSERIAL PRIMARY KEY,
    spot_id         BIGINT REFERENCES photo_spots(id) ON DELETE CASCADE,
    fetched_at      TIMESTAMPTZ DEFAULT NOW(),
    forecast_for    TIMESTAMPTZ NOT NULL,
    data            JSONB NOT NULL,
    source          VARCHAR(64)
);

CREATE INDEX idx_weather_snapshots_spot_time
    ON weather_snapshots(spot_id, forecast_for DESC);

CREATE TABLE alert_history (
    id                  BIGSERIAL PRIMARY KEY,
    rule_id             BIGINT REFERENCES alert_rules(id) ON DELETE SET NULL,
    spot_id             BIGINT REFERENCES photo_spots(id) ON DELETE CASCADE,
    triggered_at        TIMESTAMPTZ DEFAULT NOW(),
    weather_snapshot_id  BIGINT REFERENCES weather_snapshots(id),
    score               INTEGER,
    sent                BOOLEAN DEFAULT FALSE
);
