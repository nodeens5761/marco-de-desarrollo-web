package pe.agromarketperu.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistory(int id, int productoId, BigDecimal precio, LocalDateTime registradoEn) {
}
