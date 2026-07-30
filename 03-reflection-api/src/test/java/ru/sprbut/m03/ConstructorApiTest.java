package ru.sprbut.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("СХЕМА 1: узел Constructor")
class ConstructorApiTest {

    @Test
    @DisplayName("getConstructors() отдаёт только публичные, getDeclaredConstructors() — все")
    void publicVsDeclared() {
        assertThat(ConstructorApi.publicConstructorArities(Order.class))
                .containsExactly(0, 1, 2);
        assertThat(ConstructorApi.declaredConstructorArities(Order.class))
                .containsExactly(0, 1, 2, 3);
    }

    @Test
    @DisplayName("Конструктор без параметров находится отдельным запросом")
    void findsNoArgConstructor() {
        assertThat(ConstructorApi.noArgConstructor(Order.class)).isPresent();
        assertThat(ConstructorApi.noArgConstructor(Integer.class)).isEmpty();
    }

    @Test
    @DisplayName("«Жадная» стратегия выбирает конструктор с максимумом параметров")
    void picksGreediestConstructor() {
        assertThat(ConstructorApi.greediestPublicConstructor(Order.class))
                .get()
                .extracting(java.lang.reflect.Constructor::getParameterCount)
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Подбор конструктора учитывает количество и типы аргументов")
    void matchesByArgumentTypes() {
        assertThat(ConstructorApi.findMatching(Order.class, "A-1", new BigDecimal("10")))
                .get()
                .extracting(c -> c.getParameterTypes()[1])
                .isEqualTo(BigDecimal.class);

        assertThat(ConstructorApi.findMatching(Order.class, "A-1", "Иванов", new BigDecimal("10")))
                .get()
                .extracting(java.lang.reflect.Constructor::getParameterCount)
                .isEqualTo(3);
    }

    @Test
    @DisplayName("null подходит ссылочному параметру и не подходит примитивному")
    void nullMatchesOnlyReferenceTypes() {
        assertThat(ConstructorApi.matches(new Class<?>[]{String.class}, new Object[]{null})).isTrue();
        assertThat(ConstructorApi.matches(new Class<?>[]{int.class}, new Object[]{null})).isFalse();
    }

    @Test
    @DisplayName("Совместимость типов проверяется с учётом автобоксинга")
    void matchesWithBoxing() {
        assertThat(ConstructorApi.matches(new Class<?>[]{int.class}, new Object[]{42})).isTrue();
        assertThat(ConstructorApi.matches(new Class<?>[]{boolean.class}, new Object[]{"да"})).isFalse();
    }

    @Test
    @DisplayName("newInstance() создаёт объект — точка входа любого IoC-контейнера")
    void createsInstance() {
        Order order = (Order) ConstructorApi.instantiate(Order.class, "A-1", new BigDecimal("99.90"));

        assertThat(order.getId()).isEqualTo("A-1");
        assertThat(order.getTotal()).isEqualByComparingTo("99.90");
    }

    @Test
    @DisplayName("Защищённый конструктор доступен через setAccessible(true)")
    void createsViaProtectedConstructor() {
        Order order = (Order) ConstructorApi
                .instantiate(Order.class, "A-2", "Иванов", new BigDecimal("5"));

        assertThat(order.getCustomer()).isEqualTo("Иванов");
    }

    @Test
    @DisplayName("У абстрактного класса конструктор есть, но newInstance() всё равно падает")
    void rejectsAbstractTypes() {
        // Конструктор находится — абстрактность выясняется только в момент создания
        assertThat(ConstructorApi.noArgConstructor(java.util.AbstractList.class)).isPresent();

        assertThatThrownBy(() -> ConstructorApi.instantiate(java.util.AbstractList.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("абстрактный класс")
                .hasCauseInstanceOf(InstantiationException.class);
    }

    @Test
    @DisplayName("Нет подходящего конструктора — понятное сообщение с аргументами")
    void reportsMissingConstructor() {
        assertThatThrownBy(() -> ConstructorApi.instantiate(Order.class, 1, 2, 3, 4, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Нет конструктора");
    }
}
