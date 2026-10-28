package pe.edu.utp.techlab.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.repository.CourseRepository;
import pe.edu.utp.techlab.service.CourseService;

@SpringBootTest
class CoursePersistenceIntegrationTests {

    @Autowired
    private CourseService service;

    @Autowired
    private CourseRepository repository;

    @Test
    void flywayCargaElCatalogoInicial() {
        assertEquals(3L, service.cantidad());
        assertEquals(48, service.totalHoras());

        List<String> titulos = service.buscar("").stream()
            .map(CourseDto::titulo)
            .toList();

        assertEquals(
            List.of("HTML y CSS", "Bootstrap", "Spring Boot"),
            titulos
        );
    }

    @Test
    void repositorioBuscaSinDistinguirMayusculas() {
        var resultado = repository
            .findByTituloContainingIgnoreCaseOrderByIdAsc("boot");

        assertEquals(1, resultado.size());
        assertEquals("Spring Boot", resultado.getFirst().getTitulo());
    }

    // Reto: V3 asigna categorías y el repositorio filtra por ellas
    @Test
    void repositorioFiltraPorCategoria() {
        var frontend = repository
            .findByCategoriaIgnoreCaseOrderByIdAsc("frontend");
        var backend = repository
            .findByCategoriaIgnoreCaseOrderByIdAsc("Backend");

        assertEquals(2, frontend.size());
        assertEquals("HTML y CSS", frontend.getFirst().getTitulo());
        assertEquals(1, backend.size());
        assertEquals("Spring Boot", backend.getFirst().getTitulo());
    }
}
