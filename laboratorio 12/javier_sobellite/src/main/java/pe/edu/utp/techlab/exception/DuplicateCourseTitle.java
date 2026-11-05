package pe.edu.utp.techlab.exception;

public class DuplicateCourseTitle extends RuntimeException {
    public DuplicateCourseTitle() {
        super("Ya existe un curso con ese título.");
    }
}
