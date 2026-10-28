# TechLab Web – Semana 7

Laboratorio de **Marcos de Desarrollo Web**: Thymeleaf (expresiones, atributos y plantillas).

**Autor:** Angel Vilcatoma
**Stack:** Java 25 LTS, Spring Boot 4.1.1, Thymeleaf 3.1.5.RELEASE, Bootstrap 5.3.8

## Qué incluye
- API REST de la semana 6 conservada: `GET /api/v1/cursos` (JSON).
- Vistas Thymeleaf: catálogo (`/cursos`), búsqueda por GET (`/cursos?q=Spring`), detalle (`/cursos/{id}`).
- Reto de transferencia: resumen (`/cursos/resumen`) con 3 cursos y 48 horas.
- Fragmentos (`head`, `cabecera`, `pie`), mensajes externalizados, CSS accesible y página de error 4xx.
- Pruebas MVC con MockMvc (`CourseViewControllerTests`).

## Comandos
Linux / macOS / Git Bash:

    ./mvnw clean test
    ./mvnw spring-boot:run
    ./mvnw clean package
    java -jar target/techlab-web-0.0.1-SNAPSHOT.jar

Windows PowerShell: usar `.\mvnw.cmd` en lugar de `./mvnw`.

> Si el wrapper (`mvnw`) no está presente, genérelo con `mvn wrapper:wrapper`
> o abra el proyecto en el IDE (IntelliJ / Eclipse / VS Code) con Java 25.

## Matriz de pruebas manuales
| Caso | Acción | Resultado esperado | Estado |
|------|--------|--------------------|--------|
| M1 | GET /cursos | Tres tarjetas y HTML sin expresiones Thymeleaf | 200 |
| M2 | GET /cursos?q=Spring | Una tarjeta y campo con Spring | 200 |
| M3 | GET /cursos?q=NoExiste | Estado vacío y cero resultados | 200 |
| M4 | GET /cursos/2 | Detalle de Bootstrap con 16 horas | 200 |
| M5 | GET /cursos/999 | Vista 4xx y ruta informada | 404 |
| M6 | q con 61 caracteres | Solicitud rechazada | 400 |
| M7 | GET /api/v1/cursos | JSON de tres elementos | 200 |
| M8 | GET /portal | Redirección a /cursos | 302 |
| M9 | GET /cursos/resumen | 3 cursos y 48 horas | 200 |

## Evidencias
Agregar capturas de pantalla (320, 768 y 1280 px, foco con Tab, pestaña Network) en `docs/evidencias/`.
