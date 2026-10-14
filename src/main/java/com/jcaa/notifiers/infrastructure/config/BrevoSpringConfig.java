package com.jcaa.notifiers.infrastructure.config;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/** Conexión al Kafka de Aiven (SASL_SSL + certificado CA en formato PEM). */
@Slf4j
@EnableKafka
@Configuration(proxyBeanMethods = false)
public class KafkaConsumerConfig {

  private static final String PLAIN_LOGIN_MODULE =
      "org.apache.kafka.common.security.plain.PlainLoginModule";
  private static final String SCRAM_LOGIN_MODULE =
      "org.apache.kafka.common.security.scram.ScramLoginModule";
  private static final long RETRY_INTERVAL_MS = 3_000L;
  private static final long RETRY_ATTEMPTS = 2L;

  @Value("${kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${kafka.topic}")
  private String topic;

  @Value("${kafka.group-id}")
  private String groupId;

  @Value("${kafka.security-protocol}")
  private String securityProtocol;

  @Value("${kafka.sasl-mechanism}")
  private String saslMechanism;

  @Value("${kafka.username}")
  private String username;

  @Value("${kafka.password}")
  private String password;

  @Value("${kafka.ca-cert}")
  private String caCert;

  @PostConstruct
  void logKafkaSettings() {
    log.info(
        "[KafkaConsumerConfig] servidor={} topico={} protocolo={} mecanismo={} usuario={} password={} certificado={}",
        bootstrapServers,
        topic,
        securityProtocol,
        saslMechanism,
        username.isBlank() ? "VACIO" : username,
        password.isBlank() ? "VACIA" : "definida",
        caCert.isBlank() ? "VACIO" : "definido (" + caCert.length() + " caracteres)");
  }

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    final Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    // Si el grupo es nuevo, lee también los mensajes que se publicaron mientras el servicio dormía.
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);

    if (securityProtocol.contains("SASL")) {
      props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
      props.put(SaslConfigs.SASL_JAAS_CONFIG, buildJaasConfig());
    }
    if (securityProtocol.contains("SSL") && !caCert.isBlank()) {
