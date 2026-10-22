# TechLab - Laboratorio Semana 11

Proyecto Java 25, Spring Boot 4.1.1, Thymeleaf, Jakarta Validation, JPA, Flyway y MySQL 8.4.

## Requisitos

- JDK 25
- Maven 3.6.3 o posterior
- Docker Desktop con Docker Compose
- Visual Studio Code con Extension Pack for Java

## Configuración

1. Descomprime el proyecto y ábrelo desde VS Code con `File > Open Folder`.
2. Copia `.env.example` como `.env` y cambia los dos valores de contraseña.
3. En la terminal ejecuta `docker compose up -d mysql` y espera a que MySQL esté `healthy`.
4. En PowerShell define las credenciales de aplicación con los mismos valores que colocaste en `.env`:

   ```powershell
   $env:DB_USER="techlab_app"
   $env:DB_PASSWORD="la contraseña configurada en .env"
   ```

5. Ejecuta `mvn clean test`.
6. Inicia con `mvn spring-boot:run` o usa la configuración `TechLab` de Run and Debug; esta lee `.env`.
7. Abre `http://localhost:8080/cursos`.

En Git Bash puedes cargar las variables antes de ejecutar Maven con `set -a; source .env; set +a`. Si Maven no está instalado, instálalo y comprueba con `mvn -version`. El proyecto usa Java 25 como la guía de laboratorio.

## Funciones incluidas

- CRUD MVC de cursos con Thymeleaf.
- Relación obligatoria `Course` muchos a uno `Category` con FK en `cursos.categoria_id`.
- Validación por campo en MVC y REST.
- Errores REST con `ProblemDetail`.
- Migraciones Flyway, restricciones SQL y títulos únicos.
- DTOs para no exponer entidades JPA; `EntityGraph` y `join fetch` para evitar consultas N+1.
- Búsqueda por título y filtro opcional por categoría en MVC y API (reto de la guía).
- Pruebas unitarias, web e integración.

## API REST

- `GET /api/v1/cursos`
- `GET /api/v1/cursos?q=Spring`
- `GET /api/v1/cursos?categoriaId=2`
- `GET /api/v1/cursos?q=Spring&categoriaId=2`
- `GET /api/v1/cursos/{id}`
- `POST /api/v1/cursos`
- `PUT /api/v1/cursos/{id}`
- `DELETE /api/v1/cursos/{id}`

Ejemplo para crear un curso:

```json
{
  "titulo": "Java profesional",
  "horas": 24,
  "categoriaId": 2
}
```

Prueba de validación REST desde una terminal con curl:

```bash
curl -i -X POST http://localhost:8080/api/v1/cursos -H "Content-Type: application/json" -d '{"titulo":" ","horas":0,"categoriaId":null}'
```

Debe devolver `400` con `ProblemDetail` y los campos `titulo`, `horas` y `categoriaId`.

## Verificación en MySQL

```sql
SELECT c.id, c.titulo, c.horas, k.nombre AS categoria
FROM cursos c
JOIN categorias k ON k.id = c.categoria_id
ORDER BY c.id;

SHOW CREATE TABLE cursos;

SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Nota sobre el proyecto de semana 10

La guía original asume un TechLab previo con migraciones V1 y V2 ya aplicadas. Como ese proyecto no venía adjunto, este paquete incluye migraciones base V1 y V2 para poder ejecutar el ejemplo desde cero, y la migración V3 con la relación de la semana 11. Si ya tienes tu proyecto de semana 10, conserva las migraciones V1 y V2 originales y traslada los archivos de la semana 11; no edites una migración que ya figure en `flyway_schema_history`.

## Antes de entregar

Ejecuta los casos de la matriz funcional de la guía, guarda capturas de pruebas reales y completa los resultados obtenidos. La prueba de integración necesita MySQL activo; no se afirma `BUILD SUCCESS` hasta ejecutar la suite en tu entorno.
