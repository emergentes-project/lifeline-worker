package com.upc.pe.lifeline.worker.messaging;

import com.upc.pe.lifeline.worker.config.BackendClientProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JdkCaseJobClient implements CaseJobClient {

	private final HttpClient httpClient;
	private final URI jobs;
	private final Duration readTimeout;
	private final SecretKey secretKey;
	private final String issuer;
	private final Duration jwtExpiration;

	@Autowired
	public JdkCaseJobClient(BackendClientProperties properties) {
		this(properties.backendUrl(), properties.connectTimeout(), properties.readTimeout(),
				properties.jwtSecret(), properties.jwtIssuer(), properties.jwtExpiration());
	}

	public JdkCaseJobClient(String backendUrl, Duration connectTimeout, Duration readTimeout,
			String jwtSecret, String issuer, Duration jwtExpiration) {
		this(backendUrl, readTimeout, jwtSecret, issuer, jwtExpiration,
				HttpClient.newBuilder().connectTimeout(connectTimeout).build());
	}

	JdkCaseJobClient(String backendUrl, Duration readTimeout, String jwtSecret, String issuer,
			Duration jwtExpiration, HttpClient httpClient) {
		this.httpClient = httpClient;
		this.jobs = URI.create(backendUrl.replaceAll("/+$", "") + "/api/v1/internal/case-jobs/");
		this.readTimeout = readTimeout;
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
		this.issuer = issuer;
		this.jwtExpiration = jwtExpiration;
	}

	@Override
	public int post(UUID jobId) {
		HttpRequest request = HttpRequest.newBuilder(jobs.resolve(jobId.toString()))
				.timeout(readTimeout)
				.header("Authorization", "Bearer " + token())
				.POST(HttpRequest.BodyPublishers.noBody())
				.build();
		try {
			return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new CaseJobCallException("El backend no respondió", exception);
		}
		catch (IOException exception) {
			throw new CaseJobCallException("El backend no respondió", exception);
		}
	}

	private String token() {
		Instant now = Instant.now();
		return Jwts.builder()
				.issuer(issuer)
				.subject("lifeline-worker")
				.claim("token_use", "service")
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(jwtExpiration)))
				.signWith(secretKey, Jwts.SIG.HS256)
				.compact();
	}
}
