package com.upc.pe.lifeline.worker.application;

import com.sun.net.httpserver.HttpServer;
import com.upc.pe.lifeline.worker.messaging.CaseJobMessage;
import com.upc.pe.lifeline.worker.messaging.JdkCaseJobClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteCaseJobHandlerTest {
	private static final String SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
	private static final SecretKey KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

	private final CaseJobMessage message = new CaseJobMessage(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"consultation.received",
			UUID.fromString("22222222-2222-2222-2222-222222222222"),
			UUID.fromString("33333333-3333-3333-3333-333333333333"));

	private HttpServer server;
	private ExecutorService executor;

	@AfterEach
	void stopServer() {
		if (server != null) {
			server.stop(0);
		}
		if (executor != null) {
			executor.shutdownNow();
		}
	}

	@Test
	void createdAndExistingCasesCompleteTheJob() throws Exception {
		assertThat(disposition(201)).isInstanceOf(JobDisposition.Completed.class);
		assertThat(disposition(200)).isInstanceOf(JobDisposition.Completed.class);
	}

	@Test
	void aMissingJobIsDiscarded() throws Exception {
		assertThat(disposition(404)).isEqualTo(new JobDisposition.Discard("El trabajo no existe"));
	}

	@Test
	void authenticationAndServerFailuresAreRetried() throws Exception {
		assertThat(disposition(401)).isInstanceOf(JobDisposition.Retry.class);
		assertThat(disposition(403)).isInstanceOf(JobDisposition.Retry.class);
		assertThat(disposition(503)).isInstanceOf(JobDisposition.Retry.class);
		assertThat(disposition(409)).isInstanceOf(JobDisposition.Retry.class);
	}

	@Test
	void aTimeoutIsRetried() throws Exception {
		start(exchange -> {
			try {
				Thread.sleep(1_000);
			}
			catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
			}
		});
		JdkCaseJobClient client = client(Duration.ofMillis(200), Duration.ofMillis(200));

		assertThat(new RemoteCaseJobHandler(client).handle(message, 1)).isInstanceOf(JobDisposition.Retry.class);
	}

	private JobDisposition disposition(int status) throws Exception {
		AtomicReference<String> authorization = new AtomicReference<>();
		AtomicInteger bodyLength = new AtomicInteger(-1);
		start(exchange -> {
			authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
			bodyLength.set(exchange.getRequestBody().readAllBytes().length);
			exchange.sendResponseHeaders(status, -1);
			exchange.close();
		});

		JobDisposition disposition = new RemoteCaseJobHandler(client(Duration.ofSeconds(2), Duration.ofSeconds(2)))
				.handle(message, 1);

		assertThat(bodyLength).hasValue(0);
		String token = authorization.get().substring("Bearer ".length());
		Claims claims = Jwts.parser().verifyWith(KEY).requireIssuer("lifeline").build()
				.parseSignedClaims(token).getPayload();
		assertThat(claims.getSubject()).isEqualTo("lifeline-worker");
		assertThat(claims.get("token_use", String.class)).isEqualTo("service");
		assertThat(claims.getIssuedAt()).isNotNull();
		assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
		return disposition;
	}

	private JdkCaseJobClient client(Duration connectTimeout, Duration readTimeout) {
		return new JdkCaseJobClient("http://127.0.0.1:" + server.getAddress().getPort(),
				connectTimeout, readTimeout, SECRET, "lifeline", Duration.ofMinutes(5));
	}

	private void start(com.sun.net.httpserver.HttpHandler handler) throws IOException {
		stopServer();
		executor = Executors.newCachedThreadPool(runnable -> {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		});
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/api/v1/internal/case-jobs/", handler);
		server.setExecutor(executor);
		server.start();
	}
}
