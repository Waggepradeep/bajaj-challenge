package com.bajaj.challenge.service;

import com.bajaj.challenge.config.ContestProperties;
import com.bajaj.challenge.dto.GenerateWebhookRequest;
import com.bajaj.challenge.dto.GenerateWebhookResponse;
import com.bajaj.challenge.dto.SubmitSolutionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

@Component
public class StartupRunner implements ApplicationRunner {
	private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

	private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
	private static final String TEST_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

	private final ContestProperties properties;
	private final WebClient webClient;

	public StartupRunner(ContestProperties properties) {
		this.properties = properties;
		this.webClient = WebClient.builder()
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			log.info("Starting Bajaj challenge flow");
			GenerateWebhookResponse response = generateWebhook();
			if (response == null || !StringUtils.hasText(response.getAccessToken())) {
				log.error("Failed to generate webhook. Response: {}", response);
				return;
			}

			String finalQuery = resolveFinalQuery();
			if (!StringUtils.hasText(finalQuery)) {
				log.error("No SQL found. Provide env FINAL_QUERY or fill resources/queries/*.sql");
				return;
			}

			submitSolution(response.getAccessToken(), finalQuery);
		} catch (Exception e) {
			log.error("Flow failed", e);
		}
	}

	private GenerateWebhookResponse generateWebhook() {
		GenerateWebhookRequest body = new GenerateWebhookRequest(
			Objects.requireNonNullElse(properties.getName(), ""),
			Objects.requireNonNullElse(properties.getRegNo(), ""),
			Objects.requireNonNullElse(properties.getEmail(), "")
		);

		return webClient.post()
			.uri(GENERATE_WEBHOOK_URL)
			.body(BodyInserters.fromValue(body))
			.retrieve()
			.bodyToMono(GenerateWebhookResponse.class)
			.timeout(Duration.ofSeconds(30))
			.doOnNext(r -> log.info("Webhook generated. message={}, webhook={}, hasToken={}",
				r.getMessage(), r.getWebhook(), StringUtils.hasText(r.getAccessToken())))
			.block();
	}

	private String resolveFinalQuery() {
		String fromEnv = System.getenv("FINAL_QUERY");
		if (StringUtils.hasText(fromEnv)) {
			log.info("Using SQL from env FINAL_QUERY");
			return fromEnv.trim();
		}
		// Choose based on last two digits parity; odd => odd.sql, even => even.sql
		String regNo = properties.getRegNo() == null ? "" : properties.getRegNo().trim();
		String lastTwoDigits = regNo.replaceAll("[^0-9]", "");
		if (lastTwoDigits.length() >= 2) {
			lastTwoDigits = lastTwoDigits.substring(lastTwoDigits.length() - 2);
		}
		boolean isOdd = false;
		if (StringUtils.hasText(lastTwoDigits)) {
			try {
				int n = Integer.parseInt(lastTwoDigits);
				isOdd = (n % 2) == 1;
			} catch (NumberFormatException ignored) { }
		}
		String resourcePath = isOdd ? "queries/odd.sql" : "queries/even.sql";
		try {
			byte[] bytes = Objects.requireNonNull(
				getClass().getClassLoader().getResourceAsStream(resourcePath)
			).readAllBytes();
			String sql = new String(bytes, StandardCharsets.UTF_8).trim();
			if (sql.isEmpty() || sql.startsWith("-- FILL")) {
				log.warn("SQL file {} is empty or placeholder.", resourcePath);
				return null;
			}
			log.info("Using SQL from classpath: {}", resourcePath);
			return sql;
		} catch (Exception e) {
			log.warn("Unable to read SQL from {}: {}", resourcePath, e.getMessage());
			return null;
		}
	}

	private void submitSolution(String jwtToken, String finalQuery) {
		SubmitSolutionRequest body = new SubmitSolutionRequest(finalQuery);

		ClientResponse response = webClient.post()
			.uri(TEST_WEBHOOK_URL)
			.header(HttpHeaders.AUTHORIZATION, jwtToken)
			.body(BodyInserters.fromValue(body))
			.exchangeToMono(Mono::just)
			.timeout(Duration.ofSeconds(30))
			.block();

		if (response == null) {
			log.error("No response from test webhook");
			return;
		}
		String respBody = response.bodyToMono(String.class).blockOptional().orElse("");
		log.info("Submission status={}, body={}", response.statusCode(), respBody);
	}
}


