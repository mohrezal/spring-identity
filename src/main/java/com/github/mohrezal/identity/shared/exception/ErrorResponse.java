package com.github.mohrezal.identity.shared.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        String code, String message, Map<String, String> errors, Instant timestamp, String path) {}
