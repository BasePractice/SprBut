package ru.sprbut.m20.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Сервисный слой. В тестах контроллера он подменяется {@code @MockitoBean},
 * а в тестах сервиса — работает по-настоящему.
 */
@Service
public class CatalogService {

    private final ProductRepository repository;
    private final String currency;

    public CatalogService(ProductRepository repository,
                          @Value("${sprbut.catalog.currency:RUB}") String currency) {
        this.repository = repository;
        this.currency = currency;
    }

    @Transactional(readOnly = true)
    public List<Product> available() {
        return repository.findByAvailableTrue();
    }

    @Transactional(readOnly = true)
    public Product bySku(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @Transactional
    public Product add(String sku, String name, BigDecimal price) {
        if (repository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException("Товар с артикулом " + sku + " уже есть");
        }
        return repository.save(new Product(sku, name, price));
    }

    @Transactional
    public Product changePrice(String sku, BigDecimal newPrice) {
        Product product = bySku(sku);
        product.setPrice(newPrice);
        return product;
    }

    public String priceTag(Product product) {
        return product.getPrice() + " " + currency;
    }

    /** Исключение доменного слоя — контроллер превратит его в 404. */
    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String sku) {
            super("Товар не найден: " + sku);
        }
    }
}
