package pe.agromarketperu.model;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

public final class ProductComparisonKey {
    private static final Map<String, String> NAMES = Map.ofEntries(
        Map.entry("arroz-costeno-5kg", "Arroz Costeño 5 kg"),
        Map.entry("arroz-superior-costeno-5kg", "Arroz Superior Costeño 5 kg"),
        Map.entry("arroz-costeno-anejo-5kg", "Arroz Costeño Extra Añejo 5 kg"),
        Map.entry("aceite-primor-900ml", "Aceite Primor 900 ml"),
        Map.entry("aceite-primor-1800ml", "Aceite Primor 1.8 L"),
        Map.entry("aceite-primor-1000ml", "Aceite Primor 1 L"),
        Map.entry("aceite-primor-premium-900ml", "Aceite Primor Premium 900 ml"),
        Map.entry("aceite-primor-premium-1000ml", "Aceite Primor Premium 1 L"),
        Map.entry("aceite-primor-premium-1800ml", "Aceite Primor Premium 1.8 L"),
        Map.entry("leche-gloria-sixpack-390g", "Leche Gloria evaporada sixpack 390 g"),
        Map.entry("leche-gloria-sixpack-170g", "Leche Gloria sixpack 170 g"),
        Map.entry("leche-gloria-light-sixpack-390g", "Leche Gloria light sixpack 390 g"),
        Map.entry("leche-gloria-light-sixpack-170g", "Leche Gloria light sixpack 170 g"),
        Map.entry("leche-gloria-zero-lacto-sixpack-390g", "Leche Gloria zero lacto sixpack 390 g"),
        Map.entry("leche-gloria-zero-lacto-sixpack-170g", "Leche Gloria zero lacto sixpack 170 g"),
        Map.entry("leche-gloria-slim-triple-zero-pack3-946ml", "Leche Gloria Slim Triple Zero pack de 3 x 946 ml"),
        Map.entry("leche-gloria-ninos-sixpack-390g", "Leche Gloria niños sixpack 390 g"),
        Map.entry("leche-gloria-ninos-sixpack-170g", "Leche Gloria niños sixpack 170 g"),
        Map.entry("leche-gloria-quinua-sixpack-390g", "Leche Gloria con quinua sixpack 390 g"),
        Map.entry("leche-gloria-reconstituida-sixpack-390g", "Leche Gloria reconstituida sixpack 390 g"),
        Map.entry("leche-gloria-pack3-946ml", "Leche Gloria entera pack de 3 x 946 ml"),
        Map.entry("leche-gloria-light-pack3-946ml", "Leche Gloria light pack de 3 x 946 ml"),
        Map.entry("leche-gloria-zero-lacto-pack3-946ml", "Leche Gloria zero lacto pack de 3 x 946 ml"),
        Map.entry("leche-gloria-ninos-pack3-946ml", "Leche Gloria niños pack de 3 x 946 ml"),
        Map.entry("azucar-rubia-1kg", "Azúcar rubia 1 kg"),
        Map.entry("fideos-spaghetti-500g", "Fideos spaghetti 500 g"),
        Map.entry("lenteja-500g", "Lentejas 500 g"),
        Map.entry("atun-aceite-170g", "Atún en aceite 170 g"),
        Map.entry("papa-blanca-1kg", "Papa blanca 1 kg"),
        Map.entry("platano-seda-1kg", "Plátano de seda 1 kg"),
        Map.entry("huevos-15", "Huevos, paquete de 15"));

    private ProductComparisonKey() {}

