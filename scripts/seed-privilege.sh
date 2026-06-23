#!/bin/bash

set -euo pipefail

./mvnw spring-boot:run \
    -q \
    -Dmaven.test.skip=true \
    -Dcheckstyle.skip \
    -Dspotless.check.skip=true \
    -Dmaven.antrun.skip=true \
    -Dspring-boot.run.profiles=seed,seed-privilege \
    -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false" \
    -Dspring-boot.run.arguments="--spring.main.web-application-type=none --spring.main.lazy-initialization=true --spring.devtools.add-properties=false --spring.liquibase.enabled=false --spring.data.redis.repositories.enabled=false"
