package com.upc.pe.lifeline.worker.application;

public class JobDiscardException extends RuntimeException {

	public JobDiscardException(String reason) {
		super(reason);
	}
}
