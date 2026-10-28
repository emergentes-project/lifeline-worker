package com.upc.pe.lifeline.worker.messaging;

import com.upc.pe.lifeline.worker.application.CaseJobProcessor;
import com.upc.pe.lifeline.worker.application.JobDiscardException;
import com.upc.pe.lifeline.worker.application.JobRetryException;
import com.upc.pe.lifeline.worker.config.WorkerProperties;
import jakarta.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class CaseJobListener {

	private static final Logger log = LoggerFactory.getLogger(CaseJobListener.class);

	private final CaseJobMessageReader reader;
	private final CaseJobProcessor processor;
	private final DeadLetterPublisher deadLetters;
	private final String deadLetterDestination;

	public CaseJobListener(CaseJobMessageReader reader, CaseJobProcessor processor,
			DeadLetterPublisher deadLetters, WorkerProperties properties) {
		this.reader = reader;
		this.processor = processor;
		this.deadLetters = deadLetters;
		this.deadLetterDestination = properties.deadLetterDestination();
	}

	@JmsListener(destination = "${lifeline.worker.destination}")
	public void onJob(String body, Session session,
			@Header(name = "JMSXDeliveryCount", required = false) Integer deliveryCount) {
		int attempt = deliveryCount == null ? 1 : deliveryCount;
		try {
			processor.process(reader.read(body), attempt);
		}
		catch (JobMessageException exception) {
			log.warn("El mensaje no se puede leer y pasa a {}: {}", deadLetterDestination, exception.getMessage());
			deadLetters.publish(session, body, exception.getMessage());
		}
		catch (JobDiscardException exception) {
			log.warn("El trabajo pasa a {}: {}", deadLetterDestination, exception.getMessage());
			deadLetters.publish(session, body, exception.getMessage());
		}
		catch (JobRetryException exception) {
			log.warn("El trabajo vuelve a la cola: {}", exception.getMessage());
			throw exception;
		}
	}
}
