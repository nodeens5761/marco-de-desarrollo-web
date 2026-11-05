package pe.edu.utp.techlab.security;

import pe.edu.utp.techlab.config.SecurityConfig;
import pe.edu.utp.techlab.controller.CourseViewController;
import pe.edu.utp.techlab.controller.LoginController;
import pe.edu.utp.techlab.controller.ProfileController;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.dto.CategoryDto;
import pe.edu.utp.techlab.service.CourseService;
import pe.edu.utp.techlab.web.GlobalViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({
        CourseViewController.class,
        LoginController.class,
        ProfileController.class,
        GlobalViewModel.class
})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.demo-username=docente",
        "app.security.demo-password=Prueba-Segura-2026!"
})
class WebSecurityTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseService service;

    @BeforeEach
    void prepareData() {
        given(service.getCourses()).willReturn(List.of());
        given(service.getCategories()).willReturn(List.of(new CategoryDto(2L, "Back-end")));
    }

    @Test
    void loginEsPublico() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/lista"));
    }

    @Test
    void formularioProtegidoRedirigeAlLogin() throws Exception {
        mvc.perform(get("/cursos/nuevo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void usuarioAutenticadoAbreFormulario() throws Exception {
        mvc.perform(get("/cursos/nuevo").with(user("docente")))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/formulario"));
    }

    @Test
    void postSinCsrfEsRechazado() throws Exception {
        mvc.perform(post("/cursos")
                        .with(user("docente"))
                        .param("titulo", "Seguridad web")
                        .param("horas", "24")
                        .param("categoriaId", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postAutenticadoConCsrfContinuaAlControlador() throws Exception {
        given(service.createCourse(any(CourseInputDto.class)))
                .willReturn(new CourseDto(8L, "Seguridad web", 24, 2L, "Back-end"));

        mvc.perform(post("/cursos")
                        .with(user("docente"))
                        .with(csrf())
                        .param("titulo", "Seguridad web")
                        .param("horas", "24")
                        .param("categoriaId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cursos/8"));
    }

    @Test
    void logoutSeProcesaMediantePostConCsrf() throws Exception {
        mvc.perform(post("/logout")
                        .with(user("docente"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void perfilAnonimoRedirigeAlLogin() throws Exception {
        mvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void perfilAutenticadoMuestraElNombreDeUsuario() throws Exception {
        mvc.perform(get("/perfil").with(user("docente")))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/perfil"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("docente")));
    }
}
