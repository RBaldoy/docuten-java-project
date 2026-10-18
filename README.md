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


