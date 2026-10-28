package com.upc.pe.lifeline.worker.application;

import com.upc.pe.lifeline.worker.config.WorkerProperties;
import com.upc.pe.lifeline.worker.messaging.CaseJobMessage;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseJobProcessorTest {

	private final CaseJobMessage message = new CaseJobMessage(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"consultation.received",
			UUID.fromString("22222222-2222-2222-2222-222222222222"),
			UUID.fromString("33333333-3333-3333-3333-333333333333"));

	@Test
	void completesWithoutWaitingWhenTheHandlerAcceptsTheJob() {
		CaseJobProcessor processor = processor(new JobDisposition.Completed(), Duration.ZERO);

		assertThatCode(() -> processor.process(message, 1)).doesNotThrowAnyException();
	}

	@Test
	void returnsTheMessageToTheQueueWhenTheHandlerAsksForARetry() {
		CaseJobProcessor processor = processor(new JobDisposition.Retry("el backend no respondió"), Duration.ZERO);

		assertThatThrownBy(() -> processor.process(message, 2))
				.isInstanceOf(JobRetryException.class)
				.hasMessage("el backend no respondió");
	}

	@Test
	void discardsAnInvalidOutcomeForTheDeadLetterQueue() {
		CaseJobProcessor processor = processor(new JobDisposition.Discard("contrato inválido"), Duration.ZERO);

		assertThatThrownBy(() -> processor.process(message, 1))
				.isInstanceOf(JobDiscardException.class)
				.hasMessage("contrato inválido");
	}

	private static CaseJobProcessor processor(JobDisposition disposition, Duration pauseBeforeAck) {
		CaseJobHandler handler = (message, deliveryCount) -> disposition;
		WorkerProperties properties = new WorkerProperties(
				"lifeline.case.jobs", "lifeline.case.jobs.dlq", pauseBeforeAck, 1, 1);
		return new CaseJobProcessor(handler, properties);
	}
}
