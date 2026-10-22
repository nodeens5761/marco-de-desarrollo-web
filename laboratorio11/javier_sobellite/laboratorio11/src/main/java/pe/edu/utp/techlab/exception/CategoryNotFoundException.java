package pe.edu.utp.techlab.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(long id) {
        super("La categoría seleccionada no existe: " + id);
    }
}
