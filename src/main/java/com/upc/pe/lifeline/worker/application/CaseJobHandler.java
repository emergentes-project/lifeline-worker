package com.upc.pe.lifeline.worker.application;

import com.upc.pe.lifeline.worker.messaging.CaseJobMessage;

public interface CaseJobHandler {

	JobDisposition handle(CaseJobMessage message, int deliveryCount);
}
