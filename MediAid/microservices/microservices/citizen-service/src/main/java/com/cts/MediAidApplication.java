package com.cts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cts.client")
@EnableMethodSecurity(prePostEnabled = true)
public class MediAidApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediAidApplication.class, args);
	}

	
}
