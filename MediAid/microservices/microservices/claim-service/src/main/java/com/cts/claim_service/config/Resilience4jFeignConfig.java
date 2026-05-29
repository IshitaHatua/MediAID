package com.cts.claim_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Overrides the default 1-second time limiter applied by
 * spring-cloud-starter-circuitbreaker-resilience4j to every Feign client method
 * (enabled by spring.cloud.openfeign.circuitbreaker.enabled=true in the config-server
 * claim-service.properties). The cold-start Feign calls to scheme-/enrollment-service
 * routinely exceed 1s, which cancels the call and surfaces the outer @CircuitBreaker
 * fallback with "An external service is currently unavailable."
 *
 * Configuring via Java overrides the default regardless of what the config-server
 * publishes for resilience4j.timelimiter.* (those only target named instances).
 */
@Configuration
public class Resilience4jFeignConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> feignDefaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(10))
                        .cancelRunningFuture(false)
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .build());
    }
}
