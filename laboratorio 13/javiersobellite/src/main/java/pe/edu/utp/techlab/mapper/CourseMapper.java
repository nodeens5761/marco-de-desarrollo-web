package pe.edu.utp.techlab.mapper;

import pe.edu.utp.techlab.domain.Category;
import pe.edu.utp.techlab.domain.Course;
import pe.edu.utp.techlab.dto.CategoryDto;
import pe.edu.utp.techlab.dto.CourseDto;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    public CourseDto toDto(Course course) {
        return new CourseDto(
                course.getId(),
                course.getTitulo(),
                course.getHoras(),
                course.getCategory().getId(),
                course.getCategory().getNombre());
    }

    public CategoryDto toDto(Category category) {
        return new CategoryDto(category.getId(), category.getNombre());
    }
}
