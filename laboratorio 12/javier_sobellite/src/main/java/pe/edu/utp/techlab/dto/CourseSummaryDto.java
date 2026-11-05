package pe.edu.utp.techlab.dto;

import java.util.List;

public record CourseSummaryDto(long totalCursos, List<CategoryCourseCountDto> categorias) {
    public CourseSummaryDto {
        categorias = List.copyOf(categorias);
    }
}
