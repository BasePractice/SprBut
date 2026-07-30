package ru.sprbut.m03;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("СХЕМА 1: узел Method")
final class MethodSignatureTest {

    @Test
    @DisplayName("возвращаемый тип читается напрямую")
    void readsReturnType() throws NoSuchMethodException {
        assertThat(
            "return type cannot be read from the signature",
            new MethodSignature(Order.class.getMethod("getTotal")).returnType(),
            equalTo(BigDecimal.class)
        );
    }

    @Test
    @DisplayName("void — тоже отдельный Class, а не отсутствие типа")
    void treatsVoidAsType() throws NoSuchMethodException {
        assertThat(
            "void method cannot be recognised by its return type",
            new MethodSignature(Order.class.getMethod("cancel")).voidResult(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("типы параметров перечисляются в порядке объявления")
    void readsParameterTypes() throws NoSuchMethodException {
        assertThat(
            "parameter types cannot be listed in declaration order",
            new MethodSignature(Order.class.getMethod("setCustomer", String.class)).parameterTypes(),
            contains("String")
        );
    }

    @Test
    @DisplayName("имена параметров доступны, потому что модуль собран с флагом -parameters")
    void readsParameterNames() throws NoSuchMethodException {
        assertThat(
            "parameter names cannot survive compilation",
            new MethodSignature(Order.class.getMethod("setCustomer", String.class)).parameterNames(),
            contains("customer")
        );
    }

    @Test
    @DisplayName("объявленные исключения — то, что стоит после throws")
    void readsDeclaredExceptions() throws NoSuchMethodException {
        assertThat(
            "declared exceptions cannot be read from the signature",
            new MethodSignature(Order.class.getMethod("pay", BigDecimal.class)).exceptions(),
            contains("PaymentException")
        );
    }

    @Test
    @DisplayName("varargs распознаётся отдельным флагом")
    void detectsVarargs() throws NoSuchMethodException {
        assertThat(
            "varargs method cannot be detected by its flag",
            new MethodSignature(Order.class.getMethod("addLines", BigDecimal[].class)).varargs(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("в байткоде varargs — обычный параметр-массив")
    void treatsVarargsAsArray() throws NoSuchMethodException {
        assertThat(
            "varargs parameter cannot appear as an array",
            new MethodSignature(Order.class.getMethod("addLines", BigDecimal[].class))
                .parameterTypes(),
            contains("BigDecimal[]")
        );
    }

    @Test
    @DisplayName("компактная подпись годится для логов и сообщений об ошибках")
    void printsCompactSignature() throws NoSuchMethodException {
        assertThat(
            "compact signature cannot be printed",
            new MethodSignature(Order.class.getMethod("pay", BigDecimal.class)).text(),
            equalTo("void pay(BigDecimal)")
        );
    }
}
