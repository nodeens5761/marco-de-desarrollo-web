# TechLab - Semana 12

Proyecto de integración Spring MVC, API REST, Spring Data JPA, Flyway, MySQL y Thymeleaf.

## Requisitos

- JDK 25.
- Docker Engine y Docker Compose.
- VS Code con Extension Pack for Java y Spring Boot Extension Pack.
- Conexión a internet la primera vez para descargar Maven y las dependencias.

## 1. Preparar las variables

Copia `.env.example` a `.env` y cambia las contraseñas locales. El archivo `.env` está excluido de Git.

Linux:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Las variables `TECHLAB_DB_PASSWORD` y `TECHLAB_ROOT_PASSWORD` deben coincidir con las que usarás en la conexión de Spring. En PowerShell, antes de iniciar la aplicación:

```powershell
$env:TECHLAB_DB_PASSWORD = "TechLabLocal_12345"
```

En Linux o macOS:

```bash
export TECHLAB_DB_PASSWORD='TechLabLocal_12345'
```

Usa el valor que hayas guardado en tu `.env` local, no necesariamente el valor de ejemplo.

## 2. Iniciar MySQL

Desde la carpeta raíz del proyecto:

```bash
docker compose up -d mysql
docker compose ps
docker compose logs --tail=40 mysql
```

Continúa cuando `techlab-mysql` aparezca como `healthy`.

## 3. Ejecutar la aplicación

Linux o macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación queda disponible en:

- Web: http://localhost:8080/cursos
- Nuevo curso: http://localhost:8080/cursos/nuevo
- Resumen: http://localhost:8080/cursos/resumen
- API: http://localhost:8080/api/v1/cursos

Al iniciar, Flyway ejecuta V1, V2 y V3 si la base está vacía. Hibernate valida el esquema; no crea ni actualiza tablas.

## 4. Ejecutar las pruebas

Docker debe estar encendido. Las pruebas de persistencia, MVC, REST y aceptación usan un contenedor MySQL 8.4 independiente.

```bash
./mvnw -Dtest=CourseInputDtoValidationTests test
./mvnw -Dtest=CourseCrudIntegrationTests test
./mvnw -Dtest=CourseControllerTests test
./mvnw -Dtest=CourseViewControllerTests test
./mvnw -Dtest=Apf3AcceptanceTests test
./mvnw clean test
```

En Windows reemplaza `./mvnw` por `.\mvnw.cmd`.

Los reportes se generan en `target/surefire-reports/`. La matriz de aceptación está en `docs/MATRIZ_APF3.md`.

## 5. Reiniciar la base de laboratorio

Este comando elimina el volumen MySQL de este proyecto y sus datos. Úsalo solo cuando quieras volver a probar desde una base vacía.

```bash
CONFIRM_RESET=SI ./scripts/reset-apf3.sh
```

El script vuelve a crear el contenedor. Espera el estado `healthy` y luego inicia la aplicación. Flyway reconstruirá el esquema desde V1, V2 y V3. En Windows puedes ejecutar los mismos comandos Docker manualmente desde la raíz del proyecto; revisa dos veces el nombre del volumen antes de usar `docker compose down -v`.

## Arquitectura

```text
controller/
  CourseController.java
  CourseApiExceptionHandler.java
  CourseViewController.java
domain/
  Course.java
  Category.java
dto/
  CourseInputDto.java
  CourseDto.java
  CategoryDto.java
  CategoryCourseCountDto.java
  CourseSummaryDto.java
mapper/
  CourseMapper.java
repository/
  CourseRepository.java
  CategoryRepository.java
service/
  CourseService.java
exception/
  CourseNotFound.java
  CategoryNotFound.java
  DuplicateCourseTitle.java
  InvalidCourseData.java
```

MVC y REST comparten `CourseService`, DTO, repositorios y reglas de negocio. Las entidades JPA no se exponen como respuesta de la API. La relación `Course` a `Category` usa `ManyToOne` con carga `LAZY`.

## Reto de aplicación incluido

La ruta `GET /cursos/resumen` muestra el total de cursos y la cantidad agrupada por categoría. La consulta agregada se ejecuta en `CourseRepository`, el servicio orquesta el caso de uso y Thymeleaf presenta el resultado.

## Decisiones de la semana 12

- Spring Boot 4.1.2 y Java 25.
- MySQL 8.4.
- Flyway es la única autoridad para crear y modificar el esquema.
- `spring.jpa.hibernate.ddl-auto=validate`.
- `spring.jpa.open-in-view=false`.
- Validación de entrada en DTO, servicio y restricciones de MySQL.
- Respuestas REST de error con `ProblemDetail`.
- No se agregó Spring Security, login, roles ni JWT.

## Seguridad de la entrega

No subas `.env`, contraseñas, `target/`, logs ni volúmenes de MySQL. Las migraciones V1, V2 y V3 no deben editarse una vez aplicadas; cualquier cambio posterior debe agregarse como una nueva migración.
