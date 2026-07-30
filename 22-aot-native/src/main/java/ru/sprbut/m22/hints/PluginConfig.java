package ru.sprbut.m22.hints;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import ru.sprbut.m22.reflection.CsvPlugin;
import ru.sprbut.m22.reflection.Plugin;
import ru.sprbut.m22.reflection.PluginByName;

/**
 * Конфигурация, которая честно объявляет свою рефлексию.
 * <p>
 * {@code @ImportRuntimeHints} — единственная строчка разницы между приложением,
 * которое соберётся в native image, и приложением, которое соберётся,
 * запустится и упадёт на первом же плагине.
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(PluginHints.class)
public final class PluginConfig {

    @Bean
    public Plugin plugin() {
        return new PluginByName(CsvPlugin.class.getName()).plugin();
    }
}
