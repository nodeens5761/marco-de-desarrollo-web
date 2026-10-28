package pe.agromarketperu.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.bouncycastle.crypto.generators.SCrypt;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private static final int N=16384,R=8,P=1,LEN=64;
    private final SecureRandom random=new SecureRandom();

    public String hash(String password){
        String salt=HexFormat.of().formatHex(random.generateSeed(16));
        byte[] key=SCrypt.generate(password.getBytes(StandardCharsets.UTF_8),salt.getBytes(StandardCharsets.UTF_8),N,R,P,LEN);
        return salt+":"+HexFormat.of().formatHex(key);
    }

    public boolean matches(String password,String stored){
        try{
            String[] p=stored.split(":",2);
            byte[] expected=HexFormat.of().parseHex(p[1]);
            byte[] actual=SCrypt.generate(password.getBytes(StandardCharsets.UTF_8),p[0].getBytes(StandardCharsets.UTF_8),N,R,P,LEN);
            return MessageDigest.isEqual(actual,expected);
        }catch(Exception e){return false;}
    }
}
