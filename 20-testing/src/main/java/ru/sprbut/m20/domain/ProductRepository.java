package ru.sprbut.m20.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий Spring Data: интерфейс есть, реализации нет.
 * <p>
 * Реализация создаётся в runtime — это JDK-прокси (модуль 04), а запросы
 * выводятся из имён методов разбором сигнатуры (модуль 03). Хороший повод
 * вспомнить, что «магии» здесь нет: рефлексия плюс прокси.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByAvailableTrue();

    @Query("select p from Product p where p.price <= :max order by p.price")
    List<Product> findCheaperThan(BigDecimal max);
}
