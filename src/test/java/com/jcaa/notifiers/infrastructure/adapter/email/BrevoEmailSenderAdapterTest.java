package com.jcaa.notifiers.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.notifiers.domain.exception.EmailSenderException;
import com.jcaa.notifiers.domain.model.EmailDestinationModel;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba el adaptador contra un servidor HTTP local que hace de "Brevo". */
class BrevoEmailSenderAdapterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AtomicReference<String> receivedApiKey = new AtomicReference<>();
  private final AtomicReference<String> receivedBody = new AtomicReference<>();
  private HttpServer server;
  private int responseStatus;

  @BeforeEach
  void startFakeBrevo() throws IOException {
    responseStatus = 201;
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/v3/smtp/email",
        exchange -> {
          receivedApiKey.set(exchange.getRequestHeaders().getFirst("api-key"));
          receivedBody.set(
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          final byte[] response = "{\"messageId\":\"abc\"}".getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(responseStatus, response.length);
          exchange.getResponseBody().write(response);
          exchange.close();
        });
    server.start();
  }

  @AfterEach
  void stopFakeBrevo() {
    server.stop(0);
  }

  private BrevoEmailSenderAdapter adapter() {
    final String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email";
    return new BrevoEmailSenderAdapter(
        new BrevoConfig(url, "xkeysib-prueba", "remitente@gmail.com", "Gestion de Usuarios"),
        objectMapper);
  }

  private static EmailDestinationModel destination() {
    return new EmailDestinationModel("ana@gmail.com", "Ana Pérez", "Bienvenida", "<p>Hola</p>");
  }

  @Test
  void sendsTheEmailWithTheBrevoPayload() throws IOException {
    adapter().send(destination());

    assertEquals("xkeysib-prueba", receivedApiKey.get());
    final JsonNode json = objectMapper.readTree(receivedBody.get());
    assertEquals("remitente@gmail.com", json.get("sender").get("email").asText());
    assertEquals("Gestion de Usuarios", json.get("sender").get("name").asText());
    assertEquals("ana@gmail.com", json.get("to").get(0).get("email").asText());
    assertEquals("Ana Pérez", json.get("to").get(0).get("name").asText());
    assertEquals("Bienvenida", json.get("subject").asText());
    assertEquals("<p>Hola</p>", json.get("htmlContent").asText());
  }

  @Test
  void throwsWhenBrevoRejectsTheRequest() {
    responseStatus = 401;

    final EmailSenderException exception =
        assertThrows(EmailSenderException.class, () -> adapter().send(destination()));

    assertTrue(exception.getMessage().contains("401"));
  }
}
