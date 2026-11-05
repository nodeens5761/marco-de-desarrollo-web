package pe.edu.utp.techlab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {
    @GetMapping("/perfil")
    public String profile() {
        return "auth/perfil";
    }
}
