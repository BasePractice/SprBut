package ru.sprbut.m04;

import java.io.File;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("Слайд 31: setAccessible и JPMS")
final class DeepAccessTest {

    @SuppressWarnings("unused")
    private static final class Ours {

        private String secret = "доступно";
    }

    @Test
    @DisplayName("свой класс открыт всегда — ограничения JPMS про границы модулей")
    void opensOwnClass() {
        assertThat(
            "own class cannot be open for deep reflection",
            new DeepAccess(Ours.class, "secret").attempt().succeeded(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("открытый флагом --add-opens пакет java.util доступен")
    void opensExplicitlyOpenedPackage() {
        assertThat(
            "package opened by the flag cannot be accessible",
            new DeepAccess(ArrayList.class, "elementData").attempt().succeeded(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("java.util открыт для нас, потому что флаг задан в surefire")
    void reportsOpenPackage() {
        assertThat(
            "opened package cannot report itself as open",
            new ModuleAccess(ArrayList.class).open(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("java.io экспортирован, но не открыт — это разные вещи")
    void separatesExportsFromOpens() {
        assertThat(
            "exported package cannot stay closed for deep reflection",
            new ModuleAccess(File.class).exported(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("не открытый пакет глубокую рефлексию не разрешает")
    void dontOpenClosedPackage() {
        assertThat(
            "closed package cannot refuse deep reflection",
            new ModuleAccess(File.class).open(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("класс из java.base принадлежит именованному модулю")
    void namesJdkModule() {
        assertThat(
            "JDK class cannot name its module",
            new ModuleAccess(ArrayList.class).moduleName(),
            equalTo("java.base")
        );
    }

    @Test
    @DisplayName("код с classpath живёт в безымянном модуле")
    void reportsUnnamedModule() {
        assertThat(
            "classpath code cannot live in the unnamed module",
            new ModuleAccess(DeepAccessTest.class).moduleName(),
            nullValue()
        );
    }

    @Test
    @DisplayName("несуществующее поле отличается от закрытого доступа")
    void separatesMissingFieldFromDeniedAccess() {
        assertThat(
            "missing field cannot be told apart from denied access",
            new DeepAccess(Ours.class, "nope").attempt().failure(),
            equalTo("NoSuchFieldException")
        );
    }
}
