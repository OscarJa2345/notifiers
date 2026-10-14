package com.jcaa.notifiers.infrastructure.entrypoint.rest;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de estado. Sirve como health check de Render y para "despertar" el servicio gratuito
 * antes de probar (dormido no consume mensajes de Kafka).
 */
@RestController
public class StatusController {

  @GetMapping("/")
  public Map<String, String> status() {
    return Map.of("service", "notifiers", "status", "UP");
  }
}
