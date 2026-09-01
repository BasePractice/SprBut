/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m09;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m09.model.UserEntity;

/**
 * Слайды 73–75: три механизма, один результат.
 * @since 1.0
 */
@DisplayName("Слайды 73–75: три механизма, один результат")
final class MappersTest {

    @Test
    @DisplayName("рефлексия выводит правила маппинга из метаданных, а не из кода")
    void discoversRulesItself() {
        MatcherAssert.assertThat(
            "reflective mapper cannot discover the properties itself",
            new ReflectiveMapper().discoveredProperties(),
            Matchers.greaterThan(0)
        );
    }

    @Test
    @DisplayName("рефлексивный маппинг даёт правильный результат")
    void mapsReflectively() {
        MatcherAssert.assertThat(
            "reflective mapping cannot copy the first name",
            new ReflectiveMapper().toDto(entity()).getFirstName(),
            Matchers.equalTo("Иван")
        );
    }

    @Test
    @DisplayName("null на входе — null на выходе")
    void handlesNull() {
        MatcherAssert.assertThat(
            "null entity cannot yield a null dto",
            new ReflectiveMapper().toDto(null),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("сгенерированный код даёт тот же результат, что и рефлексия")
    void agreesWithGeneratedStyle() {
        MatcherAssert.assertThat(
            "generated mapper cannot agree with the reflective one",
            new GeneratedStyleMapper().toDto(entity()),
            Matchers.equalTo(new ReflectiveMapper().toDto(entity()))
        );
    }

    @Test
    @DisplayName("класс байткодного маппера собирается в runtime — в исходниках его нет")
    void generatesClassAtRuntime() {
        MatcherAssert.assertThat(
            "bytecode mapper class cannot be generated at runtime",
            new BytecodeMapper().mapper().getClass().getName(),
            Matchers.containsString("bytebuddy")
        );
    }

    @Test
    @DisplayName("сгенерированный класс — полноценная реализация интерфейса")
    void generatedClassWorks() {
        MatcherAssert.assertThat(
            "generated class cannot implement the contract",
            new BytecodeMapper().mapper().toDto(entity()),
            Matchers.equalTo(new GeneratedStyleMapper().toDto(entity()))
        );
    }

    @Test
    @DisplayName("каждый вызов даёт новый загруженный класс")
    void loadsNewClassEachTime() {
        MatcherAssert.assertThat(
            "each generation cannot produce its own loaded class",
            new BytecodeMapper().mapper().getClass(),
            Matchers.not(Matchers.equalTo(new BytecodeMapper().mapper().getClass()))
        );
    }

    @Test
    @DisplayName("байткод проксирует класс без интерфейса — то, чего не умеет JDK-прокси")
    void proxiesClassWithoutInterface() {
        MatcherAssert.assertThat(
            "class without an interface cannot be proxied by a subclass",
            new BytecodeMapper().proxied().getClass().getSuperclass(),
            Matchers.equalTo(AuditService.class)
        );
    }

    @Test
    @DisplayName("цель интерфейсов не реализует вовсе")
    void keepsTargetInterfaceFree() {
        MatcherAssert.assertThat(
            "target cannot stay free of interfaces",
            AuditService.class.getInterfaces().length,
            Matchers.equalTo(0)
        );
    }

    @Test
    @DisplayName("перехват срабатывает, а оригинальный метод всё равно вызывается")
    void interceptsAndDelegates() {
        final AuditService proxied = new BytecodeMapper().proxied();
        proxied.record("вход");
        MatcherAssert.assertThat(
            "interceptor cannot record the call",
            new Intercepted().entries(),
            Matchers.hasItem(Matchers.containsString("Enhanced"))
        );
    }

    @Test
    @DisplayName("оригинальный метод возвращает своё значение, несмотря на перехват")
    void keepsOriginalResult() {
        MatcherAssert.assertThat(
            "original method cannot keep its own result",
            new BytecodeMapper().proxied().record("вход"),
            Matchers.equalTo("записано: вход")
        );
    }

    @Test
    @DisplayName("каждая реализация называет свою стратегию сама")
    void namesItsOwnStrategy() {
        MatcherAssert.assertThat(
            "reflective mapper cannot name its own strategy",
            new ReflectiveMapper().strategy(),
            Matchers.containsString("reflection")
        );
    }

    @Test
    @DisplayName("копия совпадает с оригиналом по всем полям")
    void copiesEveryField() {
        MatcherAssert.assertThat(
            "generated mapping cannot copy the boolean field",
            new GeneratedStyleMapper().toDto(entity()).isActive(),
            Matchers.equalTo(true)
        );
    }

    private static UserEntity entity() {
        return new UserEntity("U-1", "Иван", "Иванов", 42, true);
    }
}
