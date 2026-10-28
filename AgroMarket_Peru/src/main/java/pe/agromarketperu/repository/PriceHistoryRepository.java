package pe.agromarketperu.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.agromarketperu.model.PriceHistory;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    List<PriceHistory> findByProducto_IdOrderByRegistradoEnAsc(Integer productoId);
}
