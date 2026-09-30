package pe.agromarketperu.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import pe.agromarketperu.model.PriceHistory;
import pe.agromarketperu.model.Product;

@Service
public class ProductService {
    private final JdbcTemplate db;

    public ProductService(JdbcTemplate db) {
        this.db = db;
    }

    public List<Product> all() {
        return db.query("SELECT id,nombre,categoria,tienda,precio,imagen,url FROM productos ORDER BY nombre,tienda",
                this::map);
    }

    public Product one(int id) {
        List<Product> r = db.query("SELECT id,nombre,categoria,tienda,precio,imagen,url FROM productos WHERE id=?",
                this::map, id);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<PriceHistory> history(int id) {
        return db.query(
                "SELECT id,producto_id,precio,registrado_en FROM historial_precios WHERE producto_id=? ORDER BY registrado_en",
                this::historyMap, id);
    }

    public Product create(Product p) {
        KeyHolder key = new GeneratedKeyHolder();
        db.update(c -> {
            var s = c.prepareStatement(
                    "INSERT INTO productos(nombre,categoria,tienda,precio,imagen,url) VALUES(?,?,?,?,?,?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            s.setString(1, p.nombre());
            s.setString(2, p.categoria());
            s.setString(3, p.tienda());
            s.setBigDecimal(4, p.precio());
            s.setString(5, p.imagen());
            s.setString(6, p.url());
            return s;
        }, key);
        int id = key.getKey().intValue();
        db.update("INSERT INTO historial_precios(producto_id,precio,registrado_en) VALUES(?,?,NOW())", id, p.precio());
        return one(id);
    }

    private Product map(ResultSet rs, int n) throws SQLException {
        return new Product(rs.getInt("id"), rs.getString("nombre"), rs.getString("categoria"), rs.getString("tienda"),
                rs.getBigDecimal("precio"), rs.getString("imagen"), rs.getString("url"));
    }

    private PriceHistory historyMap(ResultSet rs, int n) throws SQLException {
        return new PriceHistory(rs.getInt("id"), rs.getInt("producto_id"), rs.getBigDecimal("precio"),
                rs.getTimestamp("registrado_en").toLocalDateTime());
    }
}
