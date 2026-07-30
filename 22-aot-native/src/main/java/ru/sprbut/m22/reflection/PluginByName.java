package ru.sprbut.m22.reflection;

/**
 * Слайд «AOT и native image»: динамическая загрузка класса по строке.
 * <p>
 * Три рефлективных действия подряд — {@code Class.forName}, поиск конструктора,
 * {@code newInstance} — и каждое требует отдельной подсказки для native image:
 * доступ к самому классу и доступ к его публичным конструкторам.
 * <p>
 * Пока приложение живёт на JVM, платить за это не нужно: classloader найдёт
 * что угодно в classpath. Цена появляется ровно в момент перехода на native.
 */
public final class PluginByName {

    private final String type;

    public PluginByName(String type) {
        this.type = type;
    }

    /**
     * Экземпляр расширения, созданный рефлексией по имени класса.
     */
    public Plugin plugin() {
        try {
            return Class.forName(this.type)
                .asSubclass(Plugin.class)
                .getDeclaredConstructor()
                .newInstance();
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException(
                "Расширение " + this.type + " недоступно в этом рантайме", failure
            );
        }
    }
}
