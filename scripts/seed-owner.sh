#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")/.."

if [[ -f .env ]]; then
  set -a
  source .env
  set +a
fi

./mvnw -pl services/identity-api -am install \
    -q \
    -Dmaven.test.skip=true \
    -Dcheckstyle.skip \
    -Dspotless.check.skip=true \
    -Dmaven.antrun.skip=true

./mvnw -pl services/identity-api spring-boot:run \
    -q \
    -Dmaven.test.skip=true \
    -Dcheckstyle.skip \
    -Dspotless.check.skip=true \
    -Dmaven.antrun.skip=true \
    -Dspring-boot.run.profiles=seed,seed-owner \
    -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false" \
    -Dspring-boot.run.arguments="--spring.main.web-application-type=none --spring.main.lazy-initialization=true --spring.devtools.add-properties=false --spring.liquibase.enabled=false --spring.data.redis.repositories.enabled=false"
