package com.jcaa.notifiers.infrastructure.entrypoint.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.infrastructure.entrypoint.kafka.dto.NotificationKafkaMessage;
import com.jcaa.notifiers.infrastructure.entrypoint.kafka.mapper.NotificationKafkaMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Paso 3 del diagrama: consume los mensajes nuevos de correo del tópico de Kafka. */
@Slf4j
@Component
public class NotificationKafkaListener {

  private final ObjectMapper objectMapper;
  private final NotificationKafkaMapper mapper;
  private final SendNotificationUseCase sendNotificationUseCase;

  public NotificationKafkaListener(
      final ObjectMapper objectMapper,
      final NotificationKafkaMapper mapper,
      final SendNotificationUseCase sendNotificationUseCase) {
    this.objectMapper = objectMapper;
    this.mapper = mapper;
    this.sendNotificationUseCase = sendNotificationUseCase;
  }

  @KafkaListener(
      topics = "${kafka.topic}",
      groupId = "${kafka.group-id}",
      autoStartup = "${kafka.listener.auto-startup:true}")
  public void onMessage(final String payload) {
    final NotificationKafkaMessage message;
    try {
      message = objectMapper.readValue(payload, NotificationKafkaMessage.class);
    } catch (final JsonProcessingException exception) {
      // Un mensaje mal formado nunca se podrá procesar: se descarta para no bloquear la cola.
      log.error("[NotificationKafkaListener] mensaje descartado, JSON inválido: {}", payload);
      return;
    }

    try {
      sendNotificationUseCase.execute(mapper.toCommand(message));
    } catch (final IllegalArgumentException | NullPointerException exception) {
      // Faltan campos obligatorios (email, nombre, asunto o mensaje): tampoco tiene reintento.
      log.error(
          "[NotificationKafkaListener] mensaje descartado, datos incompletos: {}",
          exception.getMessage());
    }
    // EmailSenderException se propaga a propósito: el error handler de Kafka reintenta el envío.
  }
}
