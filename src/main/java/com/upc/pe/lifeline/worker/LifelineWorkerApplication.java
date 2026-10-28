package com.upc.pe.lifeline.worker;

import com.upc.pe.lifeline.worker.config.BackendClientProperties;
import com.upc.pe.lifeline.worker.config.WorkerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
@EnableConfigurationProperties({WorkerProperties.class, BackendClientProperties.class})
public class LifelineWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifelineWorkerApplication.class, args);
	}

}
