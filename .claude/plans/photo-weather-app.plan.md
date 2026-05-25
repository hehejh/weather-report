# Plan: 摄影天气助手 MVP

**Source PRD:** docs/PRD.md
**Selected Milestone:** MVP (6-8 weeks, P0 functions F-01~F-04)
**Complexity:** Large (greenfield, multi-module, external API integration)

## Summary

Build the MVP of a photography weather assistant: a map-centric web application where photographers save shooting locations, view photography-specific weather metrics (cloud layers, golden/blue hour, visibility, sunrise/sunset quality), and receive push notifications when conditions at saved spots are favorable. Backend in Java 21 + Spring Boot 3.2 (REST API); frontend as a React + TypeScript SPA with Leaflet maps; PostgreSQL + PostGIS for geospatial storage.

## Architecture Decision

The PRD leaves the target platform open. Given the emphasis on map interaction, mobile usage ("智能手机为主"), and push notifications:

| Option | Pros | Cons | Verdict |
|--------|------|------|---------|
| React SPA + Spring Boot API | Best mobile UX (PWA), rich map interaction, modern DX | Two build pipelines, CORS complexity | **Selected** |
| Spring Boot + Thymeleaf | Single project, simpler deploy | Poor mobile UX, limited map interactivity | Rejected |
| React Native mobile app | True native push, GPS | 3x development cost, separate backend needed | V2 |

**Final stack:**
- **Backend:** Java 21, Spring Boot 3.2+, Spring Data JPA, Spring Scheduling
- **Frontend:** React 18, TypeScript, Vite, Leaflet + react-leaflet, PWA
- **Database:** PostgreSQL 16 + PostGIS (geospatial extension)
- **Cache:** Caffeine (in-memory, MVP) → Redis (V1.0 scale)
- **Push:** Web Push API (service worker based)
- **Weather APIs:** 和风天气 (primary, free 50k calls/month) — general weather + AQI + astronomy
- **Sun Calculations:** commons-suncalc library (pure math, no API) — sunrise/sunset, golden hour, blue hour
- **Photography Index:** self-built scoring algorithm using cloud layers, humidity, visibility, AQI, wind
- **Backup:** 7Timer! astro endpoint (free, no key) — optional supplementary cloud/transparency data

## Patterns to Mirror

| Category | Source | Pattern |
|----------|--------|---------|
| Naming | `pom.xml` | Group: `com.weather`, packages by feature: `com.weather.{controller,service,repository,model,dto}` |
| Build | `pom.xml:1` | Maven wrapper, Java 17, JUnit 5 + Mockito, JaCoCo 80% enforcement |
| Errors | (none yet) | Domain exceptions extending RuntimeException, mapped to HTTP status via `@ControllerAdvice` |
| Tests | `src/test/java/com/weather/AppTest.java` | JUnit 5 `@Test` + `@DisplayName`, AAA pattern, mirror main package structure |
| Security | `.claude/settings.json` PreToolUse hook | No hardcoded secrets, API keys from env vars |
| CI | `.github/workflows/ci.yml` | Maven compile → test → verify pipeline, JDK 17 Temurin |

**Note:** This is a greenfield project with no existing application code to mirror. Patterns above are based on project config and Java/Spring Boot conventions. No existing code patterns to follow — we set the conventions.

## Files to Change

### Backend (`src/main/java/com/weather/`)

