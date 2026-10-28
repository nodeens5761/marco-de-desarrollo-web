package pe.agromarketperu.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "productos", uniqueConstraints = @UniqueConstraint(name = "uk_producto_tienda", columnNames = {"nombre", "tienda"}))
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String nombre;
    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String categoria;
    @NotBlank @Pattern(regexp = "Plaza Vea|Tottus|Metro", message = "Selecciona Plaza Vea, Tottus o Metro") @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String tienda;
    @NotNull @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero") @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    @NotBlank @Size(max = 500) @Pattern(regexp = "https?://.+", message = "La imagen debe ser una URL http o https")
    @Column(nullable = false, length = 500)
    private String imagen;
    @NotBlank @Size(max = 500) @Pattern(regexp = "https?://.+", message = "El enlace debe ser una URL http o https")
    @Column(nullable = false, length = 500)
    private String url;
    @Size(max = 220)
    @Column(name = "clave_comparable", length = 220)
    private String claveComparable;
    @Column(nullable = false, length = 20)
    private String origen = "DEMO";
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    @JsonIgnore
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("registradoEn ASC")
    private List<PriceHistory> historial = new ArrayList<>();

    public Product() {}

    public Product(String nombre, String categoria, String tienda, BigDecimal precio, String imagen, String url, String claveComparable, String origen) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.tienda = tienda;
        this.precio = precio;
        this.imagen = imagen;
        this.url = url;
        this.claveComparable = claveComparable;
        this.origen = origen;
    }

    @PrePersist
    @PreUpdate
    public void preparar() {
        if (claveComparable == null || claveComparable.isBlank()) claveComparable = ProductComparisonKey.fromName(nombre);
        if (origen == null || origen.isBlank()) origen = "DEMO";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; this.claveComparable = ProductComparisonKey.fromName(nombre); }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getTienda() { return tienda; }
    public void setTienda(String tienda) { this.tienda = tienda; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getClaveComparable() { return claveComparable; }
    public void setClaveComparable(String claveComparable) { this.claveComparable = claveComparable; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public List<PriceHistory> getHistorial() { return historial; }
    public String nombre() { return nombre; }
    public String categoria() { return categoria; }
    public String tienda() { return tienda; }
    public BigDecimal precio() { return precio; }
    public String imagen() { return imagen; }
    public String url() { return url; }
    public int id() { return id == null ? 0 : id; }
}
