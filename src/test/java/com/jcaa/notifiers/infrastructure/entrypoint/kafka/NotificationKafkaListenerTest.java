package com.jcaa.notifiers.infrastructure.entrypoint.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.application.service.dto.command.SendNotificationCommand;
import com.jcaa.notifiers.domain.exception.EmailSenderException;
import com.jcaa.notifiers.infrastructure.entrypoint.kafka.mapper.NotificationKafkaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaListenerTest {

  private static final String VALID_PAYLOAD =
      """
      {"email":"ana@gmail.com","nombre":"Ana Pérez","asunto":"Bienvenida",
       "mensaje":"<p>Hola Ana</p>","campoExtra":"se ignora"}
      """;

  @Mock private SendNotificationUseCase useCase;

  private NotificationKafkaListener listener;

  @BeforeEach
  void setUp() {
    listener =
        new NotificationKafkaListener(new ObjectMapper(), new NotificationKafkaMapper(), useCase);
  }

  @Test
  void mapsTheAgreedJsonFormatAndSendsTheEmail() {
    listener.onMessage(VALID_PAYLOAD);

    final ArgumentCaptor<SendNotificationCommand> captor =
        ArgumentCaptor.forClass(SendNotificationCommand.class);
    verify(useCase).execute(captor.capture());
    assertEquals("ana@gmail.com", captor.getValue().email());
    assertEquals("Ana Pérez", captor.getValue().name());
    assertEquals("Bienvenida", captor.getValue().subject());
    assertEquals("<p>Hola Ana</p>", captor.getValue().body());
  }

  @Test
  void discardsMalformedJsonWithoutFailing() {
    listener.onMessage("esto no es json");

    verify(useCase, never()).execute(any());
  }

  @Test
  void discardsMessagesWithMissingFields() {
    doThrow(new IllegalArgumentException("El asunto es requerido.")).when(useCase).execute(any());

    listener.onMessage("{\"email\":\"ana@gmail.com\"}");
    // no lanza excepción: el mensaje se descarta
  }

  @Test
  void propagatesSendFailuresSoKafkaRetries() {
    doThrow(new EmailSenderException("Brevo caído")).when(useCase).execute(any());

    assertThrows(EmailSenderException.class, () -> listener.onMessage(VALID_PAYLOAD));
  }
}