| File | Action | Why |
|------|--------|-----|
| `WeatherApplication.java` | CREATE | Spring Boot entry point |
| `config/WeatherApiConfig.java` | CREATE | 和风天气 + SunsetWx API client configuration |
| `config/SecurityConfig.java` | CREATE | CORS, CSRF, auth placeholder (MVP: simple API key) |
| `config/CacheConfig.java` | CREATE | Caffeine cache for weather data (TTL-based) |
| `model/PhotoSpot.java` | CREATE | JPA entity: id, userId, name, lat, lng, tags, notes, createdAt |
| `model/AlertRule.java` | CREATE | JPA entity: id, spotId, alertType, thresholds JSON, enabled |
| `model/WeatherSnapshot.java` | CREATE | JPA entity: id, spotId, timestamp, weather data JSON blob |
| `dto/PhotoSpotRequest.java` | CREATE | Record: Create/update spot request DTO |
| `dto/PhotoSpotResponse.java` | CREATE | Record: Spot response with current weather summary |
| `dto/WeatherDashboard.java` | CREATE | Record: Aggregated photography weather for a spot |
| `dto/AlertRuleRequest.java` | CREATE | Record: Alert rule creation/update |
| `repository/PhotoSpotRepository.java` | CREATE | JPA repository with PostGIS spatial queries (find near, within bounds) |
| `repository/AlertRuleRepository.java` | CREATE | JPA repository |
| `repository/WeatherSnapshotRepository.java` | CREATE | JPA repository |
| `service/PhotoSpotService.java` | CREATE | CRUD operations for photo spots |
| `service/WeatherService.java` | CREATE | Orchestrates 和风天气 API calls, computes photography index via PhotographyIndexCalculator |
| `service/AlertService.java` | CREATE | Evaluates alert rules against weather data, triggers notifications |
| `service/SunCalcService.java` | CREATE | Wraps commons-suncalc library for golden/blue hour, twilight calculations |
| `service/PhotographyIndexCalculator.java` | CREATE | Algorithmic scoring from cloud layers, humidity, visibility, AQI, wind |
| `controller/PhotoSpotController.java` | CREATE | REST: CRUD spots, list by bounds, search |
| `controller/WeatherController.java` | CREATE | REST: Get dashboard for spot, 7-day forecast |
| `controller/AlertController.java` | CREATE | REST: CRUD alert rules, trigger test |
| `controller/HealthController.java` | CREATE | Health check + API status |
| `scheduler/AlertScheduler.java` | CREATE | Spring `@Scheduled`: scan spots daily at 06:00 + 18:00, evaluate alerts |
| `scheduler/WeatherCacheScheduler.java` | CREATE | Spring `@Scheduled`: refresh weather cache hourly |
| `exception/SpotNotFoundException.java` | CREATE | Domain exception |
| `exception/WeatherApiException.java` | CREATE | API integration error |
| `advice/GlobalExceptionHandler.java` | CREATE | `@ControllerAdvice`: map exceptions to consistent error responses |

### Frontend (`src/main/frontend/`)

| File | Action | Why |
|------|--------|-----|
| `package.json` | CREATE | React 18, TypeScript, Vite, Leaflet, react-leaflet, Tailwind CSS |
| `vite.config.ts` | CREATE | Dev server proxy to Spring Boot backend |
| `index.html` | CREATE | SPA entry, PWA manifest link |
| `public/manifest.json` | CREATE | PWA manifest |
| `public/sw.js` | CREATE | Service worker for push notifications |
| `src/main.tsx` | CREATE | React entry point |
| `src/App.tsx` | CREATE | Router + layout shell |
| `src/pages/HomePage.tsx` | CREATE | Map view + spot list + photography index |
| `src/pages/SpotDetailPage.tsx` | CREATE | Weather dashboard + golden hour timeline + 7-day forecast |
| `src/pages/AlertManagePage.tsx` | CREATE | Alert rules list + create/edit form |
| `src/components/MapView.tsx` | CREATE | Leaflet map with spot markers, clustering |
| `src/components/SpotMarker.tsx` | CREATE | Custom map marker with photography index badge |
| `src/components/WeatherDashboard.tsx` | CREATE | 8+ metric cards with color coding |
| `src/components/GoldenHourTimeline.tsx` | CREATE | Horizontal timeline: sunrise → golden → solar → golden → sunset → blue |
| `src/components/SevenDayForecast.tsx` | CREATE | Horizontal scroll day cards with photography index |
| `src/components/AlertRuleForm.tsx` | CREATE | Threshold slider form |
| `src/hooks/useGeolocation.ts` | CREATE | Browser geolocation API hook |
| `src/hooks/useWeatherData.ts` | CREATE | SWR-based weather data fetching with cache |
| `src/hooks/usePushNotification.ts` | CREATE | Service worker push subscription management |
| `src/api/client.ts` | CREATE | Axios/fetch wrapper with base URL + error handling |
| `src/api/spots.ts` | CREATE | Photo spot API functions |
| `src/api/weather.ts` | CREATE | Weather API functions |
| `src/api/alerts.ts` | CREATE | Alert API functions |
| `src/types/spot.ts` | CREATE | TypeScript types: PhotoSpot, WeatherDashboard, AlertRule |
| `src/utils/photoIndexColor.ts` | CREATE | Color coding utility: green/yellow/red by score |

