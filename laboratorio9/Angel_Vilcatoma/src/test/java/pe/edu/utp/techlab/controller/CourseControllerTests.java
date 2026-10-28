package pe.edu.utp.techlab.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.service.CourseService;

@WebMvcTest(CourseController.class)
class CourseControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseService service;

    @Test
    void apiListaYFiltraCursos() throws Exception {
        var spring = new CourseDto(3L, "Spring Boot", 20, "Backend");
        given(service.buscar("boot")).willReturn(List.of(spring));

        mvc.perform(get("/api/v1/cursos").param("q", "boot"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(jsonPath("$[0].titulo").value("Spring Boot"))
            .andExpect(jsonPath("$[0].categoria").value("Backend"));
    }

    @Test
    void apiDevuelve404ParaIdInexistente() throws Exception {
        given(service.buscarPorId(999L)).willReturn(Optional.empty());

        mvc.perform(get("/api/v1/cursos/999"))
            .andExpect(status().isNotFound());
    }
}
