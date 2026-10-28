package com.upc.pe.lifeline.worker.application;

import com.upc.pe.lifeline.worker.messaging.CaseJobClient;
import com.upc.pe.lifeline.worker.messaging.CaseJobMessage;
import org.springframework.stereotype.Component;

@Component
public class RemoteCaseJobHandler implements CaseJobHandler {

	private final CaseJobClient client;

	public RemoteCaseJobHandler(CaseJobClient client) {
		this.client = client;
	}

	@Override
	public JobDisposition handle(CaseJobMessage message, int deliveryCount) {
		try {
			int status = client.post(message.jobId());
			if (status == 201 || status == 200) {
				return new JobDisposition.Completed();
			}
			if (status == 404) {
				return new JobDisposition.Discard("El trabajo no existe");
			}
			return new JobDisposition.Retry("El backend respondió " + status);
		}
		catch (RuntimeException exception) {
			String reason = exception.getMessage() == null ? "El backend no respondió" : exception.getMessage();
			return new JobDisposition.Retry(reason);
		}
	}
}
