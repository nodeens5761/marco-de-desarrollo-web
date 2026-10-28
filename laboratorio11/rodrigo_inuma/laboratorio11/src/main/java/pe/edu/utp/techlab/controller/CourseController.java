package pe.edu.utp.techlab.controller;

import java.net.URI;
import java.util.List;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.exception.CourseNotFoundException;
import pe.edu.utp.techlab.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/cursos")
public class CourseController {
    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseDto> listar(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Long categoriaId) {
        return service.buscar(q, categoriaId);
    }

    @GetMapping("/{id}")
    public CourseDto detalle(@PathVariable long id) {
        return service.buscarPorId(id).orElseThrow(() -> new CourseNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<CourseDto> crear(
            @Valid @RequestBody CourseInputDto input,
            UriComponentsBuilder uriBuilder) {
        CourseDto creado = service.crear(input);
        URI location = uriBuilder.path("/api/v1/cursos/{id}")
            .buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public CourseDto actualizar(
            @PathVariable long id,
            @Valid @RequestBody CourseInputDto input) {
        return service.actualizar(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
