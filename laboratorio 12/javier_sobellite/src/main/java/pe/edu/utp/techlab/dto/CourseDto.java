package pe.edu.utp.techlab.dto;

public record CourseDto(
        Long id,
        String titulo,
        Integer horas,
        Long categoriaId,
        String categoriaNombre) {
}
