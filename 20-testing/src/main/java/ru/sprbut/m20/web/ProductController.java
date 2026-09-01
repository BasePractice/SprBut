/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
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
 * MVC-инфраструктура, а {@link CatalogService} подменяется {@code @MockitoBean}.
 * @since 1.0
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /**
     * Каталог.
     */
    private final CatalogService catalog;

    /**
     * Основной конструктор.
     * @param catalog Каталог
     */
    public ProductController(final CatalogService catalog) {
        this.catalog = catalog;
    }

    /**
     * Список.
     * @return Список
     */
    @GetMapping
    public List<ProductView> list() {
        return this.catalog.available().stream().map(ProductView::of).toList();
    }

    /**
     * Единственный элемент.
     * @param sku Артикул
     * @return Единственный элемент
     */
    @GetMapping("/{sku}")
    public ProductView one(final @PathVariable String sku) {
        return ProductView.of(this.catalog.bySku(sku));
    }

    /**
     * Создание.
     * @param request Запрос
     * @return Создание
     */
    @PostMapping
    public ResponseEntity<ProductView> create(final @RequestBody CreateRequest request) {
        final Product created = this.catalog.add(request.sku(), request.name(), request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductView.of(created));
    }

    /**
     * Отсутствующий элемент.
     * @param e Событие
     * @return Отсутствующий элемент
     */
    @ExceptionHandler(CatalogService.ProductNotFoundException.class)
    public ResponseEntity<String> notFound(final CatalogService.ProductNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    /**
     * Запрос.
     * @param e Событие
     * @return Запрос
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(final IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /** DTO ответа — на нём показывается срез {@code @JsonTest}. */
    public record ProductView(String sku, String name, BigDecimal price, boolean available) {

        /**
         * Источник.
         * @param product Товар
         * @return Источник
         */
        public static ProductView of(final Product product) {
            return new ProductView(product.getSku(), product.getName(),
                    product.getPrice(), product.isAvailable());
        }
    }

    /**
     * Запрос.
     * @param sku Артикул
     * @param name Имя
     * @param price Цена
     * @return Запрос
     */
    public record CreateRequest(String sku, String name, BigDecimal price) {
    }
}
