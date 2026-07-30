package ru.sprbut.m22.reflection;

/**
 * Расширение, которое забыли объявить в {@link ru.sprbut.m22.hints.PluginHints}.
 * <p>
 * На JVM неотличимо от {@link CsvPlugin} — тесты зелёные, приложение работает.
 * Разница проявится только в native image и только в рантайме:
 * {@code ClassNotFoundException} на классе, который лежит в исходниках.
 */
public final class JsonPlugin implements Plugin {

    @Override
    public String name() {
        return "json";
    }
}
