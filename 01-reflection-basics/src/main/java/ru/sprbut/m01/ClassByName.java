package ru.sprbut.m01;

/**
 * Третий способ получить {@code Class}: загрузка по строковому имени.
 * <p>
 * На этом стоит чтение конфигураций, где имя класса лежит в текстовом файле,
 * и весь механизм плагинов. Здесь же прячется главная проблема native image:
 * связь через строку не видна ни компилятору, ни сборщику образа
 * (см. [модуль 22](../../../../../../22-aot-native)).
 */
public final class ClassByName {

    private final String name;

    public ClassByName(String name) {
        this.name = name;
    }

    /**
     * Загруженный класс.
     *
     * @throws ClassNotFoundException если классу неоткуда взяться в classpath
     */
    public Class<?> type() throws ClassNotFoundException {
        return Class.forName(this.name);
    }
}
