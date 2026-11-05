# Pruebas HTTP

## Interfaz web

| Solicitud | Resultado esperado |
|---|---|
| `GET /cursos` | 200 y catálogo público |
| `GET /cursos/resumen` | 200 y resumen público |
| `GET /cursos/1` | 200 si el curso existe |
| `GET /cursos/nuevo` sin sesión | 302 hacia `/login` |
| `GET /cursos/1/editar` sin sesión | 302 hacia `/login` |
| `POST /cursos` sin sesión | 302 hacia `/login` |
| `POST /cursos` sin CSRF | 403 con sesión activa |

Los formularios de creación, edición y eliminación incluyen el token CSRF. El cierre de sesión se realiza mediante POST.

## API REST

La API exige HTTP Basic en cada solicitud. Define las variables `APP_DEMO_USERNAME` y `APP_DEMO_PASSWORD` en la terminal.

```bash
curl -i http://localhost:8080/api/v1/cursos
curl -i -u "$APP_DEMO_USERNAME:$APP_DEMO_PASSWORD" http://localhost:8080/api/v1/cursos
```

Resultados esperados: 401 sin credenciales y 200 con credenciales correctas. La API no utiliza sesión ni token CSRF.

Consulta `PRUEBAS_SEGURIDAD.md` para los casos de contraseña incorrecta, perfil y pruebas automatizadas.
