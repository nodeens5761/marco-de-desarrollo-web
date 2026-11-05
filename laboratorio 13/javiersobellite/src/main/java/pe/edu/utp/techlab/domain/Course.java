package pe.edu.utp.techlab.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cursos")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 120, unique = true)
    private String titulo;

    @Column(name = "horas", nullable = false)
    private Integer horas;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Category category;

    protected Course() {
    }

    public Course(String titulo, Integer horas, Category category) {
        this.titulo = titulo;
        this.horas = horas;
        this.category = category;
    }

    public void update(String titulo, Integer horas, Category category) {
        this.titulo = titulo;
        this.horas = horas;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getHoras() {
        return horas;
    }

    public Category getCategory() {
        return category;
    }
}
