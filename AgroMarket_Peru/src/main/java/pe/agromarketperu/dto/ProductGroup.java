package pe.agromarketperu.dto;

import java.math.BigDecimal;
import java.util.List;
import pe.agromarketperu.model.Product;

public class ProductGroup {
    private final String clave;
    private final String nombre;
    private final String categoria;
    private final String imagen;
    private final List<Product> ofertas;
    private final BigDecimal precioMinimo;

    public ProductGroup(String clave, String nombre, String categoria, String imagen, List<Product> ofertas) {
        this.clave = clave;
        this.nombre = nombre;
        this.categoria = categoria;
        this.imagen = imagen;
        this.ofertas = ofertas;
        this.precioMinimo = ofertas.stream().map(Product::getPrecio).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }
    public String getClave() { return clave; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getImagen() { return imagen; }
    public List<Product> getOfertas() { return ofertas; }
    public BigDecimal getPrecioMinimo() { return precioMinimo; }
    public String getTiendas() { return ofertas.stream().map(Product::getTienda).distinct().reduce((a,b) -> a + "|" + b).orElse(""); }
}
