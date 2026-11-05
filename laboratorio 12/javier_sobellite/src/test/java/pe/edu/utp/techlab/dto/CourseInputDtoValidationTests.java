package pe.edu.utp.techlab.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseInputDtoValidationTests {
    private static Validator validator;
    private static jakarta.validation.ValidatorFactory factory;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    void aceptaDatosValidos() {
        CourseInputDto input = new CourseInputDto("Spring MVC", 24, 1L);
        assertThat(validator.validate(input)).isEmpty();
    }

    @Test
    void rechazaTituloHorasYCategoriaInvalidos() {
        CourseInputDto input = new CourseInputDto(" ", 0, null);
        assertThat(validator.validate(input))
                .extracting(error -> error.getPropertyPath().toString())
                .contains("titulo", "horas", "categoriaId");
    }

    @Test
    void limitaElTamanoDelTitulo() {
        CourseInputDto input = new CourseInputDto("a".repeat(121), 10, 1L);
        assertThat(validator.validate(input)).extracting(error -> error.getPropertyPath().toString())
                .contains("titulo");
    }
}
