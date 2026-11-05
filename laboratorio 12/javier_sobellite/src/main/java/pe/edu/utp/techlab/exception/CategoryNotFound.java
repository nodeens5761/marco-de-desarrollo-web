package pe.edu.utp.techlab.exception;

public class CategoryNotFound extends RuntimeException {
    public CategoryNotFound(Long id) {
        super("La categoría seleccionada no existe.");
    }
}
