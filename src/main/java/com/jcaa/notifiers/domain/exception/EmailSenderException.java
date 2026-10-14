package com.jcaa.notifiers.domain.exception;

public final class EmailSenderException extends DomainException {

  private static final String MESSAGE_WITH_STATUS =
      "No se pudo enviar el correo a '%s'. Brevo respondió HTTP %d: %s";
  private static final String MESSAGE_WITH_DETAIL =
      "No se pudo enviar el correo a '%s'. Error de conexión con Brevo: %s";

  public EmailSenderException(final String message) {
    super(message);
  }

  public EmailSenderException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static EmailSenderException becauseBrevoRejected(
      final String destinationEmail, final int httpStatus, final String responseBody) {
    return new EmailSenderException(
        String.format(MESSAGE_WITH_STATUS, destinationEmail, httpStatus, responseBody));
  }

  public static EmailSenderException becauseConnectionFailed(
      final String destinationEmail, final Throwable cause) {
    return new EmailSenderException(
        String.format(MESSAGE_WITH_DETAIL, destinationEmail, cause.getMessage()), cause);
  }
}
