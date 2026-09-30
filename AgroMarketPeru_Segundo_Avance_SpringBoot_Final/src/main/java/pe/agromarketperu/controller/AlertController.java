package pe.agromarketperu.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alertas")
public class AlertController {
    private final JdbcTemplate db;

    public AlertController(JdbcTemplate db) {
        this.db = db;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> b, HttpServletRequest r) {
        Map<String, Object> u = (Map<String, Object>) r.getAttribute("user");
        if (!"user".equals(u.get("rol")))
            throw new IllegalArgumentException("Solo clientes pueden crear alertas");
        db.update("INSERT INTO alertas(usuario_id,producto_id,producto_nombre,precio_objetivo) VALUES(?,?,?,?)",
                u.get("id"), b.get("productoId"), b.get("productoNombre"), b.get("precioObjetivo"));
        return Map.of("ok", true);
    }

    @GetMapping
    public List<Map<String, Object>> list(HttpServletRequest r) {
        Map<String, Object> u = (Map<String, Object>) r.getAttribute("user");
        return db.queryForList("SELECT * FROM alertas WHERE usuario_id=? ORDER BY created_at DESC", u.get("id"));
    }
}
