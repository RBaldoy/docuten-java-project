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
* Almacenar en disco los documentos (raíz del proyecto /storage/documents)
* Tipo de proceso: enum PARALLEL o SEQUENTIAL (escalable a más tipos)
* Orden en la lista de firmantes
* La creación el proceso de firma debe de ser una transacción, con la objetivo de no persistir el proceso de forma incompleta en caso de error.
* Fecha de caducidad: TIMESTAMP en PostgreSQL

### Firma de documentos
* Un firmante firma el documento con su clave privada, usando una biblioteca criptográfica estándar
(JCA). Las claves de cada firmante se generan y guardan de forma segura.
* Solo puede firmar si le toca, si el proceso está activo y si no ha firmado ya.
* Cuando todos han firmado, el proceso queda completado.

Supuestos:
* El firmante obtiene las claves para firmar el documento
* El firmante solo firma el documento con éxito si es su turno (en caso de ser un proceso secuencial)
* Cuando firma el primer firmante, el estado del proceso de firma pasa a ser IN_PROGRESS
* Para firmar un documento se necesita el identificador del proceso, el identificador del firmante y la clave privada codificada
* Las claves y certificado siempre se almacenan y devuelven codificados

Diseño:
* Crea un enpoint para generar las claves de los firmantes
* Se crea una clave privada, una clave pública y un certificado
* Se utiliza SHA256 con RSA y un certificado de tipo X509 
* Crear un endpoint para firmar un documento a partir del id de documento, el id de firmante y de la clave privada codificada
* Se valida que exista el identificador del documento, del firmante y del proceso de firma
* Se valida que el firmante forme parte del proceso de firma
* Se valida que el proceso de firma no esté completado, expirado o rechazado
* Se valida que el firmante no haya firmado ni rechazado el documento anteriormente
* Se valida que en caso de proceso de firma secuencial, sea el turno del firmante
* na vez firmado el documento, se crea una nueva versión firmada del documento
* Se actualizan los estados del proceso de firma y del firmante

### Rechazar un documento
* Un firmante puede rechazar el documento indicando un motivo. El proceso queda rechazado.

Supuesto:
* Se necesita el identificador del documento y del firmante para rechazar un documento

Diseño:
* Se comprueba que el identificador el documento se válido
* Se comprueba que el firmante forme parte del proceso de firma
* se actualiza a rechazado los estados del firmante y del proceso de firma

### Consultar un documento
* Devuelve el estado del proceso y de cada firmante

Preguntas a producto:
* ¿Cuando se muestran los detalles del documento se debe de devolver el contenido del documento?
* ¿Se debe de mostrar el orden de firma de los firmantes en caso de ser un proceso secuencial?

Supuestos:
* Se obtiene la información del documento, estado del proceso y firmantes a través del identificador del documento
* El endpoint devuelve los siguientes campos:
    * id del documento
    * nombre del documento
    * hash
    * tamaño del documento
    * fecha de creación del documento
    * estado del proceso de firma
    * Lista de firmantes con los siguientes datos:
        * nombre
        * email
        * orden de firma
        * estado de firma

### Verificación de la firma
* Dado un proceso, comprueba que todas las firmas son válidas y que el documento no se ha modificado

Supuesto:
* Se verifica la validez de las firmas del documento a partir del identificador del proceso de firma
* Para que un documento sea verificable debe estar firmado, sus firmas deben ser válidas y el contenido del documento no debe haber sufrido modificaciones (comprobación por hash)

Diseño:
* Se comprueba que el proceso de firma es válido
* Se obtiene la última versión del documento
* Se verifica que el documento no ha sido modificado comparando el hash almacenado con el hash generado a partir los bytes del documento
* Se verificantodas las firmas del documento y el certificado a partir de los bytes del documento


### Notificación de los procesos de firma parados
* Implementaría una tarea programada (CRON JOB) para que revise periodicamente el estado de los procesos de firma pendientes o en curso.
* En caso de proceso de tipo secuencial, notificar al próximo usuario en la cola para firmar
* En caso de proceso de tipo paralelo, notificar a todos los usuarios firmantes de dicho proceso
* Antes de notificar al usuario, comprobar que no ha sido notificado en un corto plazo de tiempo
* Habría que definir cuanto tiempo es necesario para identificar un proceso como parado y el margen de tiempo para volver a notificar a un usuario

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
| rejection_reason | VARCHAR(256) | Motivo del rechazo del proceso de firma de un documento

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

### Entidad **signatory_key**
| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| id | BIGINT | PK |
| signatory_id | BIGINT | FK de signatory |
| public_key_base64 | TEXT | Clave pública en Base64 |
| private_key_base64 | TEXT | Clave privada en Base64 |
| certificate_base_64 | TEXT | Certificado en Base64 |

### Entidad **document_version**
| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| id | BIGINT | PK |
| document_id | BIGINT | FK de document |
| version_number | INTEGER | Número de versión |
| storage_path | VARCHAR(1024) | Path de la ubicación del documento
| hash | CHAR(64) | Hash SHA-256 del documento |
| creation_date | TIMESTAMP | Fecha de persistencia del documento |
> UNIQUE(document_id, version_number) 

### Relaciones entre entidades
* signature_process -- 1:1 --> document (Un proceso de firma por cada documento)
* signature_process -- 1:N --> signatories (Varios firmantes para un mismo proceso de firma)
* signatories --1:1--> signatory_key
* document --1:N--> document_version

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
* Implementar un CRON Job para actualizar el estado de los procesos de firma con fecha expirada
* Implementar la notificación a los usuarios cuando sea su turno en los procesos de firma secuencial
* Crear un tabla usuarios en lugar de almacenar los datos en firmantes y no depender del identificador de firmantes

## Uso de IA
* Los test unitarios han sido generado con Copilot
* Para el desarrollo del proceso se han utilizado las Inline Suggestions de Copilot