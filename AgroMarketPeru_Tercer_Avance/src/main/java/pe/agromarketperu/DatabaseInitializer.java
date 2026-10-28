package pe.agromarketperu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.agromarketperu.service.PasswordService;
import pe.agromarketperu.service.ProductService;

@Configuration
public class DatabaseInitializer {
    @Bean
    CommandLineRunner initialize(JdbcTemplate db, PasswordService passwords, ProductService products) {
        return args -> {
            products.initializeCatalog();
            Integer admins = db.queryForObject("SELECT COUNT(*) FROM usuarios WHERE rol='admin'", Integer.class);
            if (admins != null && admins == 0) db.update("INSERT INTO usuarios(nombre,correo,password_hash,rol) VALUES(?,?,?,'admin')", "Administrador", "admin@agromarketperu.local", passwords.hash("admin123"));
        };
    }
}
