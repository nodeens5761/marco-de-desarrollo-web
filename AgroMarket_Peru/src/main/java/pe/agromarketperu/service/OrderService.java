package pe.agromarketperu.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.agromarketperu.model.Product;

@Service
public class OrderService {
    private final JdbcTemplate db;
    private final ProductService products;

    public OrderService(JdbcTemplate db, ProductService products) {
        this.db = db;
        this.products = products;
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body, Map<String, Object> user) {
        if (!"user".equals(user.get("rol")))
            throw new IllegalArgumentException("Solo clientes pueden comprar");
        Map<String, Object> c = (Map<String, Object>) body.get("cliente");
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("items");
        if (c == null || raw == null || raw.isEmpty())
            throw new IllegalArgumentException("Datos de compra incompletos");
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> i : raw) {
            int id = ((Number) i.get("id")).intValue();
            int qty = ((Number) i.get("cantidad")).intValue();
            if (qty < 1)
                throw new IllegalArgumentException("Cantidad inválida");
            Product p = products.one(id);
            if (p == null)
                throw new IllegalArgumentException("Producto no encontrado");
            BigDecimal sub = p.precio().multiply(BigDecimal.valueOf(qty));
            total = total.add(sub);
            items.add(Map.of("id", id, "nombre", p.nombre(), "tienda", p.tienda(), "precio", p.precio(), "cantidad",
                    qty, "subtotal", sub));
        }
        String numero = "AMP-" + String.valueOf(System.currentTimeMillis()).substring(3);
        String fecha = "Entrega estimada entre hoy y mañana";
        BigDecimal totalFinal = total;
        db.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO pedidos(numero,usuario_id,nombre_cliente,correo,telefono,dni,direccion,distrito,referencia,metodo_pago,total,estado,fecha_entrega) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, numero);
            ps.setObject(2, user.get("id"));
            ps.setString(3, String.valueOf(c.getOrDefault("nombre", "")));
            ps.setString(4, String.valueOf(c.getOrDefault("correo", "")));
            ps.setString(5, String.valueOf(c.getOrDefault("telefono", "")));
            ps.setString(6, String.valueOf(c.getOrDefault("dni", "")));
            ps.setString(7, String.valueOf(c.getOrDefault("direccion", "")));
            ps.setString(8, String.valueOf(c.getOrDefault("distrito", "")));
            ps.setString(9, String.valueOf(c.getOrDefault("referencia", "")));
            ps.setString(10, String.valueOf(c.getOrDefault("metodoPago", "")));
            ps.setBigDecimal(11, totalFinal);
            ps.setString(12, "Confirmado");
            ps.setString(13, fecha);
            return ps;
        });
        Integer pedidoId = db.queryForObject("SELECT id FROM pedidos WHERE numero=?", Integer.class, numero);
        for (Map<String, Object> i : items)
            db.update(
                    "INSERT INTO detalle_pedido(pedido_id,producto_id,producto_nombre,tienda,precio,cantidad,subtotal) VALUES(?,?,?,?,?,?,?)",
                    pedidoId, i.get("id"), i.get("nombre"), i.get("tienda"), i.get("precio"), i.get("cantidad"),
                    i.get("subtotal"));
        return Map.of("ok", true, "numero", numero, "fechaEntrega", fecha, "total", totalFinal);
    }

    public List<Map<String, Object>> mine(int userId) {
        List<Map<String, Object>> orders = db.query("SELECT * FROM pedidos WHERE usuario_id=? ORDER BY created_at DESC",
                (rs, n) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getInt("id"));
                    m.put("numero", rs.getString("numero"));
                    m.put("total", rs.getBigDecimal("total"));
                    m.put("estado", rs.getString("estado"));
                    m.put("fecha_entrega", rs.getString("fecha_entrega"));
                    m.put("created_at", rs.getTimestamp("created_at"));
                    m.put("items", db.query("SELECT * FROM detalle_pedido WHERE pedido_id=?", (d, k) -> {
                        Map<String, Object> x = new LinkedHashMap<>();
                        x.put("producto_id", d.getInt("producto_id"));
                        x.put("producto_nombre", d.getString("producto_nombre"));
                        x.put("tienda", d.getString("tienda"));
                        x.put("precio", d.getBigDecimal("precio"));
                        x.put("cantidad", d.getInt("cantidad"));
                        x.put("subtotal", d.getBigDecimal("subtotal"));
                        return x;
                    }, rs.getInt("id")));
                    return m;
                }, userId);
        return orders;
    }
}
