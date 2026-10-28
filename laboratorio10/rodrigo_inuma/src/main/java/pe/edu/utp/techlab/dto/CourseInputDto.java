package pe.edu.utp.techlab.dto;

public class CourseInputDto {

    private String titulo;
    private Integer horas;
    // Reto: versión leída por el cliente (control de concurrencia optimista)
    private Long version;

    public CourseInputDto() {
    }

    public CourseInputDto(String titulo, Integer horas) {
        this.titulo = titulo;
        this.horas = horas;
    }

    public CourseInputDto(String titulo, Integer horas, Long version) {
        this.titulo = titulo;
        this.horas = horas;
        this.version = version;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getHoras() {
        return horas;
    }

    public void setHoras(Integer horas) {
        this.horas = horas;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
