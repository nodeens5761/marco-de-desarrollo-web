# TechLab Web – Semana 10 (CRUD web y REST)

Laboratorio de **Marcos de Desarrollo Web**: operaciones CRUD con Spring Boot, Spring Data JPA, Thymeleaf y API REST sobre MySQL.

**Autor:** Angel Vilcatoma
**Stack:** Java 25 LTS, Spring Boot 4.1.1, MySQL 8.4.12 (Docker Compose), Flyway, Thymeleaf, Bootstrap 5.3.8
**Línea base (semana 9):** _registrar aquí el commit o la fecha de la copia_

## Contrato REST
| Método y ruta | Uso | Éxito | Error esperado |
|---------------|-----|-------|----------------|
| GET /api/v1/cursos | Listar o filtrar | 200 | 400 |
| GET /api/v1/cursos/{id} | Detalle | 200 | 404 |
| POST /api/v1/cursos | Crear | 201 + Location | 400, 409 |
| PUT /api/v1/cursos/{id} | Actualizar | 200 | 400, 404, 409 |
| DELETE /api/v1/cursos/{id} | Eliminar | 204 | 404 |

Errores en `application/problem+json` (ProblemDetail).

## Contrato web
GET /cursos/nuevo · POST /cursos · GET /cursos/{id}/editar · POST /cursos/{id} · POST /cursos/{id}/eliminar (Post-Redirect-Get con mensajes flash).

## Puesta en marcha
    cp .env.example .env          # reemplazar contraseñas
    docker compose up -d mysql
    docker compose ps             # esperar healthy

    # Linux / macOS / Git Bash
    export DB_USER='techlab_app' DB_PASSWORD='valor_de_su_env'
    # PowerShell
    $env:DB_USER='techlab_app'; $env:DB_PASSWORD='valor_de_su_env'

    ./mvnw clean test
    ./mvnw clean package
    java -jar target/techlab-web-0.0.1-SNAPSHOT.jar

(En PowerShell usar `.\mvnw.cmd`. Si falta el wrapper: `mvn wrapper:wrapper`.)

## Migraciones Flyway
| Versión | Archivo | Descripción |
|---------|---------|-------------|
| V1 | V1__create_cursos_table.sql | Tabla `cursos` |
| V2 | V2__seed_cursos.sql | Catálogo inicial |
| V3 | V3__add_categoria_to_cursos.sql | Reto semana 9: `categoria` |
| V4 | V4__add_version_to_cursos.sql | Reto semana 10: `version` (bloqueo optimista) |

## Pruebas REST (curl)
    curl -i -X POST http://localhost:8080/api/v1/cursos -H "Content-Type: application/json" -d '{"titulo":"Java profesional","horas":24}'
    curl -i http://localhost:8080/api/v1/cursos/4
    curl -i -X PUT http://localhost:8080/api/v1/cursos/4 -H "Content-Type: application/json" -d '{"titulo":"Java web","horas":32}'
    curl -i -X DELETE http://localhost:8080/api/v1/cursos/4
    curl -i http://localhost:8080/api/v1/cursos/4

Secuencia esperada: 201, 200, 200, 204, 404. Conflicto de versión: enviar PUT con `"version"` desactualizada → 409.

## Matriz funcional
| ID | Acción | Resultado esperado | Estado |
|----|--------|--------------------|--------|
| W01 | Abrir formulario nuevo | 200 y campos vacíos | ☐ |
| W02 | Crear curso válido | Redirección y mensaje | ☐ |
| W03 | Crear título repetido | Formulario conserva valores | ☐ |
| W04 | Editar curso existente | Cambio persistente | ☐ |
| W05 | Editar id inexistente | 404 | ☐ |
| W06 | Cancelar eliminación | Curso permanece | ☐ |
| W07 | Confirmar eliminación | Redirección y curso ausente | ☐ |
| A01 | POST válido | 201, cuerpo y Location | ☐ |
| A02 | POST inválido | 400 ProblemDetail | ☐ |
| A03 | POST duplicado | 409 ProblemDetail | ☐ |
| A04 | PUT existente | 200 con datos nuevos | ☐ |
| A05 | PUT inexistente | 404 ProblemDetail | ☐ |
| A06 | DELETE existente | 204 sin cuerpo | ☐ |
| A07 | GET después de borrar | 404 | ☐ |
| A08 | PUT con versión vencida | 409 ProblemDetail | ☐ |
| P01 | Reiniciar aplicación | Datos conservados | ☐ |
| T01 | clean test | BUILD SUCCESS | ☐ |

> Aviso: las escrituras no tienen autenticación; no publicar en Internet (Spring Security en una unidad posterior).
