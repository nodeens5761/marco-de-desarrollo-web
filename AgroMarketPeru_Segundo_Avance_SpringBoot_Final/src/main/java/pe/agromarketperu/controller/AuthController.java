package pe.agromarketperu.controller;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.agromarketperu.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth){this.auth=auth;}
    @PostMapping("/register") public ResponseEntity<?> register(@RequestBody Map<String,Object> b){try{auth.register((String)b.get("nombre"),(String)b.get("correo"),(String)b.get("password"));return ResponseEntity.ok(Map.of("ok",true));}catch(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}catch(Exception e){return ResponseEntity.internalServerError().body(Map.of("error","No se pudo registrar"));}}
    @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Map<String,Object> b){try{return ResponseEntity.ok(auth.login((String)b.get("correo"),(String)b.get("password")));}catch(IllegalArgumentException e){return ResponseEntity.status(401).body(Map.of("error",e.getMessage()));}catch(Exception e){return ResponseEntity.internalServerError().body(Map.of("error","No se pudo iniciar sesión"));}}
    @PostMapping("/admin") public ResponseEntity<?> admin(@RequestBody Map<String,Object> b){try{return ResponseEntity.ok(auth.admin((String)b.get("usuario"),(String)b.get("password")));}catch(IllegalArgumentException e){return ResponseEntity.status(401).body(Map.of("error",e.getMessage()));}catch(Exception e){return ResponseEntity.internalServerError().body(Map.of("error","No se pudo validar al administrador"));}}
    @PostMapping("/logout") public Map<String,Object> logout(HttpServletRequest r){auth.logout((String)r.getAttribute("token"));return Map.of("ok",true);}
}
