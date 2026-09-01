/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m22.extended;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import ru.sprbut.m22.hints.PluginHints;
import ru.sprbut.m22.reflection.CsvPlugin;
import ru.sprbut.m22.reflection.JsonPlugin;

/**
 * Тесты аудита готовности приложения к сборке в native image.
 * @since 1.0
 */
@DisplayName("Расширенный пример: аудит готовности к native image")
final class NativeReadinessTest {

    @Test
    @DisplayName("объявленный в подсказках класс переживёт сборку образа")
    void coversRegisteredPlugin() {
        MatcherAssert.assertThat(
            "registered plugin cannot be recognised as native-ready",
            new NativeReadiness(NativeReadinessTest.registered()).covers(CsvPlugin.class),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("забытый класс виден аудиту, хотя на JVM работает исправно")
    void dontCoverForgottenPlugin() {
        MatcherAssert.assertThat(
            "forgotten plugin cannot stay invisible to the audit",
            new NativeReadiness(NativeReadinessTest.registered()).covers(JsonPlugin.class),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("аудит перечисляет именно те классы, которых не хватает")
    void listsMissingPlugins() {
        MatcherAssert.assertThat(
            "audit cannot name the plugin missing from the hints",
            new NativeReadiness(NativeReadinessTest.registered())
                .gaps(List.of(CsvPlugin.class, JsonPlugin.class)),
            Matchers.contains(JsonPlugin.class.getName())
        );
    }

    @Test
    @DisplayName("полностью покрытый набор классов не даёт замечаний")
    void reportsNoGapsWhenAllRegistered() {
        MatcherAssert.assertThat(
            "fully registered set cannot pass the audit without remarks",
            new NativeReadiness(NativeReadinessTest.registered()).gaps(List.of(CsvPlugin.class)),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("пустые подсказки означают, что рефлексии в образе не будет вовсе")
    void treatsEmptyHintsAsNoReflection() {
        MatcherAssert.assertThat(
            "empty hints cannot mean that no reflection survives",
            new NativeReadiness(new RuntimeHints()).covers(CsvPlugin.class),
            Matchers.equalTo(false)
        );
    }

    private static RuntimeHints registered() {
        final RuntimeHints hints = new RuntimeHints();
        new PluginHints().registerHints(
            hints, Thread.currentThread().getContextClassLoader()
        );
        return hints;
    }
}
