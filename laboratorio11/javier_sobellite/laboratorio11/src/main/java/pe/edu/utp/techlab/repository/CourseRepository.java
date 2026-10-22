package pe.edu.utp.techlab.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.techlab.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = "categoria")
    List<Course> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "categoria")
    List<Course> findByTituloContainingIgnoreCaseOrderByIdAsc(String titulo);

    @EntityGraph(attributePaths = "categoria")
    List<Course> findByCategoria_IdOrderByIdAsc(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    List<Course> findByTituloContainingIgnoreCaseAndCategoria_IdOrderByIdAsc(
        String titulo,
        Long categoriaId
    );

    @Query("""
        select c
        from Course c
        join fetch c.categoria
        where c.id = :id
        """)
    Optional<Course> findDetailById(@Param("id") Long id);

    boolean existsByTituloIgnoreCase(String titulo);

    boolean existsByTituloIgnoreCaseAndIdNot(String titulo, Long id);

    @Query("select sum(c.horas) from Course c")
    Long sumarHoras();
}
