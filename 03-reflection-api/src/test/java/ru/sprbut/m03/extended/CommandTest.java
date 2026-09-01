/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m03.extended;

import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Расширенный пример: строка превращается в вызов метода.
 * @since 1.0
 */
@DisplayName("Расширенный пример: строка превращается в вызов метода")
final class CommandTest {

    @Test
    @DisplayName("класс загружается по имени, конструктор подбирается по аргументам")
    void buildsTargetFromCommand() {
        MatcherAssert.assertThat(
            "command cannot build the target object",
            new Command("ru.sprbut.m03.model.Order(A-1,100)#getId").invocation().result(),
            Matchers.equalTo("A-1")
        );
    }

    @Test
    @DisplayName("строковые аргументы приводятся к типам параметров")
    void convertsArgumentsByParameterTypes() {
        MatcherAssert.assertThat(
            "string arguments cannot be converted by parameter types",
            new Command("ru.sprbut.m03.model.Order(A-1,100)#getTotal").invocation().result(),
            Matchers.equalTo(new BigDecimal("100"))
        );
    }

    @Test
    @DisplayName("varargs собирается в массив через Array.newInstance")
    void packsVarargsIntoArray() {
        MatcherAssert.assertThat(
            "varargs tail cannot be packed into an array",
            new Command("ru.sprbut.m03.model.Order(A-1,0)#addLines(10,20)").invocation().result(),
            Matchers.equalTo(new BigDecimal("30"))
        );
    }

    @Test
    @DisplayName("часть с конструктором можно опустить")
    void usesNoArgConstructorByDefault() {
        MatcherAssert.assertThat(
            "command without constructor args cannot use the no-arg one",
            new Command("ru.sprbut.m03.model.Order#isPaid").invocation().result(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("отчёт называет выбранный конструктор")
    void reportsChosenConstructor() {
        MatcherAssert.assertThat(
            "report cannot name the chosen constructor",
            new Command("ru.sprbut.m03.model.Order(A-1,100)#getId").invocation().constructor(),
            Matchers.equalTo("Order(String, BigDecimal)")
        );
    }

    @Test
    @DisplayName("отчёт называет подпись выбранного метода")
    void reportsMethodSignature() {
        MatcherAssert.assertThat(
            "report cannot name the chosen method signature",
            new Command("ru.sprbut.m03.model.Order(A-1,100)#getId").invocation().signature(),
            Matchers.equalTo("String getId()")
        );
    }

    @Test
    @DisplayName("приватный метод тоже вызывается — setAccessible снимает проверку")
    void invokesPrivateMethod() {
        MatcherAssert.assertThat(
            "private method cannot be invoked by the command",
            String.valueOf(
                new Command("ru.sprbut.m03.model.Order(A-1,100)#internalTag").invocation().result()
            ),
            Matchers.containsString("A-1")
        );
    }

    @Test
    @DisplayName("неизвестный класс отбивается на разборе, а не на вызове")
    void dontRunUnknownClass() {
        MatcherAssert.assertThat(
            "unknown class cannot be reported by name",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Command("ru.sprbut.Nope#toString").invocation()
            ).getMessage(),
            Matchers.containsString("ru.sprbut.Nope")
        );
    }

    @Test
    @DisplayName("неизвестный метод отбивается с указанием числа аргументов")
    void dontRunUnknownMethod() {
        MatcherAssert.assertThat(
            "unknown method cannot be reported with its arity",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Command("ru.sprbut.m03.model.Order#nope(1)").invocation()
            ).getMessage(),
            Matchers.containsString("nope")
        );
    }

    @Test
    @DisplayName("команда без решётки — это ошибка формата")
    void dontRunMalformedCommand() {
        MatcherAssert.assertThat(
            "malformed command cannot explain the expected format",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Command("ru.sprbut.m03.model.Order").invocation()
            ).getMessage(),
            Matchers.containsString("Класс#метод")
        );
    }

    @Test
    @DisplayName("непреобразуемый аргумент отбивается с указанием типа")
    void dontConvertMalformedArgument() {
        MatcherAssert.assertThat(
            "malformed argument cannot name the target type",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Argument("сто", int.class).value()
            ).getMessage(),
            Matchers.containsString("int")
        );
    }

    @Test
    @DisplayName("null нельзя передать в примитивный параметр")
    void dontPassNullToPrimitive() {
        MatcherAssert.assertThat(
            "null cannot be rejected for a primitive parameter",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Argument("null", int.class).value()
            ).getMessage(),
            Matchers.containsString("примитивный")
        );
    }

    @Test
    @DisplayName("enum читается без учёта регистра")
    void convertsEnumIgnoringCase() {
        MatcherAssert.assertThat(
            "enum argument cannot be read case insensitively",
            new Argument("days", java.time.temporal.ChronoUnit.class).value(),
            Matchers.equalTo(java.time.temporal.ChronoUnit.DAYS)
        );
    }

    @Test
    @DisplayName("исключение вызванного метода приходит развёрнутым")
    void unwrapsCheckedException() {
        Assertions.assertThrows(
            CommandFailed.class,
            () -> new Command("ru.sprbut.m03.model.Order(A-1,1000)#pay(1)").invocation()
        );
    }
}
