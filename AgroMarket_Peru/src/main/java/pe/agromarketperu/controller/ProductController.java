package pe.agromarketperu.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import pe.agromarketperu.model.Product;
import pe.agromarketperu.service.ProductService;

@RestController
@RequestMapping("/api/productos")
public class ProductController {
    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public List<Product> all() {
        return products.all();
    }

    @GetMapping("/{id}")
    public Product one(@PathVariable int id) {
        Product p = products.one(id);
        if (p == null)
            throw new IllegalArgumentException("Producto no encontrado");
        return p;
    }

    @GetMapping("/{id}/historial")
    public Object history(@PathVariable int id) {
        return products.history(id);
    }

    @PostMapping
    public Product create(@RequestBody Product p, HttpServletRequest r) {
        Map<String, Object> u = (Map<String, Object>) r.getAttribute("user");
        if (!"admin".equals(u.get("rol")))
            throw new IllegalArgumentException("Acceso denegado");
        return products.create(p);
    }
}