### Database

| File | Action | Why |
|------|--------|------|
| `src/main/resources/db/migration/V1__init.sql` | CREATE | Flyway migration: create tables + PostGIS extension |

### Config

| File | Action | Why |
|------|--------|------|
| `src/main/resources/application.yml` | CREATE | Spring Boot config: datasource, weather API keys (env vars), cache TTL |
| `src/main/resources/application-test.yml` | CREATE | Test profile: H2 in-memory, mock weather API |
| `docker-compose.yml` | CREATE | PostgreSQL + PostGIS for local dev |
| `.env.example` | CREATE | Required env vars template (no real values) |

## Tasks

### Phase 1: Foundation (est. 3 days)

#### Task 1.1: Project Bootstrapping
- **Action:** Create Spring Boot project structure, add dependencies (Spring Web, JPA, Flyway, Validation, Actuator), configure `application.yml`
- **Mirror:** `pom.xml` — Java 17, same JUnit/Mockito/JaCoCo versions, `com.weather` package
- **Validate:** `mvn compile` passes with all dependencies resolved

#### Task 1.2: Database Setup
- **Action:** Write `docker-compose.yml` for PostgreSQL 16 + PostGIS, Flyway migration V1 with tables for `photo_spots`, `alert_rules`, `weather_snapshots`
- **Mirror:** N/A — greenfield schema design
- **Validate:** `docker-compose up -d && mvn flyway:migrate` succeeds, tables exist

#### Task 1.3: Domain Model & Repositories
- **Action:** Create JPA entities (`PhotoSpot`, `AlertRule`, `WeatherSnapshot`) and Spring Data repositories with PostGIS spatial queries
- **Mirror:** `pom.xml` dependency patterns, JPA conventions from `java-patterns` rules
- **Validate:** `mvn test` — repository integration tests pass with `@DataJpaTest`

### Phase 2: Weather API Integration (est. 3 days)

#### Task 2.1: Weather API Client
- **Action:** Implement `WeatherApiConfig` (RestTemplate/WebClient beans), `WeatherService` calling 和风天气 API, caching with Caffeine
- **Mirror:** Error handling → domain exceptions (`WeatherApiException`)
- **Validate:** Unit tests pass with mocked API responses, integration test with test profile hits real API

#### Task 2.2: Sun Calculator Service
- **Action:** Implement `SunCalcService` wrapping the commons-suncalc library — computes sunrise/sunset, golden hour (morning/evening), blue hour (morning/evening), solar noon for any location+date. Pure math, no API dependency, no rate limits.
- **Mirror:** Immutable patterns → return `SolarTimes` record, never mutate
- **Validate:** Compare calculated times against 和风天气天文 API (±2 minute tolerance)

#### Task 2.3: Photography Index Algorithm
- **Action:** Implement `PhotographyIndexCalculator` — weighted scoring model using weather data from 和风天气:
  - Cloud layers (30%): high clouds ideal 30-70%, low cloud penalty
  - Humidity (20%): optimal 40-70% for Mie scattering
  - Visibility (20%): ideal >15km
  - AQI (15%): ideal <50
  - Wind (15%): ideal 0-15 km/h, light rain bonus
  - Output: 0-100 photography index + glow probability + per-factor breakdown
- **Mirror:** Immutable data → all inputs immutable, return new result
- **Validate:** Unit tests covering edge cases (all poor, all ideal, mixed, missing data)

### Phase 3: REST API (est. 2 days)

