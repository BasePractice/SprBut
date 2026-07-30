package ru.sprbut.m03.extended;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Расширенный пример: строка превращается в вызов метода")
final class CommandTest {

    @Test
    @DisplayName("класс загружается по имени, конструктор подбирается по аргументам")
    void buildsTargetFromCommand() {
        assertThat(
            "command cannot build the target object",
            new Command("ru.sprbut.m03.model.Order(A-1,100)#getId").invocation().result(),
            equalTo("A-1")
        );
    }

    @Test
    @DisplayName("строковые аргументы приводятся к типам параметров")
    void convertsArgumentsByParameterTypes() {
        assertThat(
            "string arguments cannot be converted by parameter types",
            new Command("ru.sprbut.m03.model.Order(A-1,100)#getTotal").invocation().result(),
            equalTo(new BigDecimal("100"))
        );
    }

    @Test
    @DisplayName("varargs собирается в массив через Array.newInstance")
    void packsVarargsIntoArray() {
        assertThat(
            "varargs tail cannot be packed into an array",
            new Command("ru.sprbut.m03.model.Order(A-1,0)#addLines(10,20)").invocation().result(),
            equalTo(new BigDecimal("30"))
        );
    }

    @Test
    @DisplayName("часть с конструктором можно опустить")
    void usesNoArgConstructorByDefault() {
        assertThat(
            "command without constructor args cannot use the no-arg one",
            new Command("ru.sprbut.m03.model.Order#isPaid").invocation().result(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("отчёт называет выбранный конструктор")
    void reportsChosenConstructor() {
        assertThat(
            "report cannot name the chosen constructor",
            new Command("ru.sprbut.m03.model.Order(A-1,100)#getId").invocation().constructor(),
            equalTo("Order(String, BigDecimal)")
        );
    }

    @Test
    @DisplayName("отчёт называет подпись выбранного метода")
    void reportsMethodSignature() {
        assertThat(
            "report cannot name the chosen method signature",
            new Command("ru.sprbut.m03.model.Order(A-1,100)#getId").invocation().signature(),
            equalTo("String getId()")
        );
    }

    @Test
    @DisplayName("приватный метод тоже вызывается — setAccessible снимает проверку")
    void invokesPrivateMethod() {
        assertThat(
            "private method cannot be invoked by the command",
            String.valueOf(
                new Command("ru.sprbut.m03.model.Order(A-1,100)#internalTag").invocation().result()
            ),
            containsString("A-1")
        );
    }

    @Test
    @DisplayName("неизвестный класс отбивается на разборе, а не на вызове")
    void dontRunUnknownClass() {
        assertThat(
            "unknown class cannot be reported by name",
            assertThrows(
                IllegalArgumentException.class,
                () -> new Command("ru.sprbut.Nope#toString").invocation()
            ).getMessage(),
            containsString("ru.sprbut.Nope")
        );
    }

    @Test
    @DisplayName("неизвестный метод отбивается с указанием числа аргументов")
    void dontRunUnknownMethod() {
        assertThat(
            "unknown method cannot be reported with its arity",
            assertThrows(
                IllegalArgumentException.class,
                () -> new Command("ru.sprbut.m03.model.Order#nope(1)").invocation()
            ).getMessage(),
            containsString("nope")
        );
    }

    @Test
    @DisplayName("команда без решётки — это ошибка формата")
    void dontRunMalformedCommand() {
        assertThat(
            "malformed command cannot explain the expected format",
            assertThrows(
                IllegalArgumentException.class,
                () -> new Command("ru.sprbut.m03.model.Order").invocation()
            ).getMessage(),
            containsString("Класс#метод")
        );
    }

    @Test
    @DisplayName("непреобразуемый аргумент отбивается с указанием типа")
    void dontConvertMalformedArgument() {
        assertThat(
            "malformed argument cannot name the target type",
            assertThrows(
                IllegalArgumentException.class,
                () -> new Argument("сто", int.class).value()
            ).getMessage(),
            containsString("int")
        );
    }

    @Test
    @DisplayName("null нельзя передать в примитивный параметр")
    void dontPassNullToPrimitive() {
        assertThat(
            "null cannot be rejected for a primitive parameter",
            assertThrows(
                IllegalArgumentException.class,
                () -> new Argument("null", int.class).value()
            ).getMessage(),
            containsString("примитивный")
        );
    }

    @Test
    @DisplayName("enum читается без учёта регистра")
    void convertsEnumIgnoringCase() {
        assertThat(
            "enum argument cannot be read case insensitively",
            new Argument("days", java.time.temporal.ChronoUnit.class).value(),
            equalTo(java.time.temporal.ChronoUnit.DAYS)
        );
    }

    @Test
    @DisplayName("исключение вызванного метода приходит развёрнутым")
    void unwrapsCheckedException() {
        assertThrows(
            CommandFailed.class,
            () -> new Command("ru.sprbut.m03.model.Order(A-1,1000)#pay(1)").invocation()
        );
    }
}
