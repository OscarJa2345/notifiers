package com.jcaa.notifiers.infrastructure.config;

import com.jcaa.notifiers.infrastructure.adapter.email.BrevoConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Lee las propiedades brevo.* y las expone como el bean BrevoConfig. */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class BrevoSpringConfig {

  @Bean
  public BrevoConfig brevoConfig(
      @Value("${brevo.api-url}") final String apiUrl,
      @Value("${brevo.api-key}") final String apiKey,
      @Value("${brevo.from.address}") final String fromAddress,
      @Value("${brevo.from.name}") final String fromName) {
    log.info(
        "[BrevoSpringConfig] url={} apiKey={} remitente={} nombre={}",
        apiUrl,
        apiKey.isBlank() ? "VACIA" : "definida",
        fromAddress.isBlank() ? "VACIO" : fromAddress,
        fromName);
    return new BrevoConfig(apiUrl, apiKey, fromAddress, fromName);
  }
}
