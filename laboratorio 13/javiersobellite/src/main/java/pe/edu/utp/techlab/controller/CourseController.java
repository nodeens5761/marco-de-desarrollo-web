package pe.edu.utp.techlab.controller;

import jakarta.validation.Valid;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cursos")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseDto> list() {
        return courseService.getCourses();
    }

    @GetMapping("/{id}")
    public CourseDto detail(@PathVariable Long id) {
        return courseService.getCourse(id);
    }

    @PostMapping
    public ResponseEntity<CourseDto> create(@Valid @RequestBody CourseInputDto input) {
        CourseDto created = courseService.createCourse(input);
        return ResponseEntity.created(URI.create("/api/v1/cursos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public CourseDto update(@PathVariable Long id, @Valid @RequestBody CourseInputDto input) {
        return courseService.updateCourse(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
