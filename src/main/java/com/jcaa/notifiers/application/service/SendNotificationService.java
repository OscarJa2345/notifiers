package com.jcaa.notifiers.application.service;

import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.application.port.out.EmailSenderPort;
import com.jcaa.notifiers.application.service.dto.command.SendNotificationCommand;
import com.jcaa.notifiers.domain.model.EmailDestinationModel;
import org.springframework.stereotype.Service;

/** Paso 4 del diagrama: convierte la notificación recibida en un correo y lo envía. */
@Service
public class SendNotificationService implements SendNotificationUseCase {

  private final EmailSenderPort emailSenderPort;

  public SendNotificationService(final EmailSenderPort emailSenderPort) {
    this.emailSenderPort = emailSenderPort;
  }

  @Override
  public void execute(final SendNotificationCommand command) {
    final EmailDestinationModel destination =
        new EmailDestinationModel(
            command.email(), command.name(), command.subject(), command.body());
    emailSenderPort.send(destination);
  }
}
