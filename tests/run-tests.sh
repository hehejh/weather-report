#!/bin/bash
# Weather project test entrypoint
set -e
cd "$(dirname "$0")/.."
mvn test --batch-mode
