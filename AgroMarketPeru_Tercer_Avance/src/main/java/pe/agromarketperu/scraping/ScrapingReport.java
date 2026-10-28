package pe.agromarketperu.scraping;

import java.time.LocalDateTime;
import java.util.List;

public record ScrapingReport(LocalDateTime fecha, int productosEncontrados, int creados, int preciosActualizados, int historialRegistrado, List<String> fuentesCorrectas, List<String> errores, String mensaje) {}