#### Task 3.1: Photo Spot API
- **Action:** `PhotoSpotController` + `PhotoSpotService` — CRUD for spots, bounds query (map viewport), search by name
- **Mirror:** DTOs use Java `record`, input validation with `jakarta.validation`
- **Validate:** `MockMvc` integration tests for all endpoints, validation error responses

#### Task 3.2: Weather API
- **Action:** `WeatherController` — GET dashboard for spot (current + today), GET 7-day forecast, GET nearby spots summary
- **Mirror:** `GlobalExceptionHandler` maps `SpotNotFoundException` → 404, `WeatherApiException` → 502
- **Validate:** Controller tests with mocked service layer

#### Task 3.3: Alert Rule API
- **Action:** `AlertController` + `AlertService` — CRUD alert rules per spot, test trigger endpoint
- **Mirror:** Same DTO pattern as spots
- **Validate:** Controller tests, validation of threshold ranges

#### Task 3.4: Global Error Handling
- **Action:** `@ControllerAdvice` — consistent error response envelope `{"success":false, "data":null, "error":"message"}`
- **Mirror:** `common/patterns.md` API Response Format
- **Validate:** Tests verify all exception types mapped correctly

### Phase 4: Alert Scheduler (est. 1.5 days)

#### Task 4.1: Scheduled Alert Evaluation
- **Action:** `AlertScheduler` — `@Scheduled(cron="0 0 6,18 * * *")` scans all enabled rules, fetches latest weather for associated spots, evaluates thresholds, records matches
- **Mirror:** Spring Boot `@Scheduled` convention, `@Transactional` on evaluation
- **Validate:** Integration test with fixed clock, pre-seeded weather data, verifies alerts fire correctly

#### Task 4.2: Push Notification Dispatch
- **Action:** Prepare push payload (spot name, condition summary), enqueue for dispatch (MVP: log + stored notification; V1.0: actual Web Push)
- **Mirror:** Error handling — failed dispatch logs warning, does not block other alerts
- **Validate:** Unit test verifies correct payload structure

### Phase 5: Frontend (est. 4 days)

#### Task 5.1: Project Setup
- **Action:** Vite + React + TypeScript + Tailwind CSS scaffolding, Vite proxy to backend, PWA manifest
- **Mirror:** `web/coding-style.md` — feature-based folder organization
- **Validate:** `npm run dev` serves app, API proxy works

#### Task 5.2: Map View with Spots
- **Action:** `MapView` with Leaflet, custom markers, clustering, click-to-add-spot flow, bottom sheet spot cards
- **Mirror:** `web/patterns.md` — container/presentational split
- **Validate:** Manual browser test: map renders, markers visible, click flow works

#### Task 5.3: Weather Dashboard
- **Action:** `SpotDetailPage` with metric card grid, golden hour timeline, 7-day forecast carousel, color-coded photography index
- **Mirror:** `web/design-quality.md` — 4+ required qualities (hierarchy, color semantics, hover states, depth)
- **Validate:** Visual regression: dashboard renders correctly with mock data

#### Task 5.4: Alert Management UI
- **Action:** `AlertManagePage` with rule list, create/edit form with threshold sliders, enable/disable toggle
- **Mirror:** `web/coding-style.md` — semantic HTML (`<form>`, `<label>`, `<fieldset>`)
- **Validate:** Form submission creates/updates alert via API

#### Task 5.5: PWA + Push Setup
- **Action:** Service worker registration, push subscription flow, notification permission request
- **Mirror:** `web/security.md` — CSP headers, HTTPS-only
- **Validate:** Lighthouse PWA audit score ≥ 90

### Phase 6: Integration & Polish (est. 1.5 days)

#### Task 6.1: End-to-End Flow Testing
- **Action:** Run through complete user journey: open app → add spot → view weather → set alert → receive notification
- **Mirror:** `e2e-testing` skill — critical user flow
- **Validate:** All P0 acceptance criteria from PRD satisfied

#### Task 6.2: Performance Validation
- **Action:** Verify cold start < 2s, weather load < 3s, map < 100 markers < 1s
- **Mirror:** `web/performance.md` — CWV targets
- **Validate:** Lighthouse report, backend response time under load

