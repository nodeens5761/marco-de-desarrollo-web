package pe.edu.utp.techlab.controller;

import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.service.CourseService;
import pe.edu.utp.techlab.support.AbstractMySqlIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseControllerTests extends AbstractMySqlIntegrationSupport {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private CourseService courseService;

    @Test
    void listaCursosComoJsonConCategoria() throws Exception {
        mvc.perform(get("/api/v1/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoriaId").isNumber())
                .andExpect(jsonPath("$[0].categoriaNombre").isString());
    }

    @Test
    void creaCursoYDevuelve201ConLocation() throws Exception {
        String title = "API " + UUID.randomUUID();
        mvc.perform(post("/api/v1/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"%s","horas":24,"categoriaId":2}
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.titulo").value(title));
    }

    @Test
    void rechazaDatosInvalidosCon400() throws Exception {
        mvc.perform(post("/api/v1/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"horas\":0,\"categoriaId\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaTituloDuplicadoCon409() throws Exception {
        mvc.perform(post("/api/v1/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Programación con Java\",\"horas\":24,\"categoriaId\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizaYEliminaUnCursoCon200Y204() throws Exception {
        String title = "API edición " + UUID.randomUUID();
        var created = courseService.createCourse(new CourseInputDto(title, 20, 1L));

        mvc.perform(put("/api/v1/cursos/" + created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"%s actualizado","horas":30,"categoriaId":2}
                                """.formatted(title)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoriaNombre").value("Desarrollo Web"));

        mvc.perform(delete("/api/v1/cursos/" + created.id()))
                .andExpect(status().isNoContent());
    }

    @Test
    void devuelve404ParaUnCursoInexistente() throws Exception {
        mvc.perform(get("/api/v1/cursos/99999999"))
                .andExpect(status().isNotFound());
    }
}
