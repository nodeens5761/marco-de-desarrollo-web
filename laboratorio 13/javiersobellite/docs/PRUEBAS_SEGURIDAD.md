# Pruebas manuales de seguridad

## Interfaz web

| Solicitud | Resultado esperado |
|---|---|
| `GET /cursos` | 200 y catálogo público |
| `GET /cursos/nuevo` sin sesión | 302 hacia `/login` |
| `GET /cursos/1/editar` sin sesión | 302 hacia `/login` |
| `POST /cursos` sin CSRF | 403 |
| `GET /perfil` sin sesión | 302 hacia `/login` |
| `POST /logout` con sesión y CSRF | Redirección hacia `/login?logout` |

Inicia sesión en `http://localhost:8080/login` con el usuario y la contraseña definidos en las variables de entorno.

## API REST

En Linux o macOS define la contraseña antes de ejecutar los comandos:

```bash
export APP_DEMO_USERNAME=docente
export APP_DEMO_PASSWORD='Tu_Clave_Local'
```

Sin credenciales:

```bash
curl -i http://localhost:8080/api/v1/cursos
```

Con credenciales correctas:

```bash
curl -i -u "$APP_DEMO_USERNAME:$APP_DEMO_PASSWORD" http://localhost:8080/api/v1/cursos
```

Con contraseña incorrecta:

```bash
curl -i -u "$APP_DEMO_USERNAME:incorrecta" http://localhost:8080/api/v1/cursos
```

Los resultados esperados son 401, 200 y 401, respectivamente. No guardes credenciales reales en archivos de solicitudes ni capturas.

## Pruebas automatizadas

```bash
./mvnw -Dtest=WebSecurityTests test
./mvnw -Dtest=ApiSecurityTests test
./mvnw clean test
```
