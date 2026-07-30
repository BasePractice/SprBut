package ru.sprbut.m20.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m20.domain.CatalogService;
import ru.sprbut.m20.domain.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * Веб-слой. На нём показывается срез {@code @WebMvcTest}: поднимается только
 * MVC-инфраструктура, а {@link CatalogService} подменяется {@code @MockBean}.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalog;

    public ProductController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<ProductView> list() {
        return catalog.available().stream().map(ProductView::of).toList();
    }

    @GetMapping("/{sku}")
    public ProductView one(@PathVariable String sku) {
        return ProductView.of(catalog.bySku(sku));
    }

    @PostMapping
    public ResponseEntity<ProductView> create(@RequestBody CreateRequest request) {
        Product created = catalog.add(request.sku(), request.name(), request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductView.of(created));
    }

    @ExceptionHandler(CatalogService.ProductNotFoundException.class)
    public ResponseEntity<String> notFound(CatalogService.ProductNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /** DTO ответа — на нём показывается срез {@code @JsonTest}. */
    public record ProductView(String sku, String name, BigDecimal price, boolean available) {

        public static ProductView of(Product product) {
            return new ProductView(product.getSku(), product.getName(),
                    product.getPrice(), product.isAvailable());
        }
    }

    public record CreateRequest(String sku, String name, BigDecimal price) {
    }
}
