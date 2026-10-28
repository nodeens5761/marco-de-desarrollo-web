package pe.edu.utp.techlab.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.service.CourseService;

// Reto de la semana 10: concurrencia optimista con @Version
@SpringBootTest
@Transactional
class CourseOptimisticLockingIntegrationTests {

    @Autowired
    private CourseService service;

    @Test
    void segundaActualizacionConVersionVencidaEsRechazada() {
        String titulo = "Concurrencia " + System.nanoTime();
        var creado = service.crear(new CourseInputDto(titulo, 10));

        // Dos "usuarios" leen el mismo curso con la misma versión
        var lecturaA = service.buscarPorId(creado.id()).orElseThrow();
        var lecturaB = service.buscarPorId(creado.id()).orElseThrow();
        assertEquals(lecturaA.version(), lecturaB.version());

        // El usuario A guarda primero
        var guardadoA = service.actualizar(
            creado.id(),
            new CourseInputDto(titulo + " A", 20, lecturaA.version())
        );
        assertEquals(lecturaA.version() + 1, guardadoA.version());

        // El usuario B intenta guardar con la versión desactualizada
        assertThrows(
            ObjectOptimisticLockingFailureException.class,
            () -> service.actualizar(
                creado.id(),
                new CourseInputDto(titulo + " B", 30, lecturaB.version())
            )
        );
    }
}
