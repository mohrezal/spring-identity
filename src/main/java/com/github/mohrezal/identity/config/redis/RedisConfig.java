package com.github.mohrezal.identity.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    RedisTemplate<String, String> redisTemplate() {
        var template = new RedisTemplate<String, String>();
        var serializer = RedisSerializer.string();

        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }
}
