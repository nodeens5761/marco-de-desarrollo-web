package pe.agromarketperu.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pe.agromarketperu.model.Product;
import pe.agromarketperu.model.PriceHistory;
import pe.agromarketperu.service.ProductService;

@RestController
@RequestMapping("/api/productos")
public class ProductController {
    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public List<Product> all() { return products.all(); }

    @GetMapping("/{id}")
    public Product one(@PathVariable int id) {
        Product p = products.one(id);
        if (p == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        return p;
    }

    @GetMapping("/{id}/historial")
    public List<PriceHistory> history(@PathVariable int id) {
        if (products.one(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        return products.history(id);
    }

    @PostMapping
    public Product create(@Valid @RequestBody Product p, HttpServletRequest request) {
        requireAdmin(request);
        return products.create(p);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable int id, @Valid @RequestBody Product p, HttpServletRequest request) {
        requireAdmin(request);
        return products.update(id, p);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable int id, HttpServletRequest request) {
        requireAdmin(request);
        products.delete(id);
        return Map.of("ok", true);
    }

    @SuppressWarnings("unchecked")
    private void requireAdmin(HttpServletRequest request) {
        Map<String, Object> user = (Map<String, Object>) request.getAttribute("user");
        if (user == null || !"admin".equals(user.get("rol"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede modificar productos");
    }
}
