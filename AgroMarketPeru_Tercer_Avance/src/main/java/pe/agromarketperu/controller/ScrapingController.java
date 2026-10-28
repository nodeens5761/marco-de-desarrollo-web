package pe.agromarketperu.controller;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pe.agromarketperu.scraping.ScrapingReport;
import pe.agromarketperu.scraping.SupermarketScraperService;

@RestController
@RequestMapping("/api/admin/scraping")
public class ScrapingController {
    private final SupermarketScraperService scraper;

    public ScrapingController(SupermarketScraperService scraper) {
        this.scraper = scraper;
    }

    @PostMapping("/actualizar")
    public ScrapingReport actualizar(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) request.getAttribute("user");
        if (user == null || !"admin".equals(user.get("rol"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede actualizar precios");
        return scraper.actualizar();
    }
}
