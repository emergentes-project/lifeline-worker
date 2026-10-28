package com.upc.pe.lifeline.worker.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CaseJobMessage(
		@JsonProperty("jobId") UUID jobId,
		@JsonProperty("eventType") String eventType,
		@JsonProperty("aggregateId") UUID aggregateId,
		@JsonProperty("idempotencyKey") UUID idempotencyKey) {
}
