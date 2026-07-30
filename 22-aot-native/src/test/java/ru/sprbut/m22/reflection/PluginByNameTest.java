package ru.sprbut.m22.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд «AOT»: загрузка класса по имени работает на JVM и ломается в native")
final class PluginByNameTest {

    @Test
    @DisplayName("classloader находит класс, на который нет ни одной ссылки в коде")
    void loadsPluginByClassName() {
        assertThat(
            "classloader cannot instantiate a plugin named by string",
            new PluginByName("ru.sprbut.m22.reflection.CsvPlugin").plugin().name(),
            equalTo("csv")
        );
    }

    @Test
    @DisplayName("незарегистрированное расширение на JVM работает точно так же")
    void loadsUnregisteredPluginOnJvm() {
        assertThat(
            "plugin missing from hints cannot work on a plain JVM",
            new PluginByName("ru.sprbut.m22.reflection.JsonPlugin").plugin().name(),
            equalTo("json")
        );
    }

    @Test
    @DisplayName("отсутствующий класс превращается в понятную ошибку, а не в ClassNotFoundException из глубины")
    void failsWithContextOnUnknownClass() {
        assertThat(
            "unknown plugin cannot report its own name in the failure",
            assertThrows(
                IllegalStateException.class,
                () -> new PluginByName("ru.sprbut.m22.reflection.XmlPlugin").plugin()
            ).getMessage(),
            containsString("XmlPlugin")
        );
    }
}
