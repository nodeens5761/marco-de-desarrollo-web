package pe.edu.utp.techlab.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.techlab.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOrderByIdAsc();

    List<Course> findByTituloContainingIgnoreCaseOrderByIdAsc(
        String titulo
    );

    List<Course> findByCategoriaIgnoreCaseOrderByIdAsc(String categoria);

    boolean existsByTituloIgnoreCase(String titulo);

    boolean existsByTituloIgnoreCaseAndIdNot(
        String titulo,
        Long id
    );

    @Query("select sum(c.horas) from Course c")
    Long sumarHoras();
}
