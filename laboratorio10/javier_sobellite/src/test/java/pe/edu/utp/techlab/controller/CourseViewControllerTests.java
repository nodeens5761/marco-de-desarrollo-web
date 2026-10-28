package pe.edu.utp.techlab.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab.service.CourseService;

@WebMvcTest({CourseViewController.class, PortalController.class})
class CourseViewControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseService service;

    private CourseDto bootstrap;

    @BeforeEach
    void prepararDatos() {
        bootstrap = new CourseDto(2L, "Bootstrap", 16, "Frontend", 0L);
        given(service.buscarPorId(2L))
            .willReturn(Optional.of(bootstrap));
        given(service.buscar("")).willReturn(List.of(
            new CourseDto(1L, "HTML y CSS", 12, "Frontend", 0L),
            bootstrap,
            new CourseDto(3L, "Spring Boot", 20, "Backend", 0L)
        ));
    }

    @Test
    void listadoRenderizaModeloYHtml() throws Exception {
        mvc.perform(get("/cursos"))
            .andExpect(status().isOk())
            .andExpect(view().name("cursos/lista"))
            .andExpect(model().attribute("cursos", hasSize(3)))
            .andExpect(content().string(
                containsString("Catálogo de cursos")
            ));
    }

    @Test
    void resumenMuestraCantidadYHoras() throws Exception {
        given(service.cantidad()).willReturn(3L);
        given(service.totalHoras()).willReturn(48);

        mvc.perform(get("/cursos/resumen"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("cantidadCursos", 3L))
            .andExpect(model().attribute("totalHoras", 48));
    }

    @Test
    void formularioNuevoTieneObjetoDeEntrada() throws Exception {
        mvc.perform(get("/cursos/nuevo"))
            .andExpect(status().isOk())
            .andExpect(view().name("cursos/formulario"))
            .andExpect(model().attributeExists("form"))
            .andExpect(model().attribute("tituloPagina", "Nuevo curso"))
            .andExpect(content().string(
                containsString("Registrar curso")
            ));
    }

    @Test
    void crearValidoAplicaPostRedirectGet() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
            .willReturn(new CourseDto(4L, "Java profesional", 24, "General", 0L));

        mvc.perform(post("/cursos")
                .param("titulo", "Java profesional")
                .param("horas", "24"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/cursos/4"))
            .andExpect(flash().attribute(
                "mensaje",
                "Curso registrado correctamente"
            ));
    }

    @Test
    void duplicadoRegresaAlFormularioConLosDatos() throws Exception {
        given(service.crear(any(CourseInputDto.class)))
            .willThrow(new DuplicateCourseTitleException("Bootstrap"));

        mvc.perform(post("/cursos")
                .param("titulo", "Bootstrap")
                .param("horas", "18"))
            .andExpect(status().isOk())
            .andExpect(view().name("cursos/formulario"))
            .andExpect(model().attributeExists("form"))
            .andExpect(model().attribute(
                "error",
                "Ya existe un curso con el título Bootstrap"
            ));
    }

    @Test
    void editarCargaDatosYEliminarRedirige() throws Exception {
        mvc.perform(get("/cursos/2/editar"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("cursoId", 2L))
            .andExpect(model().attribute("tituloPagina", "Editar curso"));

        willDoNothing().given(service).eliminar(2L);

        mvc.perform(post("/cursos/2/eliminar"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/cursos"))
            .andExpect(flash().attribute(
                "mensaje",
                "Curso eliminado correctamente"
            ));
    }

    @Test
    void editarIdInexistenteDevuelve404() throws Exception {
        given(service.buscarPorId(999L)).willReturn(Optional.empty());

        mvc.perform(get("/cursos/999/editar"))
            .andExpect(status().isNotFound());
    }

    @Test
    void erroresConservanLosEstadosHttp() throws Exception {
        mvc.perform(get("/cursos").param("q", "x".repeat(61)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void portalRedirigeAlCatalogo() throws Exception {
        mvc.perform(get("/portal"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/cursos"));
    }
}
