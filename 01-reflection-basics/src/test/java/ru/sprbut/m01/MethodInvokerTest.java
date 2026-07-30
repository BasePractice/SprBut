package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 8: вызов методов, включая private")
class MethodInvokerTest {

    private final Account account = new Account("ACC-1", "Иванов", new BigDecimal("100.00"));

    @Test
    @DisplayName("Приватный метод вызывается и возвращает результат")
    void invokesPrivateMethod() {
        Object result = MethodInvoker.invoke(account, "applyFee",
                new Class<?>[]{BigDecimal.class}, new BigDecimal("15.00"));

        assertThat(result).isEqualTo(new BigDecimal("85.00"));
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("85.00"));
    }

    @Test
    @DisplayName("Приватный void-метод возвращает null, но побочный эффект применяется")
    void invokesPrivateVoidMethod() {
        Object result = MethodInvoker.invoke(account, "block",
                new Class<?>[]{String.class}, "подозрительная операция");

        assertThat(result).isNull();
        assertThat(account.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("Статический метод вызывается с target = null")
    void invokesStaticMethod() {
        Object result = MethodInvoker.invokeStatic(Account.class, "describeType", new Class<?>[0]);

        assertThat(result).isEqualTo("Счёт типа CHECKING");
    }

    @Test
    @DisplayName("Исключение из метода приходит развёрнутым, а не как InvocationTargetException")
    void unwrapsInvocationTargetException() {
        Account broken = new Account("ACC-2", "Иванов", null);

        assertThatThrownBy(() -> MethodInvoker.invoke(broken, "applyFee",
                new Class<?>[]{BigDecimal.class}, BigDecimal.ONE))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Метод ищется по имени И по типам параметров — перегрузки различаются")
    void resolvesOverloadByParameterTypes() {
        assertThat(MethodInvoker.findMethod(String.class, "valueOf", int.class).getReturnType())
                .isEqualTo(String.class);
        assertThat(MethodInvoker.findMethod(String.class, "valueOf", char[].class).getParameterTypes())
                .containsExactly(char[].class);
    }

    @Test
    @DisplayName("Несуществующий метод — понятная ошибка")
    void failsOnUnknownMethod() {
        assertThatThrownBy(() -> MethodInvoker.invoke(account, "nope", new Class<?>[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nope");
    }
}
