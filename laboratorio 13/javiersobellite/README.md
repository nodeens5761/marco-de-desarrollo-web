# TechLab

Aplicación web para administrar cursos y categorías, con autenticación para el área de gestión y acceso protegido a la API.

## Requisitos

- JDK 25.
- Docker Engine y Docker Compose.
- Visual Studio Code con Extension Pack for Java y Spring Boot Extension Pack.
- Conexión a internet para descargar Maven y dependencias la primera vez.

## Configuración de MySQL

Copia `.env.example` a `.env` y modifica las contraseñas locales.

Linux:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Inicia la base de datos:

```bash
docker compose up -d mysql
docker compose ps
```

Continúa cuando `techlab-mysql` aparezca como `healthy`.

## Credenciales de acceso

La cuenta se configura mediante variables de entorno. El usuario predeterminado es `docente`; la contraseña debe definirse antes de iniciar la aplicación.

Linux o macOS:

```bash
export APP_DEMO_USERNAME=docente
export APP_DEMO_PASSWORD='Cambia_Esta_Clave_Local_2026!'
export TECHLAB_DB_PASSWORD='TechLabLocal_12345'
```

Windows PowerShell:

```powershell
$env:APP_DEMO_USERNAME = "docente"
$env:APP_DEMO_PASSWORD = "Cambia_Esta_Clave_Local_2026!"
$env:TECHLAB_DB_PASSWORD = "TechLabLocal_12345"
```

`TECHLAB_DB_PASSWORD` debe coincidir con el valor configurado en `.env`. `APP_DEMO_PASSWORD` no tiene un valor predeterminado: la aplicación no inicia si no se define. No guardes la contraseña real en Git ni en capturas.

## Ejecutar

Define las variables de entorno en la misma terminal o en el entorno desde el que iniciarás la aplicación. En VS Code, puedes hacerlo desde una terminal integrada.

Linux o macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Abre `http://localhost:8080/cursos`.

## Acceso a las funciones

| Ruta | Acceso |
|---|---|
| `GET /cursos` | Público |
| `GET /cursos/{id}` | Público |
| `GET /cursos/resumen` | Público |
| `GET /cursos/nuevo` | Requiere iniciar sesión |
| `GET /cursos/{id}/editar` | Requiere iniciar sesión |
| Crear, actualizar y eliminar cursos | Requiere iniciar sesión y token CSRF |
| `GET /perfil` | Requiere iniciar sesión |
| `/api/**` | Requiere autenticación HTTP Basic |

La sesión web utiliza cookie `HttpOnly`, `SameSite=Lax`, expiración de 30 minutos y protección contra fijación de sesión. El cierre de sesión se realiza mediante POST.

La API utiliza HTTP Basic y una política sin estado. En un entorno real, Basic debe utilizarse únicamente sobre HTTPS.

## Pruebas

Docker debe estar activo para las pruebas que usan MySQL mediante Testcontainers.

```bash
./mvnw -Dtest=CourseInputDtoValidationTests test
./mvnw -Dtest=CourseCrudIntegrationTests test
./mvnw -Dtest=CourseControllerTests test
./mvnw -Dtest=CourseViewControllerTests test
./mvnw -Dtest=Apf3AcceptanceTests test
./mvnw -Dtest=WebSecurityTests test
./mvnw -Dtest=ApiSecurityTests test
./mvnw clean test
```

En Windows sustituye `./mvnw` por `\.\mvnw.cmd`.

La suite cubre validaciones, persistencia, MVC, API, migraciones, login, autorización, CSRF, autenticación Basic y la ruta privada de perfil. Los reportes se generan en `target/surefire-reports/`.

## Pruebas manuales

Las solicitudes de ejemplo están en `requests.http`. Los comandos de API y sus resultados esperados están en `docs/PRUEBAS_SEGURIDAD.md`.

## Base de datos

Flyway conserva las migraciones V1, V2 y V3. Hibernate valida el esquema mediante `ddl-auto=validate`; no crea ni actualiza tablas.

Para reiniciar únicamente la base local del proyecto, revisa el nombre del volumen y utiliza:

```bash
CONFIRM_RESET=SI ./scripts/reset-apf3.sh
```

Este procedimiento elimina el volumen MySQL del proyecto. No lo ejecutes si necesitas conservar esos datos.

## Estructura principal

```text
src/main/java/pe/edu/utp/techlab/
  config/SecurityConfig.java
  controller/
    CourseController.java
    CourseApiExceptionHandler.java
    CourseViewController.java
    LoginController.java
    ProfileController.java
  domain/
  dto/
  exception/
  mapper/
  repository/
  service/CourseService.java
  web/
    GlobalViewModel.java
    SecurityView.java
```

La interfaz utiliza Thymeleaf y Bootstrap. MVC y REST comparten `CourseService`, DTO, repositorios y reglas de negocio. Las entidades JPA no se exponen directamente en la API.

## Decisiones de seguridad

- Una cadena de filtros para `/api/**` y otra para la interfaz web.
- Contraseña codificada con BCrypt.
- Usuario temporal en memoria configurado mediante variables de entorno.
- Formulario web con sesión y CSRF activo.
- API con HTTP Basic, `STATELESS` y CSRF desactivado únicamente en esa cadena.
- Recursos estáticos y catálogo público; operaciones de gestión protegidas.
- Mensaje genérico ante credenciales incorrectas.

## Entrega

Completa `docs/MATRIZ_SEGURIDAD.md` después de ejecutar las pruebas. No marques una comprobación como aprobada sin conservar su resultado real. No incluyas `.env`, contraseñas, hashes, cookies, cabeceras `Authorization`, logs ni volúmenes MySQL.
