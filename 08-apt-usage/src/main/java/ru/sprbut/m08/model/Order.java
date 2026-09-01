/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m08.model;

import ru.sprbut.m07.api.GenerateBuilder;
import ru.sprbut.m07.api.Todo;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Второй бин с генерацией билдера — плюс {@code @Todo}, чтобы в логе сборки
 * было видно работу процессора-анализатора (слайд 60).
 *
 * <p>Суффикс имени генерируемого класса переопределён: получится {@code OrderMaker}.</p>
 *
 * @since 1.0
 */
@GenerateBuilder(suffix = "Maker")
public class Order {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Order() {
        // нечего инициализировать
    }

    /**
     * Номер.
     */
    private String number;
    /**
     * Идентификатор.
     */
    private String customerId;
    /**
     * Итоговая сумма.
     */
    private BigDecimal total;
    /**
     * Момент размещения.
     */
    private LocalDate placedOn;

    /**
     * Статус.
     */
    @Todo("перевести на enum статусов")
    private String status;

    /**
     * Значение свойства {@code number}.
     * @return Значение свойства {@code number}
     */
    public String getNumber() {
        return this.number;
    }

    /**
     * Новое значение свойства {@code number}.
     * @param number Номер
     */
    public void setNumber(final String number) {
        this.number = number;
    }

    /**
     * Значение свойства {@code customerId}.
     * @return Значение свойства {@code customerId}
     */
    public String getCustomerId() {
        return this.customerId;
    }

    /**
     * Новое значение свойства {@code customerId}.
     * @param customerId Идентификатор
     */
    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    /**
     * Значение: итоговая сумма.
     * @return Значение: итоговая сумма
     */
    public BigDecimal getTotal() {
        return this.total;
    }

    /**
     * Новое значение: итоговая сумма.
     * @param total Итоговая сумма
     */
    public void setTotal(final BigDecimal total) {
        this.total = total;
    }

    /**
     * Значение: момент размещения.
     * @return Значение: момент размещения
     */
    public LocalDate getPlacedOn() {
        return this.placedOn;
    }

    /**
     * Новое значение: момент размещения.
     * @param placedOn Момент размещения
     */
    public void setPlacedOn(final LocalDate placedOn) {
        this.placedOn = placedOn;
    }

    /**
     * Значение свойства {@code status}.
     * @return Значение свойства {@code status}
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Новое значение свойства {@code status}.
     * @param status Статус
     */
    public void setStatus(final String status) {
        this.status = status;
    }
}
