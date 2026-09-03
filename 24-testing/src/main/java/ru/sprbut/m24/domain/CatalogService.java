/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// исключение домена живёт рядом с сервисом, который его бросает
// @checkstyle ProhibitStaticNestedClassesCheck disable
// @checkstyle ParameterNameCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m24.domain;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @param currency Валюта из настроек
     */
    public CatalogService(
        final ProductRepository repository,
        @Value("${sprbut.catalog.currency:RUB}") final String currency) {
        this.repository = repository;
        this.currency = currency;
    }

    /**
     * Добавление.
     * @param sku Артикул
     * @param name Имя
     * @param price Цена
     * @return Добавление
     */
    @Transactional
    public Product add(final String sku, final String name, final BigDecimal price) {
        if (this.repository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException(
                String.format("Товар с артикулом %s уже есть", sku)
            );
        }
        return this.repository.save(new Product(sku, name, price));
    }

    /**
     * Смена цены товара.
     * @param sku Артикул
     * @param newPrice Новая цена
     * @return Товар с новой ценой
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
        return String.format("%s %s", product.getPrice(), this.currency);
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
     * Товар по артикулу.
     * @param sku Артикул
     * @return Товар по артикулу
     */
    @Transactional(readOnly = true)
    public Product bySku(final String sku) {
        return this.repository.findBySku(sku)
            .orElseThrow(() -> new CatalogService.ProductNotFoundException(sku));
    }

    /**
     * Исключение доменного слоя — контроллер превратит его в 404.
     * @since 1.0
     */
    public static final class ProductNotFoundException extends RuntimeException {

        /**
         * Основной конструктор.
         * @param sku Артикул
         */
        public ProductNotFoundException(final String sku) {
            super(String.format("Товар не найден: %s", sku));
        }
    }
}
