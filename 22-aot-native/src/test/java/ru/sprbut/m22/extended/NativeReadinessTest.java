package ru.sprbut.m22.extended;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import ru.sprbut.m22.hints.PluginHints;
import ru.sprbut.m22.reflection.CsvPlugin;
import ru.sprbut.m22.reflection.JsonPlugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Расширенный пример: аудит готовности к native image")
final class NativeReadinessTest {

    @Test
    @DisplayName("объявленный в подсказках класс переживёт сборку образа")
    void coversRegisteredPlugin() {
        RuntimeHints hints = new RuntimeHints();
        new PluginHints().registerHints(hints, getClass().getClassLoader());
        assertThat(
            "registered plugin cannot be recognised as native-ready",
            new NativeReadiness(hints).covers(CsvPlugin.class),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("забытый класс виден аудиту, хотя на JVM работает исправно")
    void dontCoverForgottenPlugin() {
        RuntimeHints hints = new RuntimeHints();
        new PluginHints().registerHints(hints, getClass().getClassLoader());
        assertThat(
            "forgotten plugin cannot stay invisible to the audit",
            new NativeReadiness(hints).covers(JsonPlugin.class),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("аудит перечисляет именно те классы, которых не хватает")
    void listsMissingPlugins() {
        RuntimeHints hints = new RuntimeHints();
        new PluginHints().registerHints(hints, getClass().getClassLoader());
        assertThat(
            "audit cannot name the plugin missing from the hints",
            new NativeReadiness(hints).gaps(List.of(CsvPlugin.class, JsonPlugin.class)),
            contains(JsonPlugin.class.getName())
        );
    }

    @Test
    @DisplayName("полностью покрытый набор классов не даёт замечаний")
    void reportsNoGapsWhenAllRegistered() {
        RuntimeHints hints = new RuntimeHints();
        new PluginHints().registerHints(hints, getClass().getClassLoader());
        assertThat(
            "fully registered set cannot pass the audit without remarks",
            new NativeReadiness(hints).gaps(List.of(CsvPlugin.class)),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("пустые подсказки означают, что рефлексии в образе не будет вовсе")
    void treatsEmptyHintsAsNoReflection() {
        assertThat(
            "empty hints cannot mean that no reflection survives",
            new NativeReadiness(new RuntimeHints()).covers(CsvPlugin.class),
            equalTo(false)
        );
    }
}
