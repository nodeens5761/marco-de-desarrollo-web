package pe.agromarketperu.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final JdbcTemplate db;

    public AdminController(JdbcTemplate db) {
        this.db = db;
    }

    @GetMapping("/pedidos")
    public List<Map<String, Object>> orders(HttpServletRequest r) {
        Map<String, Object> u = (Map<String, Object>) r.getAttribute("user");
        if (!"admin".equals(u.get("rol")))
            throw new IllegalArgumentException("Acceso denegado");
        return db.queryForList(
                "SELECT numero,nombre_cliente,correo,total,estado,fecha_entrega,created_at FROM pedidos ORDER BY created_at DESC");
    }
}
