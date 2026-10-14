package com.jcaa.notifiers.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EmailDestinationModelTest {

  @Test
  void createsModelWithValidData() {
    final EmailDestinationModel model =
        new EmailDestinationModel("ana@gmail.com", "Ana Pérez", "Asunto", "<p>Hola</p>");

    assertEquals("ana@gmail.com", model.getDestinationEmail());
    assertEquals("Ana Pérez", model.getDestinationName());
    assertEquals("Asunto", model.getSubject());
    assertEquals("<p>Hola</p>", model.getBody());
  }

  @Test
  void rejectsBlankEmail() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EmailDestinationModel("  ", "Ana", "Asunto", "<p>Hola</p>"));
  }

  @Test
  void rejectsNullSubject() {
    assertThrows(
        NullPointerException.class,
        () -> new EmailDestinationModel("ana@gmail.com", "Ana", null, "<p>Hola</p>"));
  }
}
