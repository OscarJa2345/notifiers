package com.jcaa.notifiers.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.jcaa.notifiers.application.port.out.EmailSenderPort;
import com.jcaa.notifiers.application.service.dto.command.SendNotificationCommand;
import com.jcaa.notifiers.domain.model.EmailDestinationModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

  @Mock private EmailSenderPort emailSenderPort;

  @Test
  void sendsEmailWithTheCommandData() {
    final SendNotificationService service = new SendNotificationService(emailSenderPort);

    service.execute(
        new SendNotificationCommand("ana@gmail.com", "Ana Pérez", "Bienvenida", "<p>Hola</p>"));

    final ArgumentCaptor<EmailDestinationModel> captor =
        ArgumentCaptor.forClass(EmailDestinationModel.class);
    verify(emailSenderPort).send(captor.capture());
    assertEquals("ana@gmail.com", captor.getValue().getDestinationEmail());
    assertEquals("Ana Pérez", captor.getValue().getDestinationName());
    assertEquals("Bienvenida", captor.getValue().getSubject());
    assertEquals("<p>Hola</p>", captor.getValue().getBody());
  }

  @Test
  void doesNotSendWhenDataIsIncomplete() {
    final SendNotificationService service = new SendNotificationService(emailSenderPort);

    assertThrows(
        IllegalArgumentException.class,
        () -> service.execute(new SendNotificationCommand("", "Ana", "Asunto", "<p>Hola</p>")));
    verify(emailSenderPort, never()).send(any());
  }
}
