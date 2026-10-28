package com.upc.pe.lifeline.worker.messaging;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseJobMessageReaderTest {

	private final CaseJobMessageReader reader = new CaseJobMessageReader(new ObjectMapper());

	@Test
	void readsTheAgreedJobFields() {
		UUID jobId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID aggregateId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		UUID idempotencyKey = UUID.fromString("33333333-3333-3333-3333-333333333333");
		String body = """
				{
				  "jobId": "%s",
				  "eventType": "consultation.received",
				  "aggregateId": "%s",
				  "idempotencyKey": "%s",
				  "extra": "ignored"
				}
				""".formatted(jobId, aggregateId, idempotencyKey);

		CaseJobMessage message = reader.read(body);

		assertThat(message.jobId()).isEqualTo(jobId);
		assertThat(message.eventType()).isEqualTo("consultation.received");
		assertThat(message.aggregateId()).isEqualTo(aggregateId);
		assertThat(message.idempotencyKey()).isEqualTo(idempotencyKey);
	}

	@Test
	void rejectsABodyThatIsNotTheJobContract() {
		assertThatThrownBy(() -> reader.read(" ")).isInstanceOf(JobMessageException.class);
		assertThatThrownBy(() -> reader.read("{")).isInstanceOf(JobMessageException.class);
		assertThatThrownBy(() -> reader.read("{\"eventType\":\"consultation.received\"}"))
				.isInstanceOf(JobMessageException.class);
	}
}
