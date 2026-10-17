# API de procesos de firma

## Obejtivo
Crear una API REST que gestione procesos de firma: un documento PDF que tienen que firmar varias
personas, en un orden determinado y antes de una fecha límite.

## Requerimientos funcionales

### Crear proceso de firma
* Recibe un PDF en base64, la lista de firmantes (nombre y email), el tipo de proceso (secuencial: firman en orden; paralelo: firman en cualquier orden) y una fecha de caducidad.
* Devuelve el identificador del proceso

Preguntas a producto:
* ¿Cual es el número mínimo y máximmo de firmantes para un documento?
* ¿Tamaño máximo permitido del documento?
* ¿Como se deben gestionar los documentos cifrados que no pueden ser firmados?
* ¿Cómo se deben de tratar los procesos de firma caducados?
* ¿Como proceder en caso de error en la creación de un proceso de firma, el usuario debe de volver a crear el proceso?
* ¿La aplicación se comercializa en España o en más países?
* ¿Qué volumen de de procesos se estima?

Supuestos:
* Un único documento por proceso
* Firmantes: entre 1 y 5
* No se permiten firmantes repetidos
* Todos los campos de la petición son obligatorios
* El orden de la lista determina el orden específico de firma
* Cuando se crea el proceso, su estado inicial es PENDING
* El identificador del proceso será un número autoincrementable
* En caso de tipo de proceso paralelo, en el modelo se asignará el orden de la lista aunque no se relevante

Casos límite:
* Validación de la petición (inválida o incompleta)
* Tamaño máximo documento
* Tamaño máximo de firmantes
* Email inválido
* Fecha de caducidad inválida
* Firmante duplicado
* Fallo al almacenar el documento

Diseño:
* Se creará un controlador y servicio para cada uno de los tipos de operaciones:
    *   Crear proceso de firma
    *   Firmar o rechazar un documento
    *   Consultar documento
    *   Verificar firma de documento
* Se creará una entidad y un repositorio para cada una de las entidades mencionadas en el modelo de datos
* Se crearán los mapper necesarios para transferir los datos de un objeto DTO a un objeto Entidad y viceversa 
* PDF: almcenar en base de datos: referencia, tamaño y HASH SHA-256
* Almacenar el disco los documentos
* Tipo de proceso: enum PARALLEL o SEQUENTIAL (escalable a más tipos)
* Orden en la lista de firmantes
* La creación el proceso de firma debe de ser una transacción, con la objetivo de no persistir el proceso de forma incompleta en caso de error.
* Fecha de caducidad: TIMESTAMP en PostgreSQL

## Modelo de datos

### Entidad: **signature_process**
| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| id | BIGINT | PK |
| process_type | VARCHAR(30) | Secuencial/Paralelo
| status | VARCHAR(30) | Estado del proceso |
| creation_date | TIMESTAMP | Fecha de creación |
| expiration_date | TIMESTAMP | Fecha de caducidad del proceso |
| status_update_date | TIMESTAMP | Fecha de la última actualización del estado |

### Entidad: **document**
| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| id | BIGINT | PK |
| signature_process_id | BIGINT | FK UNIQUE de signature_process |
| file_name | VARCHAR(255) | Nombre del documento |
| storage_path | VARCHAR(1024) | Path de la ubicación del documento |
| hash | CHAR(64) | Hash SHA-256 del documento |
| size | INTEGER | Tamaño del documento |
| creation_date | TIMESTAMP | Fecha de persistencia del documento |

### Entidad: **signatory**
| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| id | BIGINT | PK |
| signature_process_id | BIGINT | FK de signature_process |
| name | VARCHAR(250) | Nombre del firmante |
| email | VARCHAR(320) | Email del firmante |
| sign_order | INTEGER | Posición de firma secuencial |
| sign_status | VARCHAR(30) | Estado de firma |
| sign_date | TIMESTAMP | Fecha de firma |
| creation_date | TIMESTAMP | Fecha de creación del registro |

### Relaciones entre entidades
* signature_process -- 1:1 --> document (Un proceso de firma por cada documento)
* signature_process -- 1:N --> signatories (Varios firmantes para un mismo proceso de firma)

## Estados del proceso
**Estados del proceso de firma**
| Estado | Descripción |
|--------|-------------|
| *PENDING* | Se ha creado el proceso de firma, aún no se ha iniciado el proceso de firma |
| *IN_PROGRESS* | El proceso de firma está en curso |
| *COMPLETED* | Se ha completado el proceso de firma |
| *EXPIRED* | El proceso de firma ha caducado |
| *ERROR* | Se ha producido un error durante el ciclo de vida del proceso |

**Estados del firmante**
| Estado | Descripción |
|--------|-------------|
| *PENDING | Pendiente de firmar el documento |
| *NOTIFIED* | Notificada la petición de firma |
| *SIGNED* | Ha firmado el documento |
| *REJECTED* | Ha rechazado un documento |
| *ERROR* | Se ha generado un error en el proceso |

## Propuesta para una segunda iteración
* Añadir o eliminar firmantes de un proceso de firma ya creado
* Creación de proceso de firma con varios documentos
* Almacenamiento de documntos en la nube (S3)
* Almacenar una auditoría de cada proceso
* Autenticación, permisos y roles de usuarios
* Configuración para varios entornos
* Añadir trazas de logs