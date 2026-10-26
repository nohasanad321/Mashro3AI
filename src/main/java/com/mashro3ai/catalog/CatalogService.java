package com.mashro3ai.catalog;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/** Small, versioned business catalogue. Prices always come from the owner, never the model. */
@Service
public class CatalogService {
    private final List<Product> products;

    public CatalogService(JsonMapper mapper) throws java.io.IOException {
        try (var stream = new ClassPathResource("catalog.json").getInputStream()) {
            products = List.of(mapper.readValue(stream, Product[].class));
        }
    }

    public List<Product> products() { return products; }

    public Optional<Product> find(String sku, String description) {
        return products.stream().filter(p -> (sku != null && p.sku().equals(sku)) ||
            (description != null && p.names().stream().anyMatch(n -> normalize(n).equals(normalize(description))))).findFirst();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
            .replaceAll("[\\p{M}ـ]", "").replaceAll("[أإآ]", "ا").strip();
    }

    public record Product(String sku, String name, String arabicName, BigDecimal price, List<String> aliases) {
        public UUID id() { return UUID.nameUUIDFromBytes(sku.getBytes(StandardCharsets.UTF_8)); }
        public List<String> names() {
            var names = new ArrayList<>(aliases); names.add(name); names.add(arabicName); return names;
        }
    }
}
