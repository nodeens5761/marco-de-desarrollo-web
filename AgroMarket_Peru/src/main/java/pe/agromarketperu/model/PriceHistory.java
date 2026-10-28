package pe.agromarketperu.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "historial_precios", indexes = @Index(name = "idx_historial_producto_fecha", columnList = "producto_id,registrado_en"))
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Product producto;
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    @NotNull
    @Column(name = "registrado_en", nullable = false)
    private LocalDateTime registradoEn;
    @Size(max = 20)
    @Column(length = 20)
    private String origen;

    public PriceHistory() {}
    public PriceHistory(Product producto, BigDecimal precio, LocalDateTime registradoEn) {
        this.producto = producto;
        this.precio = precio;
        this.registradoEn = registradoEn;
        this.origen = producto == null ? "DEMO" : producto.getOrigen();
    }
    @PrePersist
    public void preparar() {
        if (registradoEn == null) registradoEn = LocalDateTime.now().withNano(0);
        if (origen == null || origen.isBlank()) origen = producto == null ? "DEMO" : producto.getOrigen();
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProducto() { return producto; }
    public void setProducto(Product producto) { this.producto = producto; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public LocalDateTime getRegistradoEn() { return registradoEn; }
    public void setRegistradoEn(LocalDateTime registradoEn) { this.registradoEn = registradoEn; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public String origen() { return origen; }
    public int productoId() { return producto == null || producto.getId() == null ? 0 : producto.getId(); }
    public BigDecimal precio() { return precio; }
    public LocalDateTime registradoEn() { return registradoEn; }
}
