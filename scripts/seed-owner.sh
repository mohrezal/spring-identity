#!/bin/bash

set -euo pipefail

./mvnw clean spring-boot:run -Dspring-boot.run.profiles=seed
