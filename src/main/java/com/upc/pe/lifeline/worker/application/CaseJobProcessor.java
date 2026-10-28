package com.upc.pe.lifeline.worker.application;

import com.upc.pe.lifeline.worker.config.WorkerProperties;
import com.upc.pe.lifeline.worker.messaging.CaseJobMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CaseJobProcessor {

	private static final Logger log = LoggerFactory.getLogger(CaseJobProcessor.class);

	private final CaseJobHandler handler;
	private final WorkerProperties properties;

	public CaseJobProcessor(CaseJobHandler handler, WorkerProperties properties) {
		this.handler = handler;
		this.properties = properties;
	}

	public void process(CaseJobMessage message, int deliveryCount) {
		switch (handler.handle(message, deliveryCount)) {
			case JobDisposition.Completed ignored -> pauseBeforeAck();
			case JobDisposition.Retry(String reason) -> throw new JobRetryException(reason);
			case JobDisposition.Discard(String reason) -> throw new JobDiscardException(reason);
		}
	}

	private void pauseBeforeAck() {
		if (properties.pauseBeforeAck().isZero()) {
			return;
		}
		log.info("Esperando {} antes de confirmar el mensaje", properties.pauseBeforeAck());
		try {
			Thread.sleep(properties.pauseBeforeAck());
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new JobRetryException("La espera antes de confirmar fue interrumpida");
		}
	}
}
