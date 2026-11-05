CREATE TABLE categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    CONSTRAINT pk_categorias PRIMARY KEY (id),
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO categorias (nombre) VALUES
    ('Programación'),
    ('Desarrollo Web'),
    ('Bases de Datos');

ALTER TABLE cursos ADD COLUMN categoria_id BIGINT NULL;

UPDATE cursos
SET categoria_id = (SELECT id FROM categorias WHERE nombre = 'Programación')
WHERE titulo IN ('Programación con Java', 'Programación Orientada a Objetos');

UPDATE cursos
SET categoria_id = (SELECT id FROM categorias WHERE nombre = 'Desarrollo Web')
WHERE titulo IN ('Desarrollo Web con Spring', 'Fundamentos de HTML y CSS');

UPDATE cursos
SET categoria_id = (SELECT id FROM categorias WHERE nombre = 'Bases de Datos')
WHERE titulo = 'Base de Datos MySQL';

ALTER TABLE cursos MODIFY categoria_id BIGINT NOT NULL;

CREATE INDEX idx_cursos_categoria_id ON cursos (categoria_id);

ALTER TABLE cursos
    ADD CONSTRAINT fk_cursos_categoria
    FOREIGN KEY (categoria_id) REFERENCES categorias (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;
