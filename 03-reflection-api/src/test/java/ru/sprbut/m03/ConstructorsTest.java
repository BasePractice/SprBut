package ru.sprbut.m03;

import java.math.BigDecimal;
import java.util.AbstractList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("СХЕМА 1: узел Constructor")
final class ConstructorsTest {

    @Test
    @DisplayName("конструктор без параметров находится отдельным запросом")
    void findsNoArgConstructor() {
        assertThat(
            "no-arg constructor cannot be found",
            new Constructors(Order.class).noArg().isPresent(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("«жадная» стратегия выбирает конструктор с максимумом параметров")
    void picksGreediestConstructor() {
        assertThat(
            "greediest strategy cannot pick the widest constructor",
            new Constructors(Order.class).greediest().orElseThrow().getParameterCount(),
            equalTo(2)
        );
    }

    @Test
    @DisplayName("объявленных конструкторов не меньше, чем публичных")
    void countsDeclaredConstructors() {
        assertThat(
            "declared constructors cannot include the non public ones",
            new Constructors(Order.class).declaredArities(),
            hasItem(2)
        );
    }

    @Test
    @DisplayName("подбор учитывает количество и типы аргументов")
    void matchesByArgumentTypes() {
        assertThat(
            "constructor cannot be matched by argument types",
            new Constructors(Order.class)
                .matching("A-1", new BigDecimal("100"))
                .orElseThrow()
                .getParameterCount(),
            equalTo(2)
        );
    }

    @Test
    @DisplayName("null подходит ссылочному параметру")
    void acceptsNullForReferenceParameter() {
        assertThat(
            "null cannot fit a reference parameter",
            new Constructors(Order.class).matching(new Object[]{null}).isPresent(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("аргумент неподходящего типа конструктор не выбирает")
    void dontMatchWrongTypes() {
        assertThat(
            "wrong argument type cannot be rejected",
            new Constructors(Order.class).matching(42, 42).isPresent(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("newInstance() создаёт объект — точка входа любого IoC-контейнера")
    void createsInstance() {
        assertThat(
            "reflection cannot create the object",
            ((Order) new NewInstance(Order.class, "A-1", new BigDecimal("100")).object()).getId(),
            equalTo("A-1")
        );
    }

    @Test
    @DisplayName("у абстрактного типа экземпляра быть не может")
    void dontInstantiateAbstractType() {
        assertThrows(
            RuntimeException.class,
            () -> new NewInstance(AbstractList.class).object()
        );
    }

    @Test
    @DisplayName("нет подходящего конструктора — понятное сообщение с аргументами")
    void reportsMissingConstructor() {
        assertThat(
            "missing constructor cannot be reported with the arguments",
            assertThrows(
                IllegalArgumentException.class,
                () -> new NewInstance(Order.class, 1, 2, 3).object()
            ).getMessage(),
            containsString("Order")
        );
    }
}
