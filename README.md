# API de procesos de firma

## Stack ténico
| Componente | Stack | Versión |
|------------|-------|---------|
| Lenguaje Backend | Java | 21 |
| Framework | SpringBoot | 4.1.1 |
| Validación | Spring Validation | N/A |
| Persistencia | Spring Data JPA | N/A |
| Bases de datos | Postgres | 17 |
| Almacenamiento | Disco local | N/A |
| Documentación | Swagger | 3.1.1 |
| Test | Mockito | 5.24.0 |

## Configuración de entorno
### Iniciar servidor Tomcat Embedido de Spring Boot
Intalar las dependencias de Maven
> mvn clean install

Ejecutar en la raiz de la aplicación.
> mvn spring-boot:run
>
 API expuesta en localhost:8080/

### Autorización JWT
Para tener autorización de acceso a los endpoint de la aplicación, se debe obtener un token.

Endpoint para obtener el token de acceso:
> curl --location 'localhost:8080/auth/token' \
--header 'Content-Type: application/json' \
--data '{
	"username": "admin",
    "password": "1234"
}'
>

Una vez se obtenga el token, añadir su valor en un *header* de autorización de tipo *Bearer Token*
> curl --location 'localhost:8080/ejemplo' \
--header 'Authorization: Bearer ***'

### Swagger
Se utiliza Swagger v3 para mostrar los endpoints expuestos por la API
> http://localhost:8080/swagger-ui/index.html
>

### Postman
Importar la colección de Postman
> 
>

### Docker
Ejecutar en \infrastructure\docker
> docker-compose up -d
>

Conexión a BBDD:
> user: admin

> pasword: 1234
>

Ejecutar el script para insertar datos de ejemplo:
> \postgresql\ejemplos.sql

