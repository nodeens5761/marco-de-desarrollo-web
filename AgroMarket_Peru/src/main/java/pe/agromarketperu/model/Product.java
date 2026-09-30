package pe.agromarketperu.model;

import java.math.BigDecimal;

public record Product(int id, String nombre, String categoria, String tienda, BigDecimal precio, String imagen,
        String url) {
}
