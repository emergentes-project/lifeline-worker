package com.upc.pe.lifeline.worker.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.boot.jms.ConnectionFactoryUnwrapper;
import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration(proxyBeanMethods = false)
public class ArtemisListenerConfig {

	@Bean
	DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
			DefaultJmsListenerContainerFactoryConfigurer configurer,
			ConnectionFactory connectionFactory,
			WorkerProperties properties) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configurer.configure(factory, ConnectionFactoryUnwrapper.unwrapCaching(connectionFactory));
		factory.setConcurrency(properties.concurrency() + "-" + properties.maxConcurrency());
		return factory;
	}
}
