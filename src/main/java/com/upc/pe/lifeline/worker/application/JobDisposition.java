package com.upc.pe.lifeline.worker.application;

public sealed interface JobDisposition permits JobDisposition.Completed, JobDisposition.Retry, JobDisposition.Discard {

	record Completed() implements JobDisposition {
	}

	record Retry(String reason) implements JobDisposition {
	}

	record Discard(String reason) implements JobDisposition {
	}
}
