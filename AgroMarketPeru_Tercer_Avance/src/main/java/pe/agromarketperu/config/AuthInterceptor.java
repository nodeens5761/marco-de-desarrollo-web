package pe.agromarketperu.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.agromarketperu.service.AuthService;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService auth;

    public AuthInterceptor(AuthService auth) {
        this.auth = auth;
    }

    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (req.getMethod().equals("GET") && req.getRequestURI().startsWith("/api/productos"))
            return true;
        String h = req.getHeader("Authorization");
        String token = h != null && h.startsWith("Bearer ") ? h.substring(7) : null;
        Map<String, Object> user = token == null ? null : auth.userByToken(token);
        if (user == null) {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Sesión requerida o expirada\"}");
            return false;
        }
        req.setAttribute("user", user);
        req.setAttribute("token", token);
        return true;
    }
}
