package com.upc.pe.lifeline.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "lifeline.worker")
public record WorkerProperties(
		String destination,
		String deadLetterDestination,
		Duration pauseBeforeAck,
		int concurrency,
		int maxConcurrency) {

	public WorkerProperties {
		if (destination == null || destination.isBlank()) {
			throw new IllegalArgumentException("lifeline.worker.destination is required");
		}
		if (deadLetterDestination == null || deadLetterDestination.isBlank()) {
			throw new IllegalArgumentException("lifeline.worker.dead-letter-destination is required");
		}
		if (deadLetterDestination.equals(destination)) {
			throw new IllegalArgumentException("lifeline.worker.dead-letter-destination must differ from the job queue");
		}
		if (pauseBeforeAck == null || pauseBeforeAck.isNegative()) {
			throw new IllegalArgumentException("lifeline.worker.pause-before-ack must be zero or positive");
		}
		if (concurrency < 1 || maxConcurrency < concurrency) {
			throw new IllegalArgumentException(
					"lifeline.worker.concurrency must be at least 1 and not greater than max-concurrency");
		}
	}
}
