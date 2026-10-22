package pe.edu.utp.techlab.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.exception.CategoryNotFoundException;
import pe.edu.utp.techlab.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab.service.CourseService;

@WebMvcTest(CourseController.class)
class CourseControllerTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseService service;

    @Test
    void postValidoCreaCursoRelacionado() throws Exception {
        given(service.crear(any(CourseInputDto.class))).willReturn(
            new CourseDto(4L, "Java profesional", 24, 2L, "Back-end"));
        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Java profesional","horas":24,"categoriaId":2}
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/v1/cursos/4")))
            .andExpect(jsonPath("$.categoriaId").value(2))
            .andExpect(jsonPath("$.categoriaNombre").value("Back-end"));
    }

    @Test
    void postInvalidoDevuelveErroresPorCampo() throws Exception {
        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":" ","horas":0,"categoriaId":null}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Datos inválidos"))
            .andExpect(jsonPath("$.errors.titulo").exists())
            .andExpect(jsonPath("$.errors.horas").exists())
            .andExpect(jsonPath("$.errors.categoriaId").exists());
    }


    @Test
    void tituloDuplicadoDevuelve409() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
            .willThrow(new DuplicateCourseTitleException("Java profesional"));
        mvc.perform(post("/api/v1/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Java profesional","horas":24,"categoriaId":2}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Conflicto de datos"));
    }

    @Test
    void filtroSinCategoriaDevuelveLista() throws Exception {
        given(service.buscar("", null)).willReturn(List.of());
        mvc.perform(get("/api/v1/cursos"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void filtroPorCategoriaValidaDevuelveLista() throws Exception {
        given(service.buscar("", 2L)).willReturn(
            List.of(new CourseDto(4L, "Java profesional", 24, 2L, "Back-end")));
        mvc.perform(get("/api/v1/cursos").param("categoriaId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].categoriaId").value(2))
            .andExpect(jsonPath("$[0].categoriaNombre").value("Back-end"));
    }

    @Test
    void filtroPorCategoriaInexistenteDevuelve400() throws Exception {
        given(service.buscar("", 999L)).willThrow(new CategoryNotFoundException(999L));
        mvc.perform(get("/api/v1/cursos").param("categoriaId", "999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Categoría inválida"))
            .andExpect(jsonPath("$.errors.categoriaId").exists());
    }
}
