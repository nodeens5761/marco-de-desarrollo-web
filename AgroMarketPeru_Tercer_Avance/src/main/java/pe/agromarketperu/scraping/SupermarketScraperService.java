package pe.agromarketperu.scraping;

import java.math.BigDecimal;
import java.net.URI;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.agromarketperu.model.ProductComparisonKey;
import pe.agromarketperu.service.ProductService;
import pe.agromarketperu.service.ProductService.UpsertResult;

@Service
public class SupermarketScraperService {
    private static final Pattern PRICE = Pattern.compile("(?i)S\\s*/\\.?\\s*([0-9]{1,4}(?:[.,][0-9]{1,2})?)");
    private static final Pattern ONLINE_PRICE = Pattern.compile("(?i)precio online[^0-9]{0,35}([0-9]{1,4}(?:[.,][0-9]{1,2})?)");
    private static final List<Source> SOURCES = List.of(
        new Source("Plaza Vea", "Abarrotes", "https://www.plazavea.com.pe/api/catalog_system/pub/products/search?ft=arroz%20costeno%205kg&_from=0&_to=49"),
        new Source("Plaza Vea", "Abarrotes", "https://www.plazavea.com.pe/abarrotes/arroz"),
        new Source("Plaza Vea", "Abarrotes", "https://www.plazavea.com.pe/api/catalog_system/pub/products/search?ft=aceite%20primor&_from=0&_to=49"),
        new Source("Plaza Vea", "Abarrotes", "https://www.plazavea.com.pe/abarrotes/aceite/aceite-vegetal"),
        new Source("Plaza Vea", "Lácteos", "https://www.plazavea.com.pe/api/catalog_system/pub/products/search?ft=leche%20gloria%20sixpack&_from=0&_to=49"),
        new Source("Plaza Vea", "Lácteos", "https://www.plazavea.com.pe/api/catalog_system/pub/products/search?ft=leche%20uht%20gloria%20pack%203&_from=0&_to=49"),
        new Source("Plaza Vea", "Lácteos", "https://www.plazavea.com.pe/lacteos-y-huevos/leche/gloria"),
        new Source("Tottus", "Abarrotes", "https://www.tottus.com.pe/tottus-pe/articulo/113707306/arroz-extra-costeno-5-kg"),
        new Source("Tottus", "Abarrotes", "https://www.tottus.com.pe/tottus-pe/seleccion/arrozextra"),
        new Source("Tottus", "Abarrotes", "https://www.tottus.com.pe/tottus-pe/articulo/113706444/Aceite-Vegetal-Primor-900-mL/113706446"),
        new Source("Tottus", "Abarrotes", "https://www.tottus.com.pe/tottus-pe/seleccion/aceiteprimor"),
        new Source("Tottus", "Lácteos", "https://www.tottus.com.pe/tottus-pe/articulo/129087930/Leche-Gloria-Evaporada-Entera-Sixpack-Lata-390-g"),
        new Source("Tottus", "Lácteos", "https://www.tottus.com.pe/tottus-pe/seleccion/lechegloria6pack"),
        new Source("Tottus", "Lácteos", "https://www.tottus.com.pe/tottus-pe/articulo/144942889/leche-uht-entera-gloria-pack-3-cajas-946-ml"),
        new Source("Tottus", "Lácteos", "https://www.tottus.com.pe/tottus-pe/seleccion/lecheuht"),
        new Source("Metro", "Abarrotes", "https://www.metro.pe/colecciones/arroz-costeno/"),
        new Source("Metro", "Abarrotes", "https://www.metro.pe/colecciones/aceite-primor/"),
        new Source("Metro", "Lácteos", "https://www.metro.pe/colecciones/leche-gloria/")
    );
    private final ProductService products;
    private final ObjectMapper json;
    private final int timeout;
    private final int maxPerPage;

    public SupermarketScraperService(ProductService products, ObjectMapper json,
            @Value("${agromarket.scraping.timeout-ms:12000}") int timeout,
            @Value("${agromarket.scraping.max-products-per-page:12}") int maxPerPage) {
        this.products = products;
        this.json = json;
        this.timeout = timeout;
        this.maxPerPage = maxPerPage;
    }

