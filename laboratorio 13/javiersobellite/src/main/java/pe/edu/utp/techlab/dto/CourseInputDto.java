package pe.edu.utp.techlab.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CourseInputDto(
        @NotBlank(message = "{course.title.required}")
        @Size(max = 120, message = "{course.title.size}")
        String titulo,
        @NotNull(message = "{course.hours.required}")
        @Min(value = 1, message = "{course.hours.min}")
        @Max(value = 500, message = "{course.hours.max}")
        Integer horas,
        @NotNull(message = "{course.category.required}")
        @Positive(message = "{course.category.positive}")
        Long categoriaId) {
}
