package com.upc.pe.lifeline.worker.application;

public class JobRetryException extends RuntimeException {

	public JobRetryException(String reason) {
		super(reason);
	}
}
