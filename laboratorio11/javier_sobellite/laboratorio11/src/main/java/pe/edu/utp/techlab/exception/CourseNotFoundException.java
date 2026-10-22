package pe.edu.utp.techlab.exception;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(long id) {
        super("No existe un curso con el identificador " + id);
    }
}
