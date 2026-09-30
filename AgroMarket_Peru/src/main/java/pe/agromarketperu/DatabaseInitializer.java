package pe.agromarketperu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.agromarketperu.service.PasswordService;

@Configuration
public class DatabaseInitializer {
    @Bean
    CommandLineRunner initAdmin(JdbcTemplate db,PasswordService passwords){return args->{Integer n=db.queryForObject("SELECT COUNT(*) FROM usuarios WHERE rol='admin'",Integer.class);if(n!=null&&n==0)db.update("INSERT INTO usuarios(nombre,correo,password_hash,rol) VALUES(?, ?, ?, 'admin')","Administrador","admin@agromarketperu.local",passwords.hash("admin123"));};}
}