## Database Schema

```sql
-- PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Core table: photography spots
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

-- Alert rules per spot
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

-- Weather snapshots (cached API responses)
CREATE TABLE weather_snapshots (
    id          BIGSERIAL PRIMARY KEY,
    spot_id     BIGINT REFERENCES photo_spots(id) ON DELETE CASCADE,
    fetched_at  TIMESTAMPTZ DEFAULT NOW(),
    forecast_for TIMESTAMPTZ NOT NULL,
    data        JSONB NOT NULL,
    source      VARCHAR(64)
);

CREATE INDEX idx_weather_snapshots_spot_time
    ON weather_snapshots(spot_id, forecast_for DESC);

-- Alert history
CREATE TABLE alert_history (
    id          BIGSERIAL PRIMARY KEY,
    rule_id     BIGINT REFERENCES alert_rules(id) ON DELETE SET NULL,
    spot_id     BIGINT REFERENCES photo_spots(id) ON DELETE CASCADE,
    triggered_at TIMESTAMPTZ DEFAULT NOW(),
    weather_snapshot_id BIGINT REFERENCES weather_snapshots(id),
    score       INTEGER,
    sent        BOOLEAN DEFAULT FALSE
);
```

## API Endpoints (MVP)

```
GET    /api/health                          # Health check
GET    /api/spots                           # List user's spots (?bounds=sw_lat,sw_lng,ne_lat,ne_lng)
POST   /api/spots                           # Create spot
GET    /api/spots/{id}                      # Get spot with current weather summary
PUT    /api/spots/{id}                      # Update spot
DELETE /api/spots/{id}                      # Delete spot
GET    /api/spots/{id}/weather              # Full photography weather dashboard
GET    /api/spots/{id}/weather/forecast     # 7-day daily forecast
GET    /api/spots/{id}/alerts               # List alert rules for spot
POST   /api/spots/{id}/alerts               # Create alert rule
PUT    /api/alerts/{id}                     # Update alert rule
DELETE /api/alerts/{id}                     # Delete alert rule
GET    /api/alerts/{id}/history             # Alert trigger history
POST   /api/alerts/{id}/test                # Test-trigger an alert rule
```

## Validation

```bash
# Backend
mvn compile                                    # Compiles all Java sources
mvn test                                       # All unit + integration tests
mvn verify                                     # Full verify (tests + JaCoCo 80% + OWASP dep check)

# Frontend
cd src/main/frontend && npm run build          # Production build
cd src/main/frontend && npm run lint           # ESLint + TypeScript check

# Integration
docker-compose up -d                           # Start PostgreSQL
mvn spring-boot:run                            # Start backend on :8080
cd src/main/frontend && npm run dev            # Start frontend on :5173
# Manual: open http://localhost:5173, verify full user flow
```

## Risks

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| 和风天气 API free tier rate limit exceeded | Low | Caffeine cache TTL=30min reduces calls; switch to paid tier at scale |
| 摄影指数 prediction accuracy (self-built algorithm) | Medium | Show per-factor score breakdown; allow user feedback to calibrate weights |
| Web Push API not supported on iOS Safari | High | PWA push limited on iOS; document as known limitation, native app in V2 |
| PostGIS learning curve | Low | Simple queries (ST_DWithin, ST_MakePoint) well-documented |
| Frontend + Backend monorepo complexity | Medium | Single Maven build; frontend served as static resources in production |
| 7Timer! HTTP (not HTTPS) — mixed content in browsers | Low | Backend proxies 7Timer! requests; optional backup, disabled by default |

## Acceptance

- [ ] All 6 phases complete
- [ ] `mvn verify` passes (JaCoCo ≥ 80%)
- [ ] User can: add spot → view weather dashboard → set alert → receive notification
- [ ] Map renders with spot markers, click navigates to detail
- [ ] Weather dashboard shows ≥ 8 photography metrics
- [ ] Alert scheduler fires at correct times with correct thresholds
- [ ] Photography index color-coded (green/yellow/red)
- [ ] Patterns mirrored: DTO records, domain exceptions, error envelope, AAA tests
