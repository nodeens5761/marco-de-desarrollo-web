# Matriz de aceptación de seguridad

Registra el resultado real de cada comprobación después de ejecutarla.

| ID | Acción | Resultado esperado | Estado | Fecha y evidencia |
|---|---|---|---|---|
| S01 | Abrir catálogo sin sesión | HTTP 200 y cursos visibles | Pendiente | |
| S02 | Abrir Nuevo curso sin sesión | HTTP 302 hacia login | Pendiente | |
| S03 | Enviar credenciales inválidas | Mensaje genérico | Pendiente | |
| S04 | Iniciar sesión correctamente | Catálogo y sesión válida | Pendiente | |
| S05 | Enviar POST web sin CSRF | HTTP 403 Forbidden | Pendiente | |
| S06 | Crear curso desde formulario | Redirección y persistencia | Pendiente | |
| S07 | Cerrar sesión por POST | Sesión invalidada | Pendiente | |
| A01 | Consultar API sin Basic | HTTP 401 Unauthorized | Pendiente | |
| A02 | Consultar API con Basic | HTTP 200 y JSON | Pendiente | |
| A03 | Consultar API con clave errónea | HTTP 401 controlado | Pendiente | |
| P01 | Abrir perfil sin sesión | HTTP 302 hacia login | Pendiente | |
| P02 | Abrir perfil autenticado | HTTP 200 y nombre de usuario | Pendiente | |
| T01 | Ejecutar `./mvnw clean test` | BUILD SUCCESS | Pendiente | |

## Evidencias

- Catálogo accesible sin sesión.
- Redirección de una ruta privada hacia el formulario de acceso.
- Mensaje genérico al ingresar credenciales incorrectas.
- Sesión iniciada y cierre mediante POST.
- Rechazo de una escritura web sin token CSRF.
- API con respuesta 401 sin credenciales, 200 con credenciales correctas y 401 con clave incorrecta.
- Vista de perfil disponible solo para una sesión autenticada.
- Salida de `./mvnw clean test` y reporte Surefire.

No incluyas contraseñas, hashes, cookies, cabeceras `Authorization`, archivos `.env` ni identificadores de sesión en las evidencias.
