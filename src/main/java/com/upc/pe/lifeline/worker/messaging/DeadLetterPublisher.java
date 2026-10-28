package com.upc.pe.lifeline.worker.messaging;

import com.upc.pe.lifeline.worker.application.JobRetryException;
import com.upc.pe.lifeline.worker.config.WorkerProperties;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterPublisher {

	static final String REASON_PROPERTY = "dlqReason";

	private final String destination;

	public DeadLetterPublisher(WorkerProperties properties) {
		this.destination = properties.deadLetterDestination();
	}

	public void publish(Session session, String body, String reason) {
		try {
			TextMessage message = session.createTextMessage(body);
			message.setStringProperty(REASON_PROPERTY, reason);
			try (MessageProducer producer = session.createProducer(session.createQueue(destination))) {
				producer.send(message);
			}
		}
		catch (JMSException exception) {
			throw new JobRetryException("No se pudo publicar en " + destination);
		}
	}
}
