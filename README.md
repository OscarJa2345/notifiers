# notifiers — Microservicio de notificaciones por correo

Parte del parcial de microservicios con Kafka (users + accounts + **notifiers**).
Pasos del diagrama que cubre este servicio:

- **Paso 3:** escucha el tópico `notificaciones` de Kafka (Aiven).
- **Paso 4:** por cada mensaje envía el correo con la API HTTP de Brevo.

Es el único servicio que envía correos: `users` y `accounts` solo dejan mensajes en Kafka.

## Formato del mensaje (acordado por el grupo)

```json
{
  "email": "ana@gmail.com",
  "nombre": "Ana Pérez",
  "asunto": "Bienvenida a Gestión de Usuarios",
  "mensaje": "<p>Hola Ana, tu usuario fue creado.</p>"
}
```

## Arquitectura (misma que el repo del profe)

```
domain/          EmailDestinationModel, excepciones
application/     SendNotificationUseCase (port in) · EmailSenderPort (port out) · SendNotificationService
infrastructure/
  entrypoint/kafka/   NotificationKafkaListener  (paso 3)
  adapter/email/      BrevoEmailSenderAdapter    (paso 4)
  config/             KafkaConsumerConfig, BrevoSpringConfig
  entrypoint/rest/    StatusController  (GET / → estado, despierta el servicio en Render)
```

## Variables de entorno

| Variable | Descripción |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | Servidor de Aiven, `host:puerto` (el puerto SASL) |
| `KAFKA_USERNAME` / `KAFKA_PASSWORD` | Usuario y contraseña SASL de Aiven |
| `KAFKA_CA_CERT` | Contenido completo del certificado CA (`-----BEGIN CERTIFICATE-----` …) |
| `KAFKA_TOPIC` | `notificaciones` (por defecto) |
| `KAFKA_GROUP_ID` | `notifiers-group` (por defecto) |
| `KAFKA_SASL_MECHANISM` | `SCRAM-SHA-256` (por defecto); usar `PLAIN` si Aiven lo indica |
| `BREVO_API_KEY` | Clave API de Brevo (empieza por `xkeysib-`) |
| `BREVO_FROM_ADDRESS` | Correo remitente verificado en Brevo |
| `BREVO_FROM_NAME` | Nombre del remitente (por defecto `Gestion de Usuarios`) |

Nunca subir estos valores a GitHub: van como variables de entorno en Render.

## Ejecutar y probar

```bash
./mvnw clean test       # pruebas unitarias (no necesitan Kafka ni Brevo)
./mvnw clean package    # genera target/notifiers-1.0.0.jar
```

Local con Kafka/Brevo reales: exportar las variables de la tabla y `java -jar target/notifiers-1.0.0.jar`.

Para probar sin `users`, publicar a mano un mensaje en el tópico (por ejemplo con `kcat`):

```bash
echo '{"email":"TU_CORREO","nombre":"Prueba","asunto":"Hola","mensaje":"<p>Funciona</p>"}' | \
kcat -P -b $KAFKA_BOOTSTRAP_SERVERS -t notificaciones \
  -X security.protocol=SASL_SSL -X sasl.mechanisms=SCRAM-SHA-256 \
  -X sasl.username=$KAFKA_USERNAME -X sasl.password=$KAFKA_PASSWORD -X ssl.ca.location=ca.pem
```

## Despliegue en Render

1. Subir este proyecto a su propio repositorio de GitHub.
2. En Render: **New → Blueprint** (usa `render.yaml`) o **New → Web Service** con runtime Docker.
3. Cargar las variables de la tabla (las marcadas `sync: false` se piden en el panel).
4. Verificar: abrir la URL pública → debe responder `{"service":"notifiers","status":"UP"}`.

> En el plan gratuito Render duerme el servicio tras 15 min sin uso y dormido no lee Kafka.
> Los mensajes no se pierden: se envían al despertar. Abrir la URL antes de probar o grabar.