    public ScrapingReport actualizar() {
        List<Source> plaza = SOURCES.stream().filter(s -> s.tienda().equals("Plaza Vea")).toList();
        List<Source> tottus = SOURCES.stream().filter(s -> s.tienda().equals("Tottus")).toList();
        List<Source> metro = SOURCES.stream().filter(s -> s.tienda().equals("Metro")).toList();
        List<CompletableFuture<StoreResult>> tasks = List.of(
            CompletableFuture.supplyAsync(() -> scrapeStore("Plaza Vea", plaza)),
            CompletableFuture.supplyAsync(() -> scrapeStore("Tottus", tottus)),
            CompletableFuture.supplyAsync(() -> scrapeStore("Metro", metro))
        );
        CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        List<StoreResult> stores = tasks.stream().map(CompletableFuture::join).toList();
        Map<String, ScrapedOffer> unique = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> successful = new ArrayList<>();
        for (StoreResult result : stores) {
            errors.addAll(result.errors());
            if (!result.products().isEmpty()) successful.add(result.tienda() + " (" + result.products().size() + " productos detectados)");
            for (ScrapedOffer offer : result.products()) {
                String key = ProductComparisonKey.fromName(offer.nombre()) + "|" + offer.tienda();
                ScrapedOffer previous = unique.get(key);
                if (previous == null) unique.put(key, offer);
            }
        }
        int created = 0;
        int updated = 0;
        int histories = 0;
        for (ScrapedOffer offer : unique.values()) {
            try {
                UpsertResult result = products.upsertScraped(offer);
                if (result.created()) created++;
                if (result.priceChanged()) updated++;
                if (result.historyAdded()) histories++;
            } catch (RuntimeException e) {
                errors.add(offer.tienda() + ": no se pudo guardar " + offer.nombre());
            }
        }
        int found = unique.size();
        String message = found == 0
                ? "No se reconocieron productos en las páginas consultadas. Los datos actuales se conservaron."
                : "Se procesaron " + found + " ofertas. Los precios obtenidos se guardaron en MySQL.";
        if (found > 0 && !errors.isEmpty()) message += " Algunas páginas no respondieron o no pudieron procesarse.";
        return new ScrapingReport(LocalDateTime.now().withNano(0), found, created, updated, histories, successful, errors, message);
    }

    private StoreResult scrapeStore(String store, List<Source> sources) {
        List<ScrapedOffer> all = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int pages = 0;
        for (Source source : sources) {
            try {
                List<ScrapedOffer> extracted;
                if (store.equals("Plaza Vea") && source.url().contains("/api/catalog_system/pub/products/search")) {
                    extracted = extractVtex(fetchText(source.url()), source);
                } else {
                    Document document = fetch(source.url());
                    extracted = extract(document, source);
                }
                pages++;
                all.addAll(extracted);
                if (extracted.isEmpty()) errors.add(store + ": la página respondió, pero no se identificaron ofertas con nombre, precio y enlace de producto: " + source.url());
            } catch (Exception e) {
                errors.add(store + ": no fue posible leer " + source.url() + " (" + shortError(e) + ")");
            }
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
        return new StoreResult(store, pages, all, errors);
    }

    private Document fetch(String url) throws Exception {
        Exception failure = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try { return connection(url, false).get(); }
            catch (Exception e) {
                if (!retryable(e)) throw e;
                failure = e;
                pause(350);
            }
        }
        throw failure;
    }

