package pe.edu.utp.techlab.controller;

import pe.edu.utp.techlab.support.AbstractMySqlIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class CourseViewControllerTests extends AbstractMySqlIntegrationSupport {
    @Autowired
    private MockMvc mvc;

    @Test
    void catalogoMuestraLaVista() throws Exception {
        mvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/lista"));
    }

    @Test
    void formularioIncluyeCategorias() throws Exception {
        mvc.perform(get("/cursos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/formulario"))
                .andExpect(model().attributeExists("categorias", "curso"));
    }

    @Test
    void formularioConErroresConservaLaVistaYLasCategorias() throws Exception {
        mvc.perform(post("/cursos")
                        .param("titulo", "")
                        .param("horas", "0")
                        .param("categoriaId", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/formulario"))
                .andExpect(model().attributeHasFieldErrors("curso", "titulo", "horas", "categoriaId"))
                .andExpect(model().attributeExists("categorias"));
    }

    @Test
    void resumenAgrupaCursosPorCategoria() throws Exception {
        mvc.perform(get("/cursos/resumen"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/resumen"))
                .andExpect(model().attributeExists("resumen"));
    }
}
