package pe.edu.utp.techlab.security;

import pe.edu.utp.techlab.config.SecurityConfig;
import pe.edu.utp.techlab.controller.CourseApiExceptionHandler;
import pe.edu.utp.techlab.controller.CourseController;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CourseController.class, CourseApiExceptionHandler.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.demo-username=docente",
        "app.security.demo-password=Prueba-Segura-2026!"
})
class ApiSecurityTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseService service;

    @Test
    void apiSinCredencialesResponde401() throws Exception {
        mvc.perform(get("/api/v1/cursos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiConCredencialesCorrectasResponde200() throws Exception {
        given(service.getCourses()).willReturn(List.of(
                new CourseDto(1L, "Spring Boot", 32, 2L, "Back-end")));

        mvc.perform(get("/api/v1/cursos")
                        .with(httpBasic("docente", "Prueba-Segura-2026!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Spring Boot"));
    }

    @Test
    void apiConClaveIncorrectaResponde401() throws Exception {
        mvc.perform(get("/api/v1/cursos")
                        .with(httpBasic("docente", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }
}
