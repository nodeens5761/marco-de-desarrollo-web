package pe.agromarketperu.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import pe.agromarketperu.service.OrderService;

@RestController
@RequestMapping("/api/pedidos")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> b, HttpServletRequest r) {
        return orders.create(b, (Map<String, Object>) r.getAttribute("user"));
    }

    @GetMapping
    public List<Map<String, Object>> mine(HttpServletRequest r) {
        return orders.mine(((Number) ((Map<String, Object>) r.getAttribute("user")).get("id")).intValue());
    }
}
