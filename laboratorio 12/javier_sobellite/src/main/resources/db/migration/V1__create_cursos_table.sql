CREATE TABLE cursos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(120) NOT NULL,
    horas INT NOT NULL,
    CONSTRAINT pk_cursos PRIMARY KEY (id),
    CONSTRAINT uq_cursos_titulo UNIQUE (titulo),
    CONSTRAINT chk_cursos_horas CHECK (horas BETWEEN 1 AND 500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
