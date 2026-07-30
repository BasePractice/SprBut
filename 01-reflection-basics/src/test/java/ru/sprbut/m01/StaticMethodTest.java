package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд 8: вызов статического метода без экземпляра")
final class StaticMethodTest {

    @Test
    @DisplayName("статический метод вызывается с target = null")
    void invokesStaticMethod() {
        assertThat(
            "static method cannot be invoked without an instance",
            new StaticMethod(Account.class, "describeType").call(),
            equalTo("Счёт типа CHECKING")
        );
    }

    @Test
    @DisplayName("перегрузки различаются по типам параметров, а не по имени")
    void resolvesOverloadByParameterTypes() {
        assertThat(
            "overload cannot be resolved by its parameter types",
            new StaticMethod(String.class, "valueOf", int.class).call(42),
            equalTo("42")
        );
    }
}
