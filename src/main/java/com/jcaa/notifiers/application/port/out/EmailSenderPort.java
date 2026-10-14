package com.jcaa.notifiers.application.port.out;

import com.jcaa.notifiers.domain.model.EmailDestinationModel;

public interface EmailSenderPort {
  void send(EmailDestinationModel destination);
}
