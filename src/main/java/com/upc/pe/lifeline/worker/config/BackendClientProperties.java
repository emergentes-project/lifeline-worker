package com.upc.pe.lifeline.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "lifeline.worker")
public record BackendClientProperties(
		String backendUrl,
		Duration connectTimeout,
		Duration readTimeout,
		String jwtSecret,
		String jwtIssuer,
		Duration jwtExpiration) {

	public BackendClientProperties {
		if (backendUrl == null || backendUrl.isBlank()) {
			throw new IllegalArgumentException("lifeline.worker.backend-url is required");
		}
		if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()) {
			throw new IllegalArgumentException("lifeline.worker.connect-timeout must be positive");
		}
		if (readTimeout == null || readTimeout.isZero() || readTimeout.isNegative()) {
			throw new IllegalArgumentException("lifeline.worker.read-timeout must be positive");
		}
		if (jwtSecret == null || jwtSecret.isBlank()) {
			throw new IllegalArgumentException("lifeline.worker.jwt-secret is required");
		}
		if (jwtIssuer == null || jwtIssuer.isBlank()) {
			throw new IllegalArgumentException("lifeline.worker.jwt-issuer is required");
		}
		if (jwtExpiration == null || jwtExpiration.isZero() || jwtExpiration.isNegative()) {
			throw new IllegalArgumentException("lifeline.worker.jwt-expiration must be positive");
		}
	}
}
