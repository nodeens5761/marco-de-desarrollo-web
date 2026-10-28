package pe.edu.utp.techlab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "cursos")
public class Course {

    private static final String CATEGORIA_POR_DEFECTO = "General";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "horas", nullable = false)
    private int horas;

    @Column(name = "categoria", nullable = false, length = 40)
    private String categoria;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Course() {
    }

    private Course(String titulo, int horas) {
        this.titulo = titulo;
        this.horas = horas;
        this.categoria = CATEGORIA_POR_DEFECTO;
    }

    public static Course crear(String titulo, int horas) {
        return new Course(titulo, horas);
    }

    public void actualizar(String titulo, int horas) {
        this.titulo = titulo;
        this.horas = horas;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getHoras() {
        return horas;
    }

    public String getCategoria() {
        return categoria;
    }

    public Long getVersion() {
        return version;
    }
}
