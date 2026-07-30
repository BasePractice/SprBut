package ru.sprbut.m01;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд 8: вызов методов, включая private")
final class ObjectMethodTest {

    @Test
    @DisplayName("приватный метод вызывается и возвращает результат")
    void invokesPrivateMethod() {
        assertThat(
            "private method cannot be invoked reflectively",
            new ObjectMethod(
                new Account("ACC-1", "Иванов", new BigDecimal("100.00")),
                "applyFee",
                BigDecimal.class
            ).call(new BigDecimal("15.00")),
            equalTo(new BigDecimal("85.00"))
        );
    }

    @Test
    @DisplayName("побочный эффект приватного метода применяется к объекту")
    void appliesSideEffect() {
        Account account = new Account("ACC-2", "Иванов", new BigDecimal("100.00"));
        new ObjectMethod(account, "block", String.class).call("подозрение");
        assertThat(
            "private void method cannot change the object state",
            account.isBlocked(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("void-метод возвращает null — возвращать ему нечего")
    void returnsNullFromVoid() {
        assertThat(
            "void method cannot return null",
            new ObjectMethod(
                new Account("ACC-3", "Иванов", BigDecimal.ONE), "block", String.class
            ).call("причина"),
            nullValue()
        );
    }

    @Test
    @DisplayName("исключение из метода приходит развёрнутым, а не как InvocationTargetException")
    void unwrapsRealCause() {
        assertThrows(
            NullPointerException.class,
            () -> new ObjectMethod(
                new Account("ACC-4", "Иванов", null), "applyFee", BigDecimal.class
            ).call(BigDecimal.ONE)
        );
    }

    @Test
    @DisplayName("несуществующий метод даёт понятную ошибку")
    void failsOnUnknownMethod() {
        assertThat(
            "unknown method cannot be reported with its own name",
            assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectMethod(
                    new Account("ACC-5", "Иванов", BigDecimal.ONE), "nope"
                ).call()
            ).getMessage(),
            containsString("nope")
        );
    }
}
