package pe.agromarketperu.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.agromarketperu.dto.ProductGroup;
import pe.agromarketperu.model.PriceHistory;
import pe.agromarketperu.model.Product;
import pe.agromarketperu.model.ProductComparisonKey;
import pe.agromarketperu.repository.PriceHistoryRepository;
import pe.agromarketperu.repository.ProductRepository;
import pe.agromarketperu.scraping.ScrapedOffer;

@Service
public class ProductService {
    private static final List<String> TIENDAS = List.of("Plaza Vea", "Tottus", "Metro");
    private final ProductRepository products;
    private final PriceHistoryRepository history;

    public ProductService(ProductRepository products, PriceHistoryRepository history) {
        this.products = products;
        this.history = history;
    }

    public List<Product> all() {
        return products.findAllByOrderByNombreAscTiendaAsc().stream().filter(p -> TIENDAS.contains(p.getTienda())).toList();
    }

    public Product one(int id) {
        Product p = products.findById(id).orElse(null);
        return p != null && TIENDAS.contains(p.getTienda()) ? p : null;
    }

    public List<PriceHistory> history(int id) {
        return history.findByProducto_IdOrderByRegistradoEnAsc(id);
    }

    private List<Product> offersForDisplay() {
        List<Product> stored = all();
        List<Product> scraped = stored.stream().filter(p -> "SCRAPING".equals(p.getOrigen())).toList();
        return scraped.isEmpty() ? stored.stream().filter(p -> "DEMO".equals(p.getOrigen())).toList() : scraped;
    }

    public List<ProductGroup> groups() {
        Map<String, List<Product>> grouped = new LinkedHashMap<>();
        for (Product p : offersForDisplay()) {
            String key = p.getClaveComparable() == null || p.getClaveComparable().isBlank() ? ProductComparisonKey.fromName(p.getNombre()) : p.getClaveComparable();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }
        return grouped.entrySet().stream().map(entry -> {
            List<Product> offers = entry.getValue().stream().sorted(Comparator.comparing(Product::getPrecio)).toList();
            Product sample = offers.get(0);
            return new ProductGroup(entry.getKey(), ProductComparisonKey.displayName(entry.getKey(), sample.getNombre()), sample.getCategoria(), sample.getImagen(), offers);
        }).sorted(Comparator.comparing(ProductGroup::getNombre)).toList();
    }

    @Transactional
    public Product create(Product p) {
        p.setClaveComparable(ProductComparisonKey.fromName(p.getNombre()));
        if (products.findFirstByClaveComparableAndTiendaIgnoreCase(p.getClaveComparable(), p.getTienda()).isPresent())
            throw new IllegalArgumentException("Ya existe una oferta de ese producto para esa tienda");
        p.setOrigen("MANUAL");
        p.setFechaActualizacion(LocalDateTime.now().withNano(0));
        Product saved = products.saveAndFlush(p);
        addHistory(saved, saved.getPrecio());
        return saved;
    }

