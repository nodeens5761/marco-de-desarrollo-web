SELECT installed_rank, version, description, type, success
FROM flyway_schema_history
ORDER BY installed_rank;

SHOW CREATE TABLE categorias;
SHOW CREATE TABLE cursos;

SELECT c.id, c.titulo, c.horas, c.categoria_id, ca.nombre AS categoria
FROM cursos c
JOIN categorias ca ON ca.id = c.categoria_id
ORDER BY c.id;
