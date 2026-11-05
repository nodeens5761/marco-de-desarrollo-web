package pe.edu.utp.techlab.repository;

import pe.edu.utp.techlab.domain.Course;
import pe.edu.utp.techlab.dto.CategoryCourseCountDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = "category")
    List<Course> findAllByOrderByTituloAsc();

    @Query("select c from Course c join fetch c.category where c.id = :id")
    Optional<Course> findByIdWithCategory(@Param("id") Long id);

    boolean existsByTituloIgnoreCase(String titulo);

    boolean existsByTituloIgnoreCaseAndIdNot(String titulo, Long id);

    @Query("select new pe.edu.utp.techlab.dto.CategoryCourseCountDto(c.category.nombre, count(c)) " +
            "from Course c group by c.category.id, c.category.nombre order by c.category.nombre")
    List<CategoryCourseCountDto> countCoursesByCategory();
}
