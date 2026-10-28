package pe.agromarketperu.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.agromarketperu.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findAllByOrderByNombreAscTiendaAsc();
    Optional<Product> findFirstByClaveComparableAndTiendaIgnoreCase(String claveComparable, String tienda);
    Optional<Product> findFirstByNombreIgnoreCaseAndTiendaIgnoreCase(String nombre, String tienda);
}
