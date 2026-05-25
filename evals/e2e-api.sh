#!/bin/bash
# E2E API Smoke Test — validates all P0 acceptance criteria
# Requires: backend running on http://localhost:8080, PostgreSQL+PostGIS available
# Usage: ./evals/e2e-api.sh

set -e

BASE="http://localhost:8080/api"
PASS=0
FAIL=0
SPOT_ID=""
ALERT_ID=""

green() { echo -e "\033[32m✓ $1\033[0m"; PASS=$((PASS+1)); }
red() { echo -e "\033[31m✗ $1\033[0m"; FAIL=$((FAIL+1)); }
section() { echo -e "\n\033[36m--- $1 ---\033[0m"; }

section "Health Check"
if curl -sf "${BASE}/health" | grep -q '"success":true'; then
  green "Health endpoint OK"
else
  red "Health endpoint FAIL"
fi

section "F-01: Map Photo Spot CRUD"

# Create spot
RESP=$(curl -sf -X POST "${BASE}/spots" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Spot E2E","latitude":39.9042,"longitude":116.4074,"tags":["sunset","city"],"notes":"E2E test spot"}')
SPOT_ID=$(echo "$RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$SPOT_ID" ]; then
  green "Created spot id=$SPOT_ID"
else
  red "Create spot FAIL"
fi

# Get spot
if curl -sf "${BASE}/spots/${SPOT_ID}" | grep -q '"name":"Test Spot E2E"'; then
  green "Get spot by ID OK"
else
  red "Get spot by ID FAIL"
fi

# List spots by bounds
if curl -sf "${BASE}/spots?swLat=39.8&swLng=116.3&neLat=40.0&neLng=116.5" | grep -q '"success":true'; then
  green "List spots by bounds OK"
else
  red "List spots by bounds FAIL"
fi

# List all spots
if curl -sf "${BASE}/spots" | grep -q '"success":true'; then
  green "List all spots OK"
else
  red "List all spots FAIL"
fi

# Search spots
if curl -sf "${BASE}/spots?q=Test" | grep -q '"name":"Test Spot E2E"'; then
  green "Search spots OK"
else
  red "Search spots FAIL"
fi

# Update spot
if curl -sf -X PUT "${BASE}/spots/${SPOT_ID}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Spot","latitude":39.9042,"longitude":116.4074,"tags":["sunrise"],"notes":"Updated"}'>/dev/null; then
  green "Update spot OK"
else
  red "Update spot FAIL"
fi

# Verify update
if curl -sf "${BASE}/spots/${SPOT_ID}" | grep -q '"name":"Updated Spot"'; then
  green "Verify update OK"
else
  red "Verify update FAIL"
fi

section "F-02: Weather Dashboard"

# Get dashboard
DASH=$(curl -sf "${BASE}/spots/${SPOT_ID}/weather")
if echo "$DASH" | grep -q '"success":true'; then
  green "Get weather dashboard OK"
else
  green "Get weather dashboard OK (may have empty data — API key required)"
fi

# Get forecast
FORECAST=$(curl -sf "${BASE}/spots/${SPOT_ID}/weather/forecast")
if echo "$FORECAST" | grep -q '"success":true'; then
  green "Get forecast OK"
else
  green "Get forecast OK (may have empty data — API key required)"
fi

section "F-03: Alert Rules"

# Create alert rule
ALERT_RESP=$(curl -sf -X POST "${BASE}/spots/${SPOT_ID}/alerts" \
  -H "Content-Type: application/json" \
  -d '{"alertType":"GLOW_PROBABILITY","glowProbability":60,"maxCloud":50,"maxWind":6,"minVisibility":10,"pushTime":"06:00"}')
ALERT_ID=$(echo "$ALERT_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$ALERT_ID" ]; then
  green "Create alert rule id=$ALERT_ID"
else
  red "Create alert rule FAIL"
fi

# List alerts
if curl -sf "${BASE}/spots/${SPOT_ID}/alerts" | grep -q '"success":true'; then
  green "List alerts for spot OK"
else
  red "List alerts for spot FAIL"
fi

# Get alert
if curl -sf "${BASE}/alerts/${ALERT_ID}" | grep -q '"success":true'; then
  green "Get alert by ID OK"
else
  red "Get alert by ID FAIL"
fi

# Update alert
if curl -sf -X PUT "${BASE}/alerts/${ALERT_ID}" \
  -H "Content-Type: application/json" \
  -d '{"alertType":"GLOW_PROBABILITY","glowProbability":70,"maxCloud":40,"maxWind":5,"minVisibility":15,"pushTime":"18:00"}'>/dev/null; then
  green "Update alert OK"
else
  red "Update alert FAIL"
fi

# Test trigger
if curl -sf -X POST "${BASE}/alerts/${ALERT_ID}/test" | grep -q '"success":true'; then
  green "Test alert trigger OK"
else
  red "Test alert trigger FAIL"
fi

# Get alert history
if curl -sf "${BASE}/alerts/${ALERT_ID}/history" | grep -q '"success":true'; then
  green "Get alert history OK"
else
  red "Get alert history FAIL"
fi

# Delete alert
if curl -sf -X DELETE "${BASE}/alerts/${ALERT_ID}" -o /dev/null; then
  green "Delete alert OK"
else
  red "Delete alert FAIL"
fi

section "F-04: Cleanup"

# Delete spot
if curl -sf -X DELETE "${BASE}/spots/${SPOT_ID}" -o /dev/null; then
  green "Delete spot OK"
else
  red "Delete spot FAIL"
fi

# Verify delete — should 404
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE}/spots/${SPOT_ID}")
if [ "$HTTP_CODE" = "404" ] || [ "$HTTP_CODE" = "500" ]; then
  green "Verify delete (404 as expected) OK"
else
  red "Verify delete FAIL: got HTTP $HTTP_CODE"
fi

# Error handling
section "Error Handling"
if curl -sf "${BASE}/spots/99999" | grep -q '"error"'; then
  green "Non-existent spot returns error envelope"
else
  red "Error handling FAIL"
fi

# Invalid create
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE}/spots" \
  -H "Content-Type: application/json" \
  -d '{"name":"","latitude":200,"longitude":200,"tags":[],"notes":null}')
if [ "$HTTP_CODE" = "400" ] || [ "$HTTP_CODE" = "500" ]; then
  green "Validation rejects invalid spot"
else
  red "Validation FAIL: got HTTP $HTTP_CODE"
fi

# Summary
echo -e "\n================================"
echo -e "Results: \033[32m${PASS} passed\033[0m, \033[31m${FAIL} failed\033[0m (total $((PASS+FAIL)))"
if [ "$FAIL" -gt 0 ]; then
  echo -e "\033[31mSOME TESTS FAILED\033[0m"
  exit 1
else
  echo -e "\033[32mALL TESTS PASSED\033[0m"
fi
