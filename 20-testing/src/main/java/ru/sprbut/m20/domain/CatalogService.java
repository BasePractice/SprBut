/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m20.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * Сервисный слой. В тестах контроллера он подменяется {@code @MockitoBean},
 * а в тестах сервиса — работает по-настоящему.
 * @since 1.0
 */
@Service
public class CatalogService {

    /**
     * Репозиторий.
     */
    private final ProductRepository repository;
    /**
     * Валюта.
     */
    private final String currency;

    /**
     * Основной конструктор.
     * @param repository Репозиторий
     * @param @Value("${sprbut.catalog.currency:RUB}" Значение
     */
    public CatalogService(final ProductRepository repository,
                          @Value("${sprbut.catalog.currency:RUB}") String currency) {
        this.repository = repository;
        this.currency = currency;
    }

    /**
     * Доступные элементы.
     * @return Доступные элементы
     */
    @Transactional(readOnly = true)
    public List<Product> available() {
        return this.repository.findByAvailableTrue();
    }

    /**
     * Артикул.
     * @param sku Артикул
     * @return Артикул
     */
    @Transactional(readOnly = true)
    public Product bySku(final String sku) {
        return this.repository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    /**
     * Добавление.
     * @param name Имя
     * @param price Цена
     * @param sku Артикул
     * @return Добавление
     */
    @Transactional
    public Product add(final String sku, final String name, final BigDecimal price) {
        if (this.repository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException("Товар с артикулом " + sku + " уже есть");
        }
        return this.repository.save(new Product(sku, name, price));
    }

    /**
     * Цена.
     * @param newPrice Цена
     * @param sku Артикул
     * @return Цена
     */
    @Transactional
    public Product changePrice(final String sku, final BigDecimal newPrice) {
        final Product product = this.bySku(sku);
        product.setPrice(newPrice);
        return product;
    }

    /**
     * Цена.
     * @param product Товар
     * @return Цена
     */
    public String priceTag(final Product product) {
        return product.getPrice() + " " + this.currency;
    }

    /**
     * Исключение доменного слоя — контроллер превратит его в 404.
     */
    public static class ProductNotFoundException extends RuntimeException {
        /**
         * Основной конструктор.
         * @param sku Артикул
         */
        public ProductNotFoundException(final String sku) {
            super("Товар не найден: " + sku);
        }
    }
}