    @Transactional
    public Product update(int id, Product changes) {
        Product current = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if ("SCRAPING".equals(current.getOrigen()))
            throw new IllegalArgumentException("Las ofertas obtenidas del supermercado son de solo lectura; usa Actualizar precios.");
        Optional<Product> duplicate = products.findFirstByClaveComparableAndTiendaIgnoreCase(ProductComparisonKey.fromName(changes.getNombre()), changes.getTienda());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) throw new IllegalArgumentException("Ya existe una oferta de ese producto para esa tienda");
        BigDecimal oldPrice = current.getPrecio();
        current.setNombre(changes.getNombre());
        current.setCategoria(changes.getCategoria());
        current.setTienda(changes.getTienda());
        current.setPrecio(changes.getPrecio());
        current.setImagen(changes.getImagen());
        current.setUrl(changes.getUrl());
        current.setClaveComparable(ProductComparisonKey.fromName(changes.getNombre()));
        current.setOrigen("MANUAL");
        current.setFechaActualizacion(LocalDateTime.now().withNano(0));
        Product saved = products.saveAndFlush(current);
        if (oldPrice == null || oldPrice.compareTo(saved.getPrecio()) != 0) addHistory(saved, saved.getPrecio());
        return saved;
    }

    @Transactional
    public void delete(int id) {
        Product p = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if ("SCRAPING".equals(p.getOrigen()))
            throw new IllegalArgumentException("Las ofertas obtenidas del supermercado no se eliminan manualmente.");
        products.delete(p);
    }

    @Transactional
    public UpsertResult upsertScraped(ScrapedOffer offer) {
        String key = ProductComparisonKey.fromName(offer.nombre());
        Product p = products.findFirstByClaveComparableAndTiendaIgnoreCase(key, offer.tienda())
                .or(() -> products.findFirstByNombreIgnoreCaseAndTiendaIgnoreCase(offer.nombre(), offer.tienda()))
                .orElse(null);
        boolean created = p == null;
        if (created) {
            p = new Product(offer.nombre(), offer.categoria(), offer.tienda(), offer.precio(), offer.imagen(), offer.url(), key, "SCRAPING");
            p.setFechaActualizacion(LocalDateTime.now().withNano(0));
            p = products.saveAndFlush(p);
            addHistory(p, offer.precio());
            return new UpsertResult(true, true, true);
        }
        BigDecimal oldPrice = p.getPrecio();
        boolean changed = oldPrice == null || oldPrice.compareTo(offer.precio()) != 0;
        boolean firstRealScrape = !"SCRAPING".equals(p.getOrigen());
        p.setNombre(offer.nombre());
        p.setClaveComparable(key);
        p.setCategoria(offer.categoria());
        p.setImagen(offer.imagen());
        p.setUrl(offer.url());
        p.setOrigen("SCRAPING");
        p.setFechaActualizacion(LocalDateTime.now().withNano(0));
        p.setPrecio(offer.precio());
        p = products.saveAndFlush(p);
        List<PriceHistory> prior = history.findByProducto_IdOrderByRegistradoEnAsc(p.getId());
        LocalDateTime lastDate = prior.isEmpty() ? null : prior.get(prior.size() - 1).getRegistradoEn();
        boolean add = prior.isEmpty() || changed || firstRealScrape || lastDate == null || lastDate.toLocalDate().isBefore(LocalDate.now());
        if (add) addHistory(p, offer.precio());
        return new UpsertResult(false, changed, add);
    }

    @Transactional
    public void initializeCatalog() {
        for (Product p : products.findAll()) {
            if (!TIENDAS.contains(p.getTienda())) continue;
            boolean changed = false;
            String key = ProductComparisonKey.fromName(p.getNombre());
            if (p.getClaveComparable() == null || p.getClaveComparable().isBlank()) { p.setClaveComparable(key); changed = true; }
            if (p.getOrigen() == null || p.getOrigen().isBlank()) { p.setOrigen("DEMO"); changed = true; }
            if (changed) products.save(p);
        }
        List<Seed> seeds = seeds();
        String[] stores = {"Plaza Vea", "Tottus", "Metro"};
        for (Seed seed : seeds) {
            for (int i = 0; i < stores.length; i++) {
                String store = stores[i];
                Optional<Product> match = products.findFirstByClaveComparableAndTiendaIgnoreCase(seed.key(), store);
                if (match.isEmpty()) match = products.findFirstByNombreIgnoreCaseAndTiendaIgnoreCase(seed.name(), store);
                if (match.isPresent()) continue;
                Product p = new Product(seed.name(), seed.category(), store, seed.prices().get(i), seed.image(), seed.urls().get(i), seed.key(), "DEMO");
                Product saved = products.saveAndFlush(p);
                addHistory(saved, saved.getPrecio());
            }
        }
        for (Product p : products.findAll()) {
            if (TIENDAS.contains(p.getTienda()) && history.findByProducto_IdOrderByRegistradoEnAsc(p.getId()).isEmpty()) addHistory(p, p.getPrecio());
        }
    }

    private void addHistory(Product p, BigDecimal price) {
        LocalDateTime date = LocalDateTime.now().withNano(0);
        List<PriceHistory> rows = history.findByProducto_IdOrderByRegistradoEnAsc(p.getId());
        if (!rows.isEmpty() && !date.isAfter(rows.get(rows.size() - 1).getRegistradoEn())) date = rows.get(rows.size() - 1).getRegistradoEn().plusSeconds(1);
        history.save(new PriceHistory(p, price, date));
    }

    public record UpsertResult(boolean created, boolean priceChanged, boolean historyAdded) {}
    private record Seed(String key, String name, String category, List<BigDecimal> prices, String image, List<String> urls) {}

    private List<Seed> seeds() {
        return List.of(
            seed("arroz-costeno-5kg", "Arroz Extra Costeño Bolsa 5 kg", "Abarrotes", "21.50,22.90,23.70", "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=700&q=80", "/arroz-extra-costeno-bolsa-5kg/p", "/tottus-pe/articulo/113707306/arroz-extra-costeno-5-kg", "/arroz-extra-costeno-5kg-14974-2/p"),
            seed("arroz-superior-costeno-5kg", "Arroz Superior Costeño 5 kg", "Abarrotes", "23.90,19.90,20.90", "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=700&q=80", "/abarrotes/arroz", "/tottus-pe/articulo/113709634/arroz-superior-costeno-5-kg/113709635", "/colecciones/arroz"),
            seed("aceite-primor-900ml", "Aceite Vegetal Primor Clásico 900 ml", "Abarrotes", "9.50,8.70,9.20", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=700&q=80", "/abarrotes/aceite/vegetal/aceite", "/tottus-pe/lista/CATG16817/Aceite", "/colecciones/aceite-primor/"),
            seed("aceite-primor-1800ml", "Aceite Vegetal Primor Clásico 1.8 L", "Abarrotes", "17.90,16.80,17.50", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=700&q=80", "/abarrotes/aceite/vegetal/aceite", "/tottus-pe/lista/CATG16817/Aceite", "/colecciones/aceite-primor/"),
            seed("leche-gloria-sixpack-390g", "Leche Gloria Evaporada Sixpack 390 g", "Lácteos", "24.50,21.50,21.90", "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=700&q=80", "/lacteos-y-huevos/leche/gloria/leches", "/tottus-pe/lista/CATG16782/Leches", "/colecciones/leche-gloria/"),
            seed("leche-gloria-pack3-946ml", "Leche UHT Gloria Pack 3 x 946 ml", "Lácteos", "17.90,16.20,16.90", "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=700&q=80", "/lacteos-y-huevos/leche/gloria/leches", "/tottus-pe/seleccion/lecheuht", "/colecciones/leche-gloria/"),
            seed("azucar-rubia-1kg", "Azúcar Rubia Bolsa 1 kg", "Abarrotes", "4.50,4.20,4.30", "https://images.unsplash.com/photo-1581441363689-1f3c3c414635?auto=format&fit=crop&w=700&q=80", "/", "/", "/"),
            seed("fideos-spaghetti-500g", "Fideos Spaghetti 500 g", "Abarrotes", "3.80,3.50,3.70", "https://images.unsplash.com/photo-1551462147-ff29053bfc14?auto=format&fit=crop&w=700&q=80", "/", "/", "/"),
            seed("lenteja-500g", "Lentejas Bolsa 500 g", "Abarrotes", "5.90,5.50,5.80", "https://images.unsplash.com/photo-1515543904379-3d757afe72e4?auto=format&fit=crop&w=700&q=80", "/", "/", "/"),
            seed("atun-aceite-170g", "Atún en Aceite Lata 170 g", "Abarrotes", "5.90,5.50,5.80", "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?auto=format&fit=crop&w=700&q=80", "/", "/", "/"),
            seed("papa-blanca-1kg", "Papa Blanca 1 kg", "Frutas y Verduras", "3.80,2.59,3.20", "https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=700&q=80", "/", "/", "/"),
            seed("platano-seda-1kg", "Plátano de Seda 1 kg", "Frutas y Verduras", "5.50,5.90,5.20", "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=700&q=80", "/", "/", "/"),
            seed("huevos-15", "Huevos Pardos Paquete 15 unidades", "Lácteos", "10.90,10.50,11.20", "https://images.unsplash.com/photo-1518569656558-1f25e69d93d7?auto=format&fit=crop&w=700&q=80", "/", "/", "/")
        );
    }

    private Seed seed(String key, String name, String category, String prices, String image, String pve, String tottus, String metro) {
        String[] values = prices.split(",");
        return new Seed(key, name, category, List.of(new BigDecimal(values[0]), new BigDecimal(values[1]), new BigDecimal(values[2])), image, List.of("https://www.plazavea.com.pe" + pve, "https://www.tottus.com.pe" + tottus, "https://www.metro.pe" + metro));
    }
}
