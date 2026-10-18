package com.alquilereventos.seguridad.config.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
@EnableConfigurationProperties(SecurityCacheProperties.class)
public class SecurityCacheConfig {
    @Bean @Primary
    @ConditionalOnProperty(prefix = "app.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    CacheManager redisCacheManager(RedisConnectionFactory connectionFactory, SecurityCacheProperties properties) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)));
        CacheManager manager = RedisCacheManager.builder(connectionFactory).cacheDefaults(base)
                .withCacheConfiguration("seguridad:usuarios", base.entryTtl(Duration.ofSeconds(properties.getUsuariosTtlSeconds())))
                .withCacheConfiguration("seguridad:roles", base).build();
        return new LoggingSecurityCacheManager(manager);
    }
    @Bean @Primary
    @ConditionalOnProperty(prefix = "app.cache", name = "enabled", havingValue = "false")
    CacheManager noOpCacheManager() { return new NoOpCacheManager(); }
}
