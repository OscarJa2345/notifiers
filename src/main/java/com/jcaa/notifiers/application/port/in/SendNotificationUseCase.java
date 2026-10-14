package com.jcaa.notifiers.application.port.in;

import com.jcaa.notifiers.application.service.dto.command.SendNotificationCommand;

public interface SendNotificationUseCase {
  void execute(SendNotificationCommand command);
}
