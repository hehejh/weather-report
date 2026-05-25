# Smoke Eval

Application health and integration checks.

## Test 1: App compiles and tests pass
```bash
./test.sh
```

Expected: exit code 0, all tests pass, coverage >= 80%.

## Test 2: Frontend builds
```bash
cd src/main/frontend && npm ci && npm run build
```

Expected: tsc passes, vite build succeeds, output in dist/.

## Test 3: E2E API flow
```bash
# Requires: backend running on :8080, DB available
./evals/e2e-api.sh
```

Expected: all API endpoints respond, CRUD flow works, error handling correct.

## P0 Acceptance Criteria

| Criterion | Check |
|-----------|-------|
| F-01: Map spot CRUD (create/read/update/delete/list/search) | e2e-api.sh |
| F-02: Weather dashboard (8+ metrics, golden hour, forecast) | e2e-api.sh |
| F-03: Alert rules (CRUD, test trigger, history) | e2e-api.sh |
| F-04: Cold start < 2s, frontend build succeeds | npm run build |
| 80%+ test coverage (backend) | mvn test (JaCoCo) |
| 3-click flow: select spot → view weather → set alert | e2e-api.sh (sequential) |
