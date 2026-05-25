---
name: project-overview
description: Weather project overview — Java application tech stack, goals, and conventions
metadata:
  type: project
---

# Weather Project

Java 17+ application using Maven, JUnit 5, and Mockito.

**Tech Stack:** Java 17, Maven, JUnit 5, Mockito, JaCoCo
**Test Coverage:** 80% minimum (enforced by JaCoCo)
**CI:** GitHub Actions (`.github/workflows/ci.yml`)

## Conventions
- TDD workflow: write tests first
- Immutable patterns preferred
- Environment variables for secrets (never hardcoded)
