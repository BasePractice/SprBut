/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// JPA-сущность: сеттеры принимают одноимённые полям параметры
// @checkstyle HiddenFieldCheck disable
// @checkstyle ConstructorsOrderCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m24.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Сущность каталога. На ней показывается срез {@code @DataJpaTest}.
 * @since 1.0
 */
@Entity
@Table(name = "products")
@SuppressWarnings({"PMD.DataClass", "PMD.ConstructorShouldDoInitialization"})
public class Product {

    /**
     * Идентификатор.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Артикул.
     */
    @Column(nullable = false, unique = true)
    private String sku;

    /**
     * Имя.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Цена.
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * Доступные элементы.
     */
    private boolean available = true;

    protected Product() {
        // требуется JPA
    }

    /**
     * Основной конструктор.
     * @param sku Артикул
     * @param name Имя
     * @param price Цена
     */
    public Product(final String sku, final String name, final BigDecimal price) {
        this.sku = sku;
        this.name = name;
        this.price = price;
    }

    /**
     * Значение свойства {@code id}.
     * @return Значение свойства {@code id}
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Значение свойства {@code sku}.
     * @return Значение свойства {@code sku}
     */
    public String getSku() {
        return this.sku;
    }

    /**
     * Значение свойства {@code name}.
     * @return Значение свойства {@code name}
     */
    public String getName() {
        return this.name;
    }

    /**
     * Значение свойства {@code price}.
     * @return Значение свойства {@code price}
     */
    public BigDecimal getPrice() {
        return this.price;
    }

    /**
     * Новое значение свойства {@code price}.
     * @param price Цена
     */
    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    /**
     * Значение: доступные элементы.
     * @return Значение: доступные элементы
     */
    public boolean isAvailable() {
        return this.available;
    }

    /**
     * Новое значение: доступные элементы.
     * @param available Доступные элементы
     */
    public void setAvailable(final boolean available) {
        this.available = available;
    }
}
