package pe.edu.utp.techlab.repository;

import pe.edu.utp.techlab.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByNombreAsc();
}
