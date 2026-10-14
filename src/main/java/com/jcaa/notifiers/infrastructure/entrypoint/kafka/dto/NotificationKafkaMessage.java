package com.jcaa.notifiers.infrastructure.entrypoint.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Formato acordado por el grupo para el tópico "notificaciones":
 * {"email": "...", "nombre": "...", "asunto": "...", "mensaje": "<p>...</p>"}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationKafkaMessage(String email, String nombre, String asunto, String mensaje) {}
