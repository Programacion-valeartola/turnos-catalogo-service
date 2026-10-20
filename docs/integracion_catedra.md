# Integración con el servicio de la cátedra

Este documento describe cómo se registró y verificó la cuenta técnica del proyecto ante el servicio central de la cátedra. **No contiene credenciales**: el JWT técnico, la contraseña de Redis y la contraseña de la cuenta se almacenan únicamente en el archivo `.env` local, excluido del repositorio.

## Requisitos de red

El servicio de la cátedra se accede a través de la red ZeroTier asignada por la cátedra. El equipo debe estar unido a la red y autorizado por el docente antes de realizar cualquier solicitud.

| Servicio | Variable de entorno |
|---|---|
| API REST | `CATEDRA_API_URL` |
| Redis | `REDIS_HOST`, `REDIS_PORT` |
| Kafka | `KAFKA_BOOTSTRAP_SERVERS` |

Las direcciones concretas se configuran en `.env` y no se publican en el repositorio.

## 1. Registro de la cuenta técnica

Se realiza **una única vez** por proyecto.

```http
POST {CATEDRA_API_URL}/api/student/register
Content-Type: application/json
```

```json
{
  "login": "<usuario>",
  "password": "<contraseña>",
  "groupId": "<nombre del proyecto>",
  "firstName": "<nombre>",
  "lastName": "<apellido>",
  "email": "<email>",
  "langKey": "es"
}
```

Respuesta esperada: `201 Created` con `id_token` (JWT técnico) y el objeto `integration`.

Resultado obtenido:

- `groupId` normalizado: `proyecto-artola`
- `provisioningStatus`: `PROVISIONED`
- Topics y consumer group asignados, utilizados por los servicios mediante variables de entorno.

## 2. Verificación de la integración

```http
GET {CATEDRA_API_URL}/api/student/integration
Authorization: Bearer <JWT técnico>
```

Respuesta obtenida: `200 OK` con la configuración de integración vigente.

## 3. Consulta del snapshot del catálogo

```http
GET {CATEDRA_API_URL}/api/synchronization/snapshot
Authorization: Bearer <JWT técnico>
```

Respuesta obtenida: `200 OK`, `snapshotVersion = 1`.

| Colección | Cantidad | Deshabilitados |
|---|---|---|
| `professionalCategories` | 8 | 0 |
| `professionals` | 24 | 3 |
| `weeklySchedules` | 44 | 3 |

Los campos recibidos coinciden con las columnas definidas en la migración `V1__esquema_inicial_catalogo.sql`, por lo que el esquema no requiere modificaciones para almacenar el snapshot.

## Manejo de credenciales

- El JWT técnico tiene una vigencia de un año y se guarda en `CATEDRA_JWT` dentro de `.env`.
- Ambos servicios utilizan la misma cuenta técnica; el JWT técnico nunca se envía a la aplicación KMP.
- `.env.example` documenta las variables requeridas sin valores sensibles.
- Si las credenciales se exponen, se debe registrar una nueva cuenta técnica, actualizar ambos servicios e informar a la cátedra.