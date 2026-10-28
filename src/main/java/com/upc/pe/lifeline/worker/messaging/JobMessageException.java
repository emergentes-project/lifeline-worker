package com.upc.pe.lifeline.worker.messaging;

public class JobMessageException extends RuntimeException {

	public JobMessageException(String message) {
		super(message);
	}

	public JobMessageException(String message, Throwable cause) {
		super(message, cause);
	}
}
