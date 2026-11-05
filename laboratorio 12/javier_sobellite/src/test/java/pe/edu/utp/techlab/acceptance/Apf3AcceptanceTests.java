package pe.edu.utp.techlab.acceptance;

import pe.edu.utp.techlab.support.AbstractMySqlIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class Apf3AcceptanceTests extends AbstractMySqlIntegrationSupport {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void esquemaTieneLasTresMigraciones() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1 AND version IN ('1', '2', '3')",
                Integer.class);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void catalogoMvcRespondeConVista() throws Exception {
        mvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(view().name("cursos/lista"));
    }

    @Test
    void apiRespondeJsonConCategoria() throws Exception {
        mvc.perform(get("/api/v1/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoriaId").isNumber())
                .andExpect(jsonPath("$[0].categoriaNombre").isString());
    }
}
