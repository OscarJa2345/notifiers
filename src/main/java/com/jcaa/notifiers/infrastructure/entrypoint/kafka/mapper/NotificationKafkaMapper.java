package com.jcaa.notifiers.infrastructure.entrypoint.kafka.mapper;

import com.jcaa.notifiers.application.service.dto.command.SendNotificationCommand;
import com.jcaa.notifiers.infrastructure.entrypoint.kafka.dto.NotificationKafkaMessage;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaMapper {

  public SendNotificationCommand toCommand(final NotificationKafkaMessage message) {
    return new SendNotificationCommand(
        message.email(), message.nombre(), message.asunto(), message.mensaje());
  }
}
