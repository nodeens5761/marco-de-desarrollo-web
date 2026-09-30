package pe.agromarketperu.controller;

import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.agromarketperu.model.Product;
import pe.agromarketperu.service.ProductService;

@Controller
public class WebController {
    private final ProductService products;

    public WebController(ProductService products) {
        this.products = products;
    }

    @GetMapping({ "/", "/index.html" })
    public String home(Model model) {
        model.addAttribute("productos", products.all());
        return "index";
    }

    @GetMapping("/modulo-2-catalogo/index.html")
    public String catalog(Model model) {
        model.addAttribute("productos", products.all());
        return "modulo-2-catalogo/index";
    }

    @GetMapping("/modulo-3-carrito-compras-historial/historial-precios.html")
    public String history(@RequestParam int id, Model model) {
        Product p = products.one(id);
        model.addAttribute("producto", p);
        if (p != null) {
            var h = products.history(id);
            model.addAttribute("historial", h);
            model.addAttribute("labels",
                    h.stream().map(x -> x.registradoEn().toLocalDate().toString()).collect(Collectors.toList()));
            model.addAttribute("values", h.stream().map(x -> x.precio()).collect(Collectors.toList()));
        }
        return "modulo-3-carrito-compras-historial/historial-precios";
    }
}