    private String fetchText(String url) throws Exception {
        Exception failure = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                Connection.Response response = connection(url, true).execute();
                if (response.statusCode() >= 400) throw new HttpStatusException("HTTP " + response.statusCode(), response.statusCode(), url);
                return response.body();
            } catch (Exception e) {
                if (!retryable(e)) throw e;
                failure = e;
                pause(350);
            }
        }
        throw failure;
    }

    private boolean retryable(Exception error) {
        if (!(error instanceof HttpStatusException status)) return true;
        int code = status.getStatusCode();
        return code == 408 || code == 429 || code >= 500;
    }

    private void pause(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private Connection connection(String url, boolean jsonResponse) {
        String host = url.contains("plazavea.com.pe") ? "https://www.plazavea.com.pe/"
                : url.contains("tottus.com.pe") ? "https://www.tottus.com.pe/" : "https://www.metro.pe/";
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                .header("Accept-Language", "es-PE,es;q=0.9,en;q=0.7")
                .header("Accept", jsonResponse ? "application/json, text/plain, */*" : "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Referer", host)
                .header("Cache-Control", "no-cache")
                .timeout(timeout)
                .followRedirects(true)
                .ignoreHttpErrors(false)
                .ignoreContentType(jsonResponse);
    }

    private List<ScrapedOffer> extractVtex(String body, Source source) throws Exception {
        JsonNode root = json.readTree(body);
        Map<String, ScrapedOffer> found = new LinkedHashMap<>();
        if (root == null || !root.isArray()) throw new IOException("La API pública no devolvió un listado de productos");
        for (JsonNode product : root) {
            String name = firstText(product, "productName", "productTitle", "name");
            if (name.isBlank() || !isEssential(name)) continue;
            String link = text(product.get("link"));
            if (link.isBlank()) continue;
            String url = absolute(link, "https://www.plazavea.com.pe/");
            String image = "";
            JsonNode items = product.path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    if (image.isBlank()) image = imageFrom(item);
                    JsonNode sellers = item.path("sellers");
                    if (!sellers.isArray()) continue;
                    for (JsonNode seller : sellers) {
                        JsonNode offer = seller.path("commertialOffer");
                        BigDecimal price = firstDecimal(offer, "Price", "SellingPrice", "price");
                        JsonNode stock = offer.get("AvailableQuantity");
                        if (price == null || (stock != null && stock.isNumber() && stock.asInt() <= 0)) continue;
                        String imageUrl = image.isBlank() ? "" : absolute(image, "https://www.plazavea.com.pe/");
                        add(found, new ScrapedOffer(cleanName(name), categoryFor(name, source.categoria()), source.tienda(), price,
                                safeImage(imageUrl, source.categoria()), url));
                        break;
                    }
                    if (found.size() >= maxPerPage) break;
                }
            }
            if (found.size() >= maxPerPage) break;
        }
        return new ArrayList<>(found.values());
    }

    private String imageFrom(JsonNode item) {
        JsonNode images = item.path("images");
        if (images.isArray() && images.size() > 0) return text(images.get(0).path("imageUrl"));
        return "";
    }

    private BigDecimal firstDecimal(JsonNode node, String... names) {
        for (String name : names) {
            BigDecimal value = decimal(node.get(name));
            if (value != null) return value;
        }
        return null;
    }

    private String firstText(JsonNode node, String... names) {
        for (String name : names) {
            String value = text(node.get(name));
            if (!value.isBlank()) return value;
        }
        return "";
    }

    private List<ScrapedOffer> extract(Document document, Source source) {
        boolean tottus = source.tienda().equals("Tottus");
        boolean detailPage = tottus && source.url().contains("/articulo/");
        if (detailPage && document.text().toLowerCase(Locale.ROOT).contains("producto sin stock")) return List.of();
        Map<String, ScrapedOffer> result = new LinkedHashMap<>();
        if (detailPage) {
            extractTottusDetail(document, source, result);
            if (result.isEmpty()) collectStructuredOffers(document, source, result);
            return new ArrayList<>(result.values());
        }
        if (!tottus) collectStructuredOffers(document, source, result);
        String linksSelector = tottus ? "a[href*=\"/articulo/\"]" : "a[href]";
        for (Element link : document.select(linksSelector)) {
            String href = link.absUrl("href");
            if (!isProductLink(href, source.tienda())) continue;
            Element card = findCard(link);
            String name = cardName(link, card);
            BigDecimal price = extractPrice(card == null ? link.text() : card.text(), source.tienda());
            if (name.isBlank() || price == null || !isEssential(name)) continue;
            String image = card == null ? "" : card.select("img").stream()
                    .map(img -> firstNonBlank(img.attr("data-src"), img.attr("data-original"), img.absUrl("src")))
                    .filter(value -> !value.isBlank() && !value.startsWith("data:image"))
                    .findFirst().orElse("");
            add(result, new ScrapedOffer(cleanName(name), categoryFor(name, source.categoria()), source.tienda(), price, safeImage(image, source.categoria()), href));
            if (result.size() >= maxPerPage) break;
        }
        if (tottus) collectStructuredOffers(document, source, result);
        return new ArrayList<>(result.values());
    }

    private void extractTottusDetail(Document document, Source source, Map<String, ScrapedOffer> result) {
        Element heading = document.selectFirst("main h1, h1");
        String name = heading == null ? "" : cleanName(heading.text());
        Element block = findDetailBlock(heading);
        String text = block == null ? document.select("main").text() : block.text();
        BigDecimal price = extractPrice(text, source.tienda());
        if (price == null) price = extractPrice(document.text(), source.tienda());
        if (name.isBlank() || price == null || !isEssential(name)) return;
        String image = "";
        Element meta = document.selectFirst("meta[property=og:image], meta[name=twitter:image]");
        if (meta != null) image = meta.attr("content");
        if (image.isBlank() && block != null) {
            Element img = block.selectFirst("img[data-src], img[data-original], img[src]");
            if (img != null) image = firstNonBlank(img.absUrl("data-src"), img.absUrl("data-original"), img.absUrl("src"));
        }
        String imageUrl = image.isBlank() ? "" : absolute(image, document.location());
        add(result, new ScrapedOffer(name, categoryFor(name, source.categoria()), source.tienda(), price,
                safeImage(imageUrl, source.categoria()), document.location()));
    }

    private Element findDetailBlock(Element heading) {
        for (Element current = heading; current != null; current = current.parent()) {
            String value = current.text().replace(' ', ' ').replace(' ', ' ');
            if (value.length() > 1800) break;
            if (value.length() >= 20 && PRICE.matcher(value).find()) return current;
        }
        return null;
    }

    private void collectStructuredOffers(Document document, Source source, Map<String, ScrapedOffer> result) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            try { collectJson(json.readTree(script.data().isBlank() ? script.html() : script.data()), source, document, result); }
            catch (Exception ignored) {}
        }
    }

    private void collectJson(JsonNode node, Source source, Document doc, Map<String, ScrapedOffer> out) {
        if (node == null) return;
        if (node.isArray()) { node.forEach(n -> collectJson(n, source, doc, out)); return; }
        JsonNode type = node.get("@type");
        boolean productType = type != null && ((type.isTextual() && type.asText().equalsIgnoreCase("Product")) || (type.isArray() && iterableContains(type, "Product")));
        if (productType) {
            String name = text(node.get("name"));
            BigDecimal price = priceFromOffers(node.get("offers"));
            if (!name.isBlank() && price != null && isEssential(name)) {
                String url = absolute(text(node.get("url")), doc.location());
                String image = text(node.get("image"));
                if (node.get("image") != null && node.get("image").isArray() && node.get("image").size() > 0) image = text(node.get("image").get(0));
                String imageUrl = image.isBlank() ? "" : absolute(image, doc.location());
                add(out, new ScrapedOffer(cleanName(name), categoryFor(name, source.categoria()), source.tienda(), price, safeImage(imageUrl, source.categoria()), url));
            }
        }
        node.fields().forEachRemaining(entry -> {
            if (entry.getValue().isContainerNode()) collectJson(entry.getValue(), source, doc, out);
        });
    }

    private BigDecimal priceFromOffers(JsonNode offers) {
        if (offers == null) return null;
        if (offers.isArray()) {
            for (JsonNode offer : offers) {
                BigDecimal value = decimal(offer.has("price") ? offer.get("price") : offer.get("lowPrice"));
                if (value != null) return value;
            }
            return null;
        }
        return decimal(offers.has("price") ? offers.get("price") : offers.get("lowPrice"));
    }

    private BigDecimal extractPrice(String raw, String store) {
        String value = raw == null ? "" : raw.replace(' ', ' ').replace(' ', ' ');
        Matcher online = ONLINE_PRICE.matcher(value);
        if ((store.equals("Plaza Vea") || store.equals("Metro")) && online.find()) {
            BigDecimal price = money(online.group(1));
            if (price != null) return price;
        }
        Matcher matcher = PRICE.matcher(value);
        while (matcher.find()) {
            String after = value.substring(matcher.end(), Math.min(value.length(), matcher.end() + 16)).toLowerCase(Locale.ROOT).trim();
            if (after.matches("^(kg|\\/\\s*kg|por\\s*kg|kilogramo).*")) continue;
            BigDecimal price = money(matcher.group(1));
            if (price != null) return price;
        }
        return null;
    }

    private Element findCard(Element link) {
        Element current = link;
        Element fallback = link.parent();
        for (int i = 0; i < 12 && current != null; i++, current = current.parent()) {
            String value = current.text().replace(' ', ' ').replace(' ', ' ');
            if (value.length() > 900) continue;
            boolean hasPrice = PRICE.matcher(value).find() || ONLINE_PRICE.matcher(value).find();
            boolean hasImage = !current.select("img").isEmpty();
            boolean hasTitle = !current.select("h1,h2,h3,h4,[class*=product-name],[class*=productName],[class*=product-title],[class*=productTitle],[data-testid*=product-name]").isEmpty();
            if (hasPrice && (hasImage || hasTitle)) return current;
        }
        return fallback;
    }

    private String cardName(Element link, Element card) {
        String title = firstNonBlank(link.attr("title"), link.attr("aria-label"));
        if (!title.isBlank() && isPlausibleName(title)) return title;
        if (card != null) {
            Element heading = card.selectFirst("h1,h2,h3,h4,[class*=product-name],[class*=productName],[class*=product-title],[class*=productTitle],[data-testid*=product-name]");
            if (heading != null && isPlausibleName(heading.text())) return heading.text();
            Element image = card.selectFirst("img[alt]");
            if (image != null && isPlausibleName(image.attr("alt"))) return image.attr("alt");
        }
        title = link.text();
        return isPlausibleName(title) ? title : "";
    }

    private boolean isProductLink(String href, String store) {
        if (href == null || href.isBlank()) return false;
        String path;
        try { path = URI.create(href).getPath().toLowerCase(Locale.ROOT); }
        catch (Exception e) { return false; }
        if (store.equals("Tottus")) return path.contains("/articulo/");
        return path.endsWith("/p") || path.endsWith("/p/");
    }

    private boolean isEssential(String name) {
        String s = ProductComparisonKey.normalize(name);
        return List.of("arroz", "aceite", "leche", "azucar", "fideo", "spaghetti", "pasta", "lenteja", "lentejas", "frejol", "frijol", "atun", "papa", "platano", "huevo", "harina", "quinua", "avena", "cebolla", "tomate", "menestra", "pollo", "sal ", "galleta", "pan ").stream().anyMatch(s::contains);
    }

    private boolean isPlausibleName(String name) {
        String s = name == null ? "" : name.trim();
        return s.length() >= 4 && s.length() <= 180 && !s.matches("(?i).*\\d+ resultados?.*") && isEssential(s);
    }

    private String categoryFor(String name, String fallback) {
        String s = ProductComparisonKey.normalize(name);
        if (List.of("leche", "huevo", "yogurt", "queso").stream().anyMatch(s::contains)) return "Lácteos";
        if (List.of("papa", "platano", "cebolla", "tomate", "zanahoria", "manzana", "fruta", "verdura").stream().anyMatch(s::contains)) return "Frutas y Verduras";
        return "Abarrotes";
    }

    private String cleanName(String value) {
        return value.replaceAll("\\s+", " ").replaceAll("[|•]+$", "").trim();
    }

    private String safeImage(String value, String category) {
        if (value != null && value.startsWith("https://") && !value.contains("data:image")) return value;
        if (category.equals("Lácteos")) return "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=700&q=80";
        if (category.equals("Frutas y Verduras")) return "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=700&q=80";
        return "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=700&q=80";
    }

    private String absolute(String value, String base) {
        if (value == null || value.isBlank()) return base;
        try { return URI.create(base).resolve(value).toString(); } catch (Exception e) { return base; }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return "";
    }

    private String text(JsonNode node) {
        return node == null || node.isNull() ? "" : node.isValueNode() ? node.asText().trim() : "";
    }

    private BigDecimal decimal(JsonNode node) {
        if (node == null) return null;
        try { return money(node.asText()); } catch (Exception e) { return null; }
    }

    private BigDecimal money(String value) {
        if (value == null) return null;
        String normalized = value.trim().replace(" ", "");
        if (normalized.contains(",") && normalized.contains(".")) normalized = normalized.replace(",", "");
        else normalized = normalized.replace(',', '.');
        BigDecimal price = new BigDecimal(normalized);
        return price.signum() > 0 && price.compareTo(new BigDecimal("1500")) < 0 ? price.setScale(2, java.math.RoundingMode.HALF_UP) : null;
    }

    private boolean iterableContains(JsonNode array, String value) {
        for (JsonNode n : array) if (n.asText().equalsIgnoreCase(value)) return true;
        return false;
    }

    private void add(Map<String, ScrapedOffer> result, ScrapedOffer offer) {
        if (offer.url() == null || offer.url().isBlank() || !offer.url().startsWith("https://")) return;
        if (offer.nombre().length() > 180 || !isEssential(offer.nombre())) return;
        String key = ProductComparisonKey.fromName(offer.nombre()) + "|" + offer.tienda();
        ScrapedOffer old = result.get(key);
        if (old == null) result.put(key, offer);
    }

    private String shortError(Exception error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) return error.getClass().getSimpleName();
        return message.length() > 100 ? message.substring(0, 100) : message;
    }

    private record Source(String tienda, String categoria, String url) {}
    private record StoreResult(String tienda, int pagesRead, List<ScrapedOffer> products, List<String> errors) {}
}
