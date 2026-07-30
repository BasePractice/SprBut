package ru.sprbut.m11.extended;

import java.util.ArrayList;
import java.util.List;

/**
 * Набор компонентов для {@link MiniContainer}: нормальный граф зависимостей
 * и все три патологии, из-за которых контейнер отказывается стартовать.
 */
public final class Components {

    private Components() {
    }

    // --- Нормальный граф: repository ← service ← facade ----------------------

    @MiniComponent
    public static class Repository {

        private final List<String> rows = new ArrayList<>();

        public void save(String row) {
            rows.add(row);
        }

        public List<String> rows() {
            return List.copyOf(rows);
        }
    }

    @MiniComponent
    public static class Clock {

        public String now() {
            return "2026-07-30";
        }
    }

    @MiniComponent("orders")
    public static class OrderService {

        private final Repository repository;
        private final Clock clock;

        public OrderService(Repository repository, Clock clock) {
            this.repository = repository;
            this.clock = clock;
        }

        public String place(String item) {
            String row = clock.now() + " " + item;
            repository.save(row);
            return row;
        }

        public Repository repository() {
            return repository;
        }
    }

    @MiniComponent
    public static class OrderFacade {

        private final OrderService service;

        public OrderFacade(OrderService service) {
            this.service = service;
        }

        public String checkout(String item) {
            return service.place(item);
        }

        public OrderService service() {
            return service;
        }
    }

    // --- Патология 1: зависимость, которой нет в контейнере ------------------

    /** Не помечен {@code @MiniComponent} — контейнер о нём не знает. */
    public static class UnmanagedDependency {
    }

    @MiniComponent
    public static class NeedsUnmanaged {

        public NeedsUnmanaged(UnmanagedDependency dependency) {
        }
    }

    // --- Патология 2: два кандидата на один тип ------------------------------

    public interface Payment {
        String kind();
    }

    @MiniComponent("cardPayment")
    public static class CardPayment implements Payment {
        @Override
        public String kind() {
            return "card";
        }
    }

    @MiniComponent("cashPayment")
    public static class CashPayment implements Payment {
        @Override
        public String kind() {
            return "cash";
        }
    }

    // --- Патология 3: циклическая зависимость через конструкторы -------------

    @MiniComponent
    public static class AlphaService {

        public AlphaService(BetaService beta) {
        }
    }

    @MiniComponent
    public static class BetaService {

        public BetaService(AlphaService alpha) {
        }
    }

    // --- Патология 4: неоднозначный выбор конструктора -----------------------

    @MiniComponent
    public static class TwoConstructors {

        public TwoConstructors(Repository repository) {
        }

        public TwoConstructors(Repository repository, Clock clock) {
        }
    }
}
