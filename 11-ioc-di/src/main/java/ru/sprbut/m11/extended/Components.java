/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.extended;

import java.util.ArrayList;
import java.util.List;

/**
 * Набор компонентов для {@link MiniContainer}: нормальный граф зависимостей
 * и все три патологии, из-за которых контейнер отказывается стартовать.
 * @since 1.0
 */
public final class Components {

    private Components() {
    }

    // --- Нормальный граф: repository ← service ← facade ----------------------

    /**
     * Репозиторий.
     */
    @MiniComponent
    public static class Repository {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Repository() {
            // нечего инициализировать
        }

        /**
         * Строки.
         */
        private final List<String> rows = new ArrayList<>();

        /**
         * Сохранение.
         * @param row Значение {@code row}
         */
        public void save(final String row) {
            this.rows.add(row);
        }

        /**
         * Строки.
         * @return Строки
         */
        public List<String> rows() {
            return List.copyOf(this.rows);
        }
    }

    /**
     * Часы.
     */
    @MiniComponent
    public static class Clock {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Clock() {
            // нечего инициализировать
        }

        /**
         * Момент времени.
         * @return Момент времени
         */
        public String now() {
            return "2026-07-30";
        }
    }

    /**
     * Порядок.
     */
    @MiniComponent("orders")
    public static class OrderService {

        /**
         * Репозиторий.
         */
        private final Repository repository;
        /**
         * Часы.
         */
        private final Clock clock;

        /**
         * Основной конструктор.
         * @param repository Репозиторий
         * @param clock Часы
         */
        public OrderService(final Repository repository, final Clock clock) {
            this.repository = repository;
            this.clock = clock;
        }

        /**
         * Размещение.
         * @param item Значение {@code item}
         * @return Размещение
         */
        public String place(final String item) {
            final String row = this.clock.now() + " " + item;
            this.repository.save(row);
            return row;
        }

        /**
         * Репозиторий.
         * @return Репозиторий
         */
        public Repository repository() {
            return this.repository;
        }
    }

    /**
     * Порядок.
     */
    @MiniComponent
    public static class OrderFacade {

        /**
         * Сервис.
         */
        private final OrderService service;

        /**
         * Основной конструктор.
         * @param service Сервис
         */
        public OrderFacade(final OrderService service) {
            this.service = service;
        }

        /**
         * Значение {@code checkout}.
         * @param item Значение {@code item}
         * @return Значение {@code checkout}
         */
        public String checkout(final String item) {
            return this.service.place(item);
        }

        /**
         * Сервис.
         * @return Сервис
         */
        public OrderService service() {
            return this.service;
        }
    }

    // --- Патология 1: зависимость, которой нет в контейнере ------------------

    /**
     * Не помечен {@code @MiniComponent} — контейнер о нём не знает.
     */
    public static class UnmanagedDependency {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public UnmanagedDependency() {
            // нечего инициализировать
        }

    }

    /**
     * Значение {@code NeedsUnmanaged}.
     */
    @MiniComponent
    public static class NeedsUnmanaged {

        /**
         * Основной конструктор.
         * @param dependency Зависимость
         */
        public NeedsUnmanaged(final UnmanagedDependency dependency) {
        }
    }

    // --- Патология 2: два кандидата на один тип ------------------------------

    /**
     * Значение {@code Payment}.
     */
    public interface Payment {
        /**
         * Вид.
         * @return Вид
         */
        String kind();
    }

    /**
     * Значение {@code CardPayment}.
     */
    @MiniComponent("cardPayment")
    public static class CardPayment implements Payment {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public CardPayment() {
            // нечего инициализировать
        }

        @Override
        public String kind() {
            return "card";
        }
    }

    /**
     * Значение {@code CashPayment}.
     */
    @MiniComponent("cashPayment")
    public static class CashPayment implements Payment {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public CashPayment() {
            // нечего инициализировать
        }

        @Override
        public String kind() {
            return "cash";
        }
    }

    // --- Патология 3: циклическая зависимость через конструкторы -------------

    /**
     * Сервис.
     */
    @MiniComponent
    public static class AlphaService {

        /**
         * Основной конструктор.
         * @param beta Бета-зависимость
         */
        public AlphaService(final BetaService beta) {
        }
    }

    /**
     * Сервис.
     */
    @MiniComponent
    public static class BetaService {

        /**
         * Основной конструктор.
         * @param alpha Альфа-зависимость
         */
        public BetaService(final AlphaService alpha) {
        }
    }

    // --- Патология 4: неоднозначный выбор конструктора -----------------------

    /**
     * Значение {@code TwoConstructors}.
     */
    @MiniComponent
    public static class TwoConstructors {

        /**
         * Основной конструктор.
         * @param repository Репозиторий
         */
        public TwoConstructors(final Repository repository) {
        }

        /**
         * Основной конструктор.
         * @param repository Репозиторий
         * @param clock Часы
         */
        public TwoConstructors(final Repository repository, final Clock clock) {
        }
    }
}
