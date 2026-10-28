package pe.edu.utp.techlab.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.entity.Course;
import pe.edu.utp.techlab.exception.InvalidCourseDataException;
import pe.edu.utp.techlab.service.CourseService;

@WebMvcTest(CourseController.class)
class CourseControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseService service;

    @Test
    void getListaYFiltraCursos() throws Exception {
        var spring = new CourseDto(3L, "Spring Boot", 20, "Backend", 0L);
        given(service.buscar("boot")).willReturn(List.of(spring));

        mvc.perform(get("/api/v1/cursos").param("q", "boot"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(jsonPath("$[0].titulo").value("Spring Boot"))
            .andExpect(jsonPath("$[0].categoria").value("Backend"));
    }

    @Test
    void getDevuelve404ParaIdInexistente() throws Exception {
        given(service.buscarPorId(999L)).willReturn(Optional.empty());

        mvc.perform(get("/api/v1/cursos/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void postCreaRecursoYDevuelveLocation() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
            .willReturn(new CourseDto(4L, "Java profesional", 24, "General", 0L));

        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"titulo\":\"Java profesional\",\"horas\":24}"
                ))
            .andExpect(status().isCreated())
            .andExpect(header().string(
                "Location",
                endsWith("/api/v1/cursos/4")
            ))
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.titulo").value("Java profesional"));
    }

    @Test
    void putActualizaYDeleteElimina() throws Exception {
        given(service.actualizar(eq(4L), any(CourseInputDto.class)))
            .willReturn(new CourseDto(4L, "Java web", 32, "General", 1L));

        mvc.perform(put("/api/v1/cursos/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Java web\",\"horas\":32}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.horas").value(32));

        mvc.perform(delete("/api/v1/cursos/4"))
            .andExpect(status().isNoContent());
    }

    @Test
    void errorDeEntradaUsaProblemDetail() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
            .willThrow(new InvalidCourseDataException(
                "El título es obligatorio"
            ));

        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"\",\"horas\":24}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON
            ))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Datos inválidos"))
            .andExpect(jsonPath("$.detail").value(
                "El título es obligatorio"
            ));
    }

    // Reto: conflicto de versión -> 409
    @Test
    void versionVencidaDevuelve409() throws Exception {
        given(service.actualizar(eq(4L), any(CourseInputDto.class)))
            .willThrow(new ObjectOptimisticLockingFailureException(
                Course.class, 4L
            ));

        mvc.perform(put("/api/v1/cursos/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"titulo\":\"Java web\",\"horas\":32,\"version\":0}"
                ))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON
            ))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Conflicto de versión"));
    }
}
