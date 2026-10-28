package com.upc.pe.lifeline.worker.messaging;

import java.util.UUID;

public interface CaseJobClient {

	int post(UUID jobId);
}
