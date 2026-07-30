package ru.sprbut.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("СХЕМА 1: узел Method")
class MethodApiTest {

    interface Greeter {
        String greet(String name);

        default String greetAll() {
            return "всем привет";
        }
    }

    private Method method(String name, Class<?>... params) throws NoSuchMethodException {
        return Order.class.getDeclaredMethod(name, params);
    }

    @Test
    @DisplayName("Возвращаемый тип читается напрямую; void — отдельный Class")
    void readsReturnType() throws NoSuchMethodException {
        assertThat(MethodApi.returnType(method("getId"))).isEqualTo(String.class);
        assertThat(MethodApi.isVoid(method("cancel"))).isTrue();
        assertThat(MethodApi.isVoid(method("getId"))).isFalse();
    }

    @Test
    @DisplayName("Типы параметров перечисляются в порядке объявления")
    void readsParameterTypes() throws NoSuchMethodException {
        assertThat(MethodApi.parameterTypeNames(method("pay", BigDecimal.class)))
                .containsExactly("BigDecimal");
        assertThat(MethodApi.parameterTypeNames(method("setCustomer", String.class)))
                .containsExactly("String");
    }

    @Test
    @DisplayName("Имена параметров доступны, потому что модуль собран с флагом -parameters")
    void readsParameterNames() throws NoSuchMethodException {
        assertThat(MethodApi.parameterNames(method("setCustomer", String.class)))
                .containsExactly("customer");
        assertThat(MethodApi.parameterNames(method("pay", BigDecimal.class)))
                .containsExactly("amount");
    }

    @Test
    @DisplayName("Объявленные checked-исключения видны в метаданных")
    void readsDeclaredExceptions() throws NoSuchMethodException {
        assertThat(MethodApi.declaredExceptions(method("pay", BigDecimal.class)))
                .containsExactly("PaymentException");
        assertThat(MethodApi.declaredExceptions(method("cancel"))).isEmpty();
    }

    @Test
    @DisplayName("varargs — это параметр-массив плюс отдельный флаг")
    void varargsIsAnArrayPlusFlag() throws NoSuchMethodException {
        Method addLines = method("addLines", BigDecimal[].class);

        assertThat(MethodApi.isVarArgs(addLines)).isTrue();
        assertThat(MethodApi.parameterTypeNames(addLines)).containsExactly("BigDecimal[]");
        assertThat(MethodApi.isVarArgs(method("cancel"))).isFalse();
    }

    @Test
    @DisplayName("default-метод интерфейса отличается от абстрактного")
    void detectsDefaultMethod() throws NoSuchMethodException {
        assertThat(MethodApi.isDefault(Greeter.class.getMethod("greetAll"))).isTrue();
        assertThat(MethodApi.isDefault(Greeter.class.getMethod("greet", String.class))).isFalse();
    }

    @Test
    @DisplayName("Bridge-метод создаёт компилятор — фреймворкам его надо отфильтровывать")
    void detectsBridgeMethod() {
        class StringGreeter implements java.util.function.Function<String, String> {
            @Override
            public String apply(String s) {
                return s;
            }
        }

        long bridges = Arrays.stream(StringGreeter.class.getDeclaredMethods())
                .filter(MethodApi::isBridge)
                .count();

        // компилятор добавил apply(Object) поверх apply(String) — это и есть bridge
        assertThat(bridges).isEqualTo(1);
    }

    @Test
    @DisplayName("Подпись метода собирается из метаданных для логов и ошибок")
    void buildsSignature() throws NoSuchMethodException {
        assertThat(MethodApi.signature(method("pay", BigDecimal.class)))
                .isEqualTo("void pay(BigDecimal)");
        assertThat(MethodApi.signature(method("addLines", BigDecimal[].class)))
                .isEqualTo("BigDecimal addLines(BigDecimal[])");
    }
}
