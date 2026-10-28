package pe.agromarketperu.service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JdbcTemplate db;
    private final PasswordService passwords;
    private final SecureRandom random=new SecureRandom();

    public AuthService(JdbcTemplate db,PasswordService passwords){this.db=db;this.passwords=passwords;}

    public void register(String nombre,String correo,String password){
        if(nombre==null||correo==null||password==null||nombre.isBlank()||correo.isBlank()||password.isBlank())throw new IllegalArgumentException("Completa los campos");
        try{db.update("INSERT INTO usuarios(nombre,correo,password_hash,rol) VALUES(?,?,?,'user')",nombre.trim(),correo.trim().toLowerCase(),passwords.hash(password));}
        catch(Exception e){if(e.getMessage()!=null&&e.getMessage().contains("Duplicate"))throw new IllegalArgumentException("El correo ya está registrado");throw e;}
    }

    public Map<String,Object> login(String correo,String password){
        Map<String,Object> u=userByEmail(correo);
        if(u==null||password==null||!passwords.matches(password,(String)u.get("password_hash")))throw new IllegalArgumentException("Correo o contraseña incorrectos");
        return session(u);
    }

    public Map<String,Object> admin(String usuario,String password){
        if(!"ADMIN".equals(usuario)||!"admin123".equals(password))throw new IllegalArgumentException("Credenciales administrativas incorrectas");
        Map<String,Object> u=db.queryForMap("SELECT * FROM usuarios WHERE rol='admin' LIMIT 1");
        return session(u);
    }

    public Map<String,Object> userByToken(String token){
        try{return db.queryForMap("SELECT u.* FROM sesiones s JOIN usuarios u ON u.id=s.usuario_id WHERE s.token=? AND s.expires_at>NOW()",token);}catch(EmptyResultDataAccessException e){return null;}
    }

    public void logout(String token){db.update("DELETE FROM sesiones WHERE token=?",token);}

    private Map<String,Object> userByEmail(String correo){
        try{return db.queryForMap("SELECT * FROM usuarios WHERE correo=?",String.valueOf(correo==null?"":correo).trim().toLowerCase());}catch(EmptyResultDataAccessException e){return null;}
    }

    private Map<String,Object> session(Map<String,Object> u){
        String token=HexFormat.of().formatHex(random.generateSeed(32));
        db.update("INSERT INTO sesiones(token,usuario_id,expires_at) VALUES(?,?,DATE_ADD(NOW(),INTERVAL 8 HOUR))",token,u.get("id"));
        return Map.of("token",token,"user",Map.of("id",u.get("id"),"nombre",u.get("nombre"),"correo",u.get("correo"),"rol",u.get("rol")));
    }
}
