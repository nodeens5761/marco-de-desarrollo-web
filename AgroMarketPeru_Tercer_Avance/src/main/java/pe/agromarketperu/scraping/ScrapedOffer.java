package pe.agromarketperu.scraping;

import java.math.BigDecimal;

public record ScrapedOffer(String nombre, String categoria, String tienda, BigDecimal precio, String imagen, String url) {}
