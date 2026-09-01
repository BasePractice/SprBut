/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// подопытные компоненты собраны в одном файле: так виден весь граф
// зависимостей, который разбирает мини-контейнер
// @checkstyle ProhibitStaticNestedClassesCheck disable
// @checkstyle ConstructorsOrderCheck disable
package ru.sprbut.m11.extended;

import java.util.ArrayList;
import java.util.List;

/**
 * Набор компонентов для {@link MiniContainer}: нормальный граф зависимостей
 * и все три патологии, из-за которых контейнер отказывается стартовать.
 * @since 1.0
 */
// класс — витрина вложенных компонентов, у него самого состояния нет,
// но и утилитой он не является: контейнер получает именно вложенные типы
@SuppressWarnings({"PMD.ConstructorShouldDoInitialization", "PMD.InstantiableUtilityClass"})
public final class Components {

    /**
     * Открытый конструктор: класс существует ради вложенных компонентов.
     */
    public Components() {
        // состояния у набора нет
    }

    // --- Нормальный граф: repository берётся из service берётся из facade ----------------------

    /**
     * Репозиторий.
     * @since 1.0
     */
    @MiniComponent
    public static final class Repository {

        /**
         * Строки.
         */
        private final List<String> rows = new ArrayList<>(0);

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Repository() {
            // нечего инициализировать
        }

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
     * @since 1.0
     */
    @MiniComponent
    public static final class Clock {

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
        // @checkstyle NonStaticMethodCheck (3 lines)
        public String now() {
            return "2026-07-30";
        }
    }

    /**
     * Порядок.
     * @since 1.0
     */
    @MiniComponent("orders")
    public static final class OrderService {

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
            final String row = String.format("%s %s", this.clock.now(), item);
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
     * @since 1.0
     */
    @MiniComponent
    public static final class OrderFacade {

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
     * @since 1.0
     */
    public static final class UnmanagedDependency {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public UnmanagedDependency() {
            // нечего инициализировать
        }
    }

    /**
     * Значение {@code NeedsUnmanaged}.
     * @since 1.0
     */
    @MiniComponent
    public static final class NeedsUnmanaged {

        /**
         * Основной конструктор.
         * @param dependency Зависимость
         */
        public NeedsUnmanaged(final UnmanagedDependency dependency) {
            // тело намеренно пустое
        }
    }

    // --- Патология 2: два кандидата на один тип ------------------------------

    /**
     * Значение {@code Payment}.
     * @since 1.0
     */
    @SuppressWarnings("PMD.ImplicitFunctionalInterface")
    public interface Payment {

        /**
         * Вид.
         * @return Вид
         */
        String kind();
    }

    /**
     * Значение {@code CardPayment}.
     * @since 1.0
     */
    @MiniComponent("cardPayment")
    public static final class CardPayment implements Payment {

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
     * @since 1.0
     */
    @MiniComponent("cashPayment")
    public static final class CashPayment implements Payment {

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
     * @since 1.0
     */
    @MiniComponent
    public static final class AlphaService {

        /**
         * Основной конструктор.
         * @param beta Бета-зависимость
         */
        public AlphaService(final BetaService beta) {
            // тело намеренно пустое
        }
    }

    /**
     * Сервис.
     * @since 1.0
     */
    @MiniComponent
    public static final class BetaService {

        /**
         * Основной конструктор.
         * @param alpha Альфа-зависимость
         */
        public BetaService(final AlphaService alpha) {
            // тело намеренно пустое
        }
    }

    // --- Патология 4: неоднозначный выбор конструктора -----------------------

    /**
     * Значение {@code TwoConstructors}.
     * @since 1.0
     */
    @MiniComponent
    public static final class TwoConstructors {

        /**
         * Основной конструктор.
         * @param repository Репозиторий
         */
        public TwoConstructors(final Repository repository) {
            // тело намеренно пустое
        }

        /**
         * Основной конструктор.
         * @param repository Репозиторий
         * @param clock Часы
         */
        public TwoConstructors(final Repository repository, final Clock clock) {
            // тело намеренно пустое
        }
    }
}
