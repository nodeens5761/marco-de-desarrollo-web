package pe.edu.utp.techlab.exception;

public class CourseNotFound extends RuntimeException {
    public CourseNotFound(Long id) {
        super("No existe el curso solicitado.");
    }
}
