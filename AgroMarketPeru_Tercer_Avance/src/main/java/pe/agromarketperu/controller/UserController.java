package pe.agromarketperu.controller;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {
    private final JdbcTemplate db;

    public UserController(JdbcTemplate db) {
        this.db = db;
    }

    @GetMapping("/perfil")
    public Map<String, Object> profile(HttpServletRequest r) {
        Map<String, Object> u = (Map<String, Object>) r.getAttribute("user");
        return db.queryForMap("SELECT id,nombre,correo,telefono,dni,direccion,distrito FROM usuarios WHERE id=?",
                u.get("id"));
    }

    @PutMapping("/perfil")
    public Map<String, Object> update(@RequestBody Map<String, Object> b, HttpServletRequest r) {
        Map<String, Object> u = (Map<String, Object>) r.getAttribute("user");
        db.update("UPDATE usuarios SET nombre=?,telefono=?,dni=?,direccion=?,distrito=? WHERE id=?", b.get("nombre"),
                b.get("telefono"), b.get("dni"), b.get("direccion"), b.get("distrito"), u.get("id"));
        return Map.of("ok", true);
    }
}
