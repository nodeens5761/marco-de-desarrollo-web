package pe.edu.utp.techlab.controller;

import pe.edu.utp.techlab.exception.CategoryNotFound;
import pe.edu.utp.techlab.exception.CourseNotFound;
import pe.edu.utp.techlab.exception.DuplicateCourseTitle;
import pe.edu.utp.techlab.exception.InvalidCourseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = CourseController.class)
public class CourseApiExceptionHandler {
    @ExceptionHandler(CourseNotFound.class)
    public ProblemDetail notFound(CourseNotFound ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateCourseTitle.class)
    public ProblemDetail conflict(DuplicateCourseTitle ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({CategoryNotFound.class, InvalidCourseData.class})
    public ProblemDetail invalidData(RuntimeException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Revise los campos enviados.");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "El cuerpo JSON no tiene un formato válido.");
    }

    private ProblemDetail problem(HttpStatus status, String message) {
        return ProblemDetail.forStatusAndDetail(status, message);
    }
}
