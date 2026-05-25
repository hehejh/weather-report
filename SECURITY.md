# Security Policy

## Secret Management
- Never hardcode secrets (API keys, passwords, tokens)
- Use environment variables for all credentials
- Validate required secrets at application startup

## Dependency Scanning
- Dependabot or Renovate enabled for automated updates
- Review dependency changes in PRs

## Code Review
- All PRs require review before merge
- Security-sensitive changes require additional scrutiny

## Reporting Vulnerabilities
Report security issues to the project maintainers.
