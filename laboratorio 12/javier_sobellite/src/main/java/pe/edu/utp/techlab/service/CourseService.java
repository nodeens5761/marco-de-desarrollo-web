package pe.edu.utp.techlab.service;

import pe.edu.utp.techlab.domain.Category;
import pe.edu.utp.techlab.domain.Course;
import pe.edu.utp.techlab.dto.CategoryDto;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.dto.CourseSummaryDto;
import pe.edu.utp.techlab.exception.CategoryNotFound;
import pe.edu.utp.techlab.exception.CourseNotFound;
import pe.edu.utp.techlab.exception.DuplicateCourseTitle;
import pe.edu.utp.techlab.exception.InvalidCourseData;
import pe.edu.utp.techlab.mapper.CourseMapper;
import pe.edu.utp.techlab.repository.CategoryRepository;
import pe.edu.utp.techlab.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CourseMapper mapper;

    public CourseService(CourseRepository courseRepository, CategoryRepository categoryRepository, CourseMapper mapper) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    public List<CourseDto> getCourses() {
        return courseRepository.findAllByOrderByTituloAsc().stream().map(mapper::toDto).toList();
    }

    public CourseDto getCourse(Long id) {
        return mapper.toDto(findCourse(id));
    }

    public List<CategoryDto> getCategories() {
        return categoryRepository.findAllByOrderByNombreAsc().stream().map(mapper::toDto).toList();
    }

    @Transactional
    public CourseDto createCourse(CourseInputDto input) {
        validateInput(input);
        String titulo = input.titulo().trim();
        if (courseRepository.existsByTituloIgnoreCase(titulo)) {
            throw new DuplicateCourseTitle();
        }
        Category category = findCategory(input.categoriaId());
        Course course = new Course(titulo, input.horas(), category);
        return mapper.toDto(courseRepository.save(course));
    }

    @Transactional
    public CourseDto updateCourse(Long id, CourseInputDto input) {
        validateInput(input);
        Course course = findCourse(id);
        String titulo = input.titulo().trim();
        if (courseRepository.existsByTituloIgnoreCaseAndIdNot(titulo, id)) {
            throw new DuplicateCourseTitle();
        }
        Category category = findCategory(input.categoriaId());
        course.update(titulo, input.horas(), category);
        return mapper.toDto(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id) {
        courseRepository.delete(findCourse(id));
    }

    public CourseSummaryDto getSummary() {
        return new CourseSummaryDto(courseRepository.count(), courseRepository.countCoursesByCategory());
    }

    private Course findCourse(Long id) {
        return courseRepository.findByIdWithCategory(id).orElseThrow(() -> new CourseNotFound(id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFound(id));
    }

    private void validateInput(CourseInputDto input) {
        if (input == null || input.titulo() == null || input.titulo().isBlank()
                || input.horas() == null || input.horas() < 1 || input.horas() > 500
                || input.categoriaId() == null || input.categoriaId() < 1) {
            throw new InvalidCourseData("Los datos del curso no son válidos.");
        }
        if (input.titulo().trim().length() > 120) {
            throw new InvalidCourseData("El título no puede superar los 120 caracteres.");
        }
    }
}
