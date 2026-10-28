package com.upc.pe.lifeline.worker.messaging;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class CaseJobMessageReader {

	private final ObjectMapper objectMapper;

	public CaseJobMessageReader(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public CaseJobMessage read(String body) {
		if (body == null || body.isBlank()) {
			throw new JobMessageException("El cuerpo del mensaje está vacío");
		}
		try {
			CaseJobMessage message = objectMapper.readValue(body, CaseJobMessage.class);
			if (message.jobId() == null
					|| message.aggregateId() == null
					|| message.idempotencyKey() == null
					|| message.eventType() == null
					|| message.eventType().isBlank()) {
				throw new JobMessageException(
						"El mensaje debe incluir jobId, eventType, aggregateId e idempotencyKey");
			}
			return message;
		}
		catch (JobMessageException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new JobMessageException("El mensaje no es un JSON de trabajo válido", exception);
		}
	}
}
