/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m24.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Репозиторий Spring Data: интерфейс есть, реализации нет.
 *
 * <p>Реализация создаётся в runtime — это JDK-прокси (модуль 04), а запросы
 * выводятся из имён методов разбором сигнатуры (модуль 03). Хороший повод
 * вспомнить, что «магии» здесь нет: рефлексия плюс прокси.</p>
 *
 * @since 1.0
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Артикул.
     * @param sku Артикул
     * @return Артикул
     */
    Optional<Product> findBySku(String sku);

    /**
     * Значение {@code findByAvailableTrue}.
     * @return Значение {@code findByAvailableTrue}
     */
    List<Product> findByAvailableTrue();

    /**
     * Значение {@code findCheaperThan}.
     * @param max Максимум
     * @return Значение {@code findCheaperThan}
     */
    @Query("select p from Product p where p.price <= :max order by p.price")
    List<Product> findCheaperThan(BigDecimal max);
}
