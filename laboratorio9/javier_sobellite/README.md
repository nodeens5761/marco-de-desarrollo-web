# TechLab Web – Semana 9 (Persistencia)

Laboratorio de **Marcos de Desarrollo Web**: JPA, Hibernate, Spring Data JPA, Flyway y MySQL.

**Autor:** Angel Vilcatoma
**Stack:** Java 25 LTS, Spring Boot 4.1.1, MySQL 8.4.12 (Docker Compose), Flyway, Thymeleaf, Bootstrap 5.3.8
**Línea base (semana 8):** _registrar aquí el commit o la fecha de la copia_

## Arquitectura
Controlador → `CourseService` (@Transactional readOnly) → `CourseRepository` (Spring Data JPA) → Hibernate/JDBC → MySQL.
El servicio devuelve `CourseDto` mediante `CourseMapper`; las entidades JPA nunca llegan a la capa web.

## Puesta en marcha
1. `cp .env.example .env` y reemplazar ambas contraseñas (el `.env` real no se sube a Git).
2. Levantar MySQL:

       docker compose config --quiet
       docker compose up -d mysql
       docker compose ps

3. Definir variables en la misma terminal donde se ejecuta Maven:

       # Linux / macOS / Git Bash
       export DB_USER='techlab_app' DB_PASSWORD='valor_de_su_env'
       # PowerShell
       $env:DB_USER='techlab_app'; $env:DB_PASSWORD='valor_de_su_env'

4. Ejecutar:

       ./mvnw clean test
       ./mvnw clean package
       java -jar target/techlab-web-0.0.1-SNAPSHOT.jar

   (En PowerShell usar `.\mvnw.cmd`. Si falta el wrapper: `mvn wrapper:wrapper`.)

## Migraciones Flyway
| Versión | Archivo | Descripción |
|---------|---------|-------------|
| V1 | V1__create_cursos_table.sql | Tabla `cursos` |
| V2 | V2__seed_cursos.sql | Catálogo inicial (3 cursos) |
| V3 | V3__add_categoria_to_cursos.sql | Reto: columna `categoria` |

Las migraciones aplicadas no se editan; todo cambio nuevo recibe un número nuevo.

## Matriz funcional
| ID | Acción | Resultado esperado | Estado |
|----|--------|--------------------|--------|
| P01 | `docker compose ps` | mysql healthy | ☐ |
| P02 | Consultar cursos | Tres filas y 48 horas | ☐ |
| P03 | Consultar `flyway_schema_history` | V1, V2 y V3 con success=1 | ☐ |
| F01 | /cursos | 200 y tres tarjetas con categoría | ☐ |
| F02 | /cursos?q=boot | Solo Spring Boot | ☐ |
| F03 | /cursos/2 | Detalle de Bootstrap | ☐ |
| F04 | /cursos/999 | 404 sin traza | ☐ |
| F05 | /cursos/resumen | 3 cursos y 48 horas | ☐ |
| A01 | /api/v1/cursos | JSON con tres objetos (incluye categoria) | ☐ |
| A02 | /actuator/health | status UP | ☐ |
| R01 | Reiniciar aplicación | Migraciones no se repiten | ☐ |
| R02 | Reiniciar contenedor | Datos disponibles | ☐ |

## Reinicio destructivo (solo entorno local)
    docker compose down
    docker volume rm techlab_mysql_data
    docker compose up -d mysql
