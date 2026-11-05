# Pruebas manuales de la API

La API de esta semana permanece pública. No se agregó Spring Security.

## Listar cursos

```bash
curl -i http://localhost:8080/api/v1/cursos
```

## Consultar un curso

```bash
curl -i http://localhost:8080/api/v1/cursos/1
```

## Crear curso (201)

```bash
curl -i -X POST http://localhost:8080/api/v1/cursos \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Seguridad web","horas":24,"categoriaId":2}'
```

## Datos inválidos (400)

```bash
curl -i -X POST http://localhost:8080/api/v1/cursos \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"","horas":0,"categoriaId":999999}'
```

## Actualizar curso (200)

```bash
curl -i -X PUT http://localhost:8080/api/v1/cursos/1 \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Programación con Java actualizada","horas":36,"categoriaId":1}'
```

## Eliminar curso (204)

```bash
curl -i -X DELETE http://localhost:8080/api/v1/cursos/1
```

## Curso inexistente (404)

```bash
curl -i http://localhost:8080/api/v1/cursos/99999999
```

## Título duplicado (409)

```bash
curl -i -X POST http://localhost:8080/api/v1/cursos \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Programación con Java","horas":24,"categoriaId":1}'
```

Usa títulos distintos durante las pruebas para evitar conflictos con registros creados anteriormente.
