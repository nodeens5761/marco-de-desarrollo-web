# Matriz de aceptación APF3

Completa el estado, la fecha y la evidencia después de ejecutar cada comprobación. No marques una prueba como aprobada sin conservar su resultado real.

| ID | Acción | Resultado esperado | Estado | Fecha / evidencia / observación |
|---|---|---|---|---|
| DB01 | Crear base vacía | V1, V2 y V3 exitosas | Pendiente | |
| DB02 | Consultar relación | Curso y categoría consistentes | Pendiente | |
| W01 | Abrir catálogo | HTTP 200 y datos visibles | Pendiente | |
| W02 | Crear curso válido | Redirección y persistencia | Pendiente | |
| W03 | Enviar formulario inválido | Errores accesibles | Pendiente | |
| W04 | Editar y eliminar | CRUD completo | Pendiente | |
| A01 | Listar API | HTTP 200 y JSON | Pendiente | |
| A02 | Crear API | HTTP 201 y Location | Pendiente | |
| A03 | Enviar JSON inválido | HTTP 400 ProblemDetail | Pendiente | |
| A04 | Consultar inexistente | HTTP 404 ProblemDetail | Pendiente | |
| T01 | Ejecutar `./mvnw clean test` | BUILD SUCCESS | Pendiente | |

## Evidencias solicitadas

- Historial `flyway_schema_history` con V1, V2 y V3 exitosas.
- Salida completa de `./mvnw clean test` y cantidad de pruebas.
- Capturas del catálogo, formulario, errores de validación, detalle y resumen.
- Solicitudes REST que demuestren 200, 201, 204, 400, 404 y 409.
- No incluir contraseñas, cabeceras sensibles, archivos `.env`, logs ni datos del volumen MySQL.
