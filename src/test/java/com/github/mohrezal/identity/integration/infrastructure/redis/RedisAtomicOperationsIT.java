package com.github.mohrezal.identity.integration.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.redis.CounterState;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.support.IntegrationTestSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RedisAtomicOperationsIT extends IntegrationTestSupport {

    private static final int REQUEST_COUNT = 16;

    @Autowired
    private RedisService redisService;

    @Test
    void consume_allowsExactlyOneConcurrentConsumer() throws Exception {
        var state = UUID.randomUUID().toString();
        var value = "oauth-state-payload";
        redisService.set(RedisKey.OAUTH_STATE, value, state);
        var ready = new CountDownLatch(REQUEST_COUNT);
        var start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(REQUEST_COUNT)) {
            var consumers = new ArrayList<java.util.concurrent.Future<Optional<String>>>();
            IntStream.range(0, REQUEST_COUNT)
                    .forEach(
                            ignored ->
                                    consumers.add(
                                            executor.submit(
                                                    () -> {
                                                        ready.countDown();
                                                        start.await();
                                                        return redisService.consume(
                                                                RedisKey.OAUTH_STATE,
                                                                String.class,
                                                                state);
                                                    })));

            var allConsumersReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertThat(allConsumersReady).isTrue();

            var results = new ArrayList<Optional<String>>();
            for (var consumer : consumers) {
                results.add(consumer.get(10, TimeUnit.SECONDS));
            }

            assertThat(results).filteredOn(Optional::isPresent).hasSize(1);
            var consumedValues = results.stream().flatMap(Optional::stream).toList();
            assertThat(consumedValues).containsExactly(value);
            assertThat(redisService.get(RedisKey.OAUTH_STATE, String.class, state)).isEmpty();
        } finally {
            redisService.delete(RedisKey.OAUTH_STATE, state);
        }
    }

    @Test
    void increment_isAtomicAndDoesNotResetTheFixedWindow() throws Exception {
        var policy = "concurrent-policy";
        var subject = UUID.randomUUID().toString();
        var window = Duration.ofSeconds(10);
        var firstIncrement =
                redisService.increment(RedisKey.RATE_LIMIT_IP, window, policy, subject);
        Thread.sleep(Duration.ofMillis(1100));
        var ready = new CountDownLatch(REQUEST_COUNT);
        var start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(REQUEST_COUNT)) {
            var increments = new ArrayList<java.util.concurrent.Future<CounterState>>();
            IntStream.range(0, REQUEST_COUNT)
                    .forEach(
                            ignored ->
                                    increments.add(
                                            executor.submit(
                                                    () -> {
                                                        ready.countDown();
                                                        start.await();
                                                        return redisService.increment(
                                                                RedisKey.RATE_LIMIT_IP,
                                                                window,
                                                                policy,
                                                                subject);
                                                    })));

            var allIncrementsReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertThat(allIncrementsReady).isTrue();

            var results = new ArrayList<CounterState>();
            for (var increment : increments) {
                results.add(increment.get(10, TimeUnit.SECONDS));
            }

            assertThat(firstIncrement.value()).isOne();
            assertThat(firstIncrement.ttlSeconds()).isPositive();
            assertThat(results)
                    .extracting(CounterState::value)
                    .containsExactlyInAnyOrderElementsOf(
                            IntStream.rangeClosed(2, REQUEST_COUNT + 1).boxed().toList());
            assertThat(results)
                    .extracting(CounterState::ttlSeconds)
                    .allSatisfy(
                            ttl ->
                                    assertThat(ttl)
                                            .isPositive()
                                            .isLessThan(firstIncrement.ttlSeconds()));
        } finally {
            redisService.delete(RedisKey.RATE_LIMIT_IP, policy, subject);
        }
    }
}
