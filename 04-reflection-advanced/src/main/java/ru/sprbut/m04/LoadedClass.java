package ru.sprbut.m04;

/**
 * Слайд 30: «Class: X.class, obj.getClass(), Class.forName()».
 * <p>
 * Класс, загруженный по строковому имени, — единственный из трёх способов,
 * который работает с именем, известным только в runtime: из конфига,
 * из имени бина, из заголовка запроса.
 * <p>
 * Разница между двумя методами здесь не косметическая. {@code forName(name)}
 * ещё и <b>инициализирует</b> класс, выполняя статические блоки;
 * {@link #dormant} загружает его без побочных эффектов. Сканеры classpath
 * пользуются вторым: им нужны метаданные, а не последствия.
 */
public final class LoadedClass {

    private final String name;

    public LoadedClass(String name) {
        this.name = name;
    }

    /**
     * Загруженный и инициализированный класс.
     */
    public Class<?> type() throws ClassNotFoundException {
        return Class.forName(this.name);
    }

    /**
     * Загруженный, но не инициализированный класс: статические блоки не выполнены.
     */
    public Class<?> dormant(ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(this.name, false, loader);
    }
}
