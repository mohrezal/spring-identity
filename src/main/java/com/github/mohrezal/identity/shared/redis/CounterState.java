package com.github.mohrezal.identity.shared.redis;

public record CounterState(int value, int ttlSeconds) {}
