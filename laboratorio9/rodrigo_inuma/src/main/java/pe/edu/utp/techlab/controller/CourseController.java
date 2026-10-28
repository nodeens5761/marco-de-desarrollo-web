package pe.edu.utp.techlab.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.service.CourseService;

/**
 * API REST de la semana 6. Se conserva sin cambios funcionales:
 * /api/v1/cursos sigue devolviendo JSON.
 */
@RestController
@RequestMapping("/api/v1/cursos")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseDto> listar(@RequestParam(defaultValue = "") String q) {
        return service.buscar(q);
    }

    @GetMapping("/{id}")
    public CourseDto detalle(@PathVariable long id) {
        return service.buscarPorId(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Curso no encontrado"));
    }
}
