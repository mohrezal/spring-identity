#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

FLAGS=(
  -DskipTests
  -Dcheckstyle.skip=true
  -Dspotless.check.skip=true
  -Dmaven.antrun.skip=true
  -B
)

echo "==> Building identity-api..."
./mvnw clean package -pl services/identity-api -am "${FLAGS[@]}"

echo "==> Building audit-worker..."
./mvnw clean package -pl workers/audit-worker -am "${FLAGS[@]}"

echo "==> Starting services..."
docker compose up -d --build

echo ""
echo "  identity-api  → http://localhost:8080"
echo "  audit-worker  → http://localhost:8081"
echo "  prometheus    → http://localhost:9090"
echo "  loki          → http://localhost:3100"
echo "  grafana       → http://localhost:3000"