    public static String fromName(String name) {
        String s = normalize(name);
        if (s.contains("arroz") && s.contains("costeno") && s.contains("superior") && s.contains("5kg")) return "arroz-superior-costeno-5kg";
        if (s.contains("arroz") && s.contains("costeno") && s.contains("anejo") && s.contains("5kg")) return "arroz-costeno-anejo-5kg";
        if (s.contains("arroz") && s.contains("costeno") && !s.contains("superior") && !s.contains("anejo") && s.contains("5kg")) return "arroz-costeno-5kg";
        if (s.contains("aceite") && s.contains("primor")) {
            boolean premium = s.contains("premium");
            if (s.contains("1 8l") || s.contains("1800ml") || s.contains("1.8l")) return premium ? "aceite-primor-premium-1800ml" : "aceite-primor-1800ml";
            if (s.contains("900ml") || s.contains("900 ml")) return premium ? "aceite-primor-premium-900ml" : "aceite-primor-900ml";
            if (s.contains("1l") || s.contains("1 litro")) return premium ? "aceite-primor-premium-1000ml" : "aceite-primor-1000ml";
        }
        if (s.contains("leche") && s.contains("gloria")) {
            boolean sixpack = s.contains("sixpack") || s.contains("six pack") || s.contains("six-pack");
            boolean ninos = s.contains("ninos");
            boolean zeroLacto = s.contains("zero lacto") || s.contains("sin lactosa");
            boolean light = s.contains("light");
            boolean slimTripleZero = s.contains("slim triple zero");
            boolean quinua = s.contains("quinua") || s.contains("kiwicha") || s.contains("cani hua") || s.contains("canihua");
            boolean reconstituida = s.contains("reconstituida");
            if (sixpack) {
                boolean g170 = s.contains("170g") || s.contains("170 g");
                boolean g390 = s.contains("390g") || s.contains("390 g");
                if (ninos) return g170 ? "leche-gloria-ninos-sixpack-170g" : g390 ? "leche-gloria-ninos-sixpack-390g" : "leche-gloria-ninos-sixpack";
                if (zeroLacto) return g170 ? "leche-gloria-zero-lacto-sixpack-170g" : g390 ? "leche-gloria-zero-lacto-sixpack-390g" : "leche-gloria-zero-lacto-sixpack";
                if (light) return g170 ? "leche-gloria-light-sixpack-170g" : g390 ? "leche-gloria-light-sixpack-390g" : "leche-gloria-light-sixpack";
                if (quinua) return "leche-gloria-quinua-sixpack-390g";
                if (reconstituida) return "leche-gloria-reconstituida-sixpack-390g";
                if (g170) return "leche-gloria-sixpack-170g";
                if (g390) return "leche-gloria-sixpack-390g";
                return "leche-gloria-sixpack";
            }
            if ((s.contains("pack 3") || s.contains("pack3") || s.contains("tripack") || (s.contains("3 cajas") && s.contains("946"))) && s.contains("946")) {
                if (slimTripleZero) return "leche-gloria-slim-triple-zero-pack3-946ml";
                if (ninos) return "leche-gloria-ninos-pack3-946ml";
                if (zeroLacto) return "leche-gloria-zero-lacto-pack3-946ml";
                if (light) return "leche-gloria-light-pack3-946ml";
                return "leche-gloria-pack3-946ml";
            }
        }
        if (s.contains("azucar") && s.contains("rubia") && (s.contains("1kg") || s.contains("1 kg"))) return "azucar-rubia-1kg";
        if (s.contains("spaghetti") && s.contains("500g")) return "fideos-spaghetti-500g";
        if (s.contains("lenteja") && s.contains("500g")) return "lenteja-500g";
        if (s.contains("atun") && (s.contains("170g") || s.contains("170 g")) && s.contains("aceite")) return "atun-aceite-170g";
        if (s.contains("papa") && (s.contains("1kg") || s.contains("1 kg"))) return "papa-blanca-1kg";
        if (s.contains("platano") && (s.contains("1kg") || s.contains("1 kg"))) return "platano-seda-1kg";
        if (s.contains("huevo") && (s.contains("15") || s.contains("quincena"))) return "huevos-15";
        return s.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    public static String displayName(String key, String fallback) {
        return NAMES.getOrDefault(key, fallback);
    }

    public static String normalize(String value) {
        String s = value == null ? "" : value.toLowerCase(Locale.ROOT);
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replaceAll("(?i)\\b(kilos?|kgs?)\\b", "kg").replaceAll("(?i)\\b(mililitros?)\\b", "ml");
        s = s.replaceAll("(?i)(\\d)\\s+(kg|ml|g|l)\\b", "$1$2");
        return s.replaceAll("[^a-z0-9. ]", " ").replaceAll("\\s+", " ").trim();
    }
}
