package com.cts.claim_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ClaimServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(ClaimServiceApplication.class);

	public static void main(String[] args) {
		// The config-server's claim-service.properties sets
		// spring.cloud.openfeign.circuitbreaker.enabled=true, which makes Spring Cloud
		// OpenFeign wrap every Feign call in a circuit breaker whose TimeLimiter defaults
		// to 1 second. Java system properties take precedence over config-server imports,
		// so setting this here authoritatively disables the redundant wrapper.
		System.setProperty("spring.cloud.openfeign.circuitbreaker.enabled", "false");
		log.warn("[ClaimService-BOOT-MARKER-v2] System property override applied: " +
				"spring.cloud.openfeign.circuitbreaker.enabled={}",
				System.getProperty("spring.cloud.openfeign.circuitbreaker.enabled"));
		SpringApplication.run(ClaimServiceApplication.class, args);
	}

}
