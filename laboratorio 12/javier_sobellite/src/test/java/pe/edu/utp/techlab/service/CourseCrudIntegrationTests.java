package pe.edu.utp.techlab.service;

import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.exception.CourseNotFound;
import pe.edu.utp.techlab.support.AbstractMySqlIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CourseCrudIntegrationTests extends AbstractMySqlIntegrationSupport {
    @Autowired
    private CourseService courseService;

    @Test
    void creaActualizaYEliminaUnCursoConCategoria() {
        String title = "Prueba CRUD " + UUID.randomUUID();
        CourseDto created = courseService.createCourse(new CourseInputDto(title, 18, 1L));

        assertThat(created.id()).isNotNull();
        assertThat(created.categoriaNombre()).isEqualTo("Programación");

        CourseDto updated = courseService.updateCourse(
                created.id(), new CourseInputDto(title + " editado", 20, 2L));
        assertThat(updated.titulo()).isEqualTo(title + " editado");
        assertThat(updated.categoriaNombre()).isEqualTo("Desarrollo Web");

        courseService.deleteCourse(created.id());
        assertThatThrownBy(() -> courseService.getCourse(created.id()))
                .isInstanceOf(CourseNotFound.class);
    }
}
