package com.jcaa.notifiers.infrastructure.adapter.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.notifiers.application.port.out.EmailSenderPort;
import com.jcaa.notifiers.domain.exception.EmailSenderException;
import com.jcaa.notifiers.domain.model.EmailDestinationModel;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Envía correos con la API HTTP de Brevo (POST /v3/smtp/email). Se usa la API y no SMTP porque
 * Render bloquea los puertos SMTP salientes.
 */
@Slf4j
@Component
public class BrevoEmailSenderAdapter implements EmailSenderPort {

  private static final String HEADER_API_KEY = "api-key";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_ACCEPT = "Accept";
  private static final String JSON = "application/json";
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

  private final BrevoConfig config;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public BrevoEmailSenderAdapter(final BrevoConfig config, final ObjectMapper objectMapper) {
    this.config = config;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
  }

  @Override
  public void send(final EmailDestinationModel destination) {
    final String destinationEmail = destination.getDestinationEmail();
    try {
      final HttpRequest request = buildRequest(destination);
      final HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw EmailSenderException.becauseBrevoRejected(
            destinationEmail, response.statusCode(), response.body());
      }
      log.info("[BrevoEmailSenderAdapter] correo enviado a {}.", destinationEmail);
    } catch (final IOException exception) {
      throw EmailSenderException.becauseConnectionFailed(destinationEmail, exception);
    } catch (final InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw EmailSenderException.becauseConnectionFailed(destinationEmail, exception);
    }
  }

  private HttpRequest buildRequest(final EmailDestinationModel destination)
      throws JsonProcessingException {
    final Map<String, Object> payload =
        Map.of(
            "sender", Map.of("name", config.fromName(), "email", config.fromAddress()),
            "to",
                List.of(
                    Map.of(
                        "email", destination.getDestinationEmail(),
                        "name", destination.getDestinationName())),
            "subject", destination.getSubject(),
            "htmlContent", destination.getBody());

    return HttpRequest.newBuilder()
        .uri(URI.create(config.apiUrl()))
        .timeout(REQUEST_TIMEOUT)
        .header(HEADER_API_KEY, config.apiKey())
        .header(HEADER_CONTENT_TYPE, JSON)
        .header(HEADER_ACCEPT, JSON)
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
        .build();
  }
}
