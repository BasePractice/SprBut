package ru.sprbut.m04;

/**
 * Слайд 30: «Class: X.class, obj.getClass(), Class.forName()».
 * <p>
 * Три способа получить один и тот же объект {@code Class} — но с разными
 * свойствами: литерал проверяется компилятором, {@code getClass()} даёт
 * <i>фактический</i> тип, а {@code forName()} работает по строке и может
 * бросить исключение в runtime.
 */
public final class ClassLoading {

    /** Флаг, который взводится при инициализации класса. */
    static boolean initialized;

    private ClassLoading() {
    }

    /** Литерал: тип известен и проверен на этапе компиляции. */
    public static Class<?> byLiteral() {
        return String.class;
    }

    /**
     * {@code getClass()} возвращает <b>фактический</b> класс объекта.
     * Для {@code Object o = "текст"} это будет {@code String}, а не {@code Object}.
     */
    public static Class<?> byInstance(Object target) {
        return target.getClass();
    }

    /**
     * {@code forName()} — единственный способ загрузить класс, имя которого
     * известно только в runtime (из конфига, из имени бина, из заголовка).
     * По умолчанию класс при этом ещё и <b>инициализируется</b>.
     */
    public static Class<?> byName(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    /**
     * Вариант с {@code initialize = false}: класс загружается, но статические
     * инициализаторы не выполняются. Так делают сканеры classpath — им нужны
     * только метаданные, а побочные эффекты инициализации не нужны.
     */
    public static Class<?> byNameWithoutInit(String name, ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(name, false, loader);
    }

    /**
     * У примитива есть свой {@code Class}, и он не равен классу обёртки.
     * {@code int.class == Integer.TYPE != Integer.class} — частый источник багов
     * при подборе методов по типам параметров.
     */
    public static boolean primitiveClassDiffersFromWrapper() {
        return int.class != Integer.class && int.class == Integer.TYPE;
    }

    /**
     * Имя класса массива в JVM-нотации: {@code [Ljava.lang.String;}.
     * {@code getSimpleName()} при этом покажет привычное {@code String[]}.
     */
    public static String jvmArrayName() {
        return String[].class.getName();
    }

    /**
     * ClassLoader — тот, кто на самом деле загрузил класс. У классов из
     * {@code java.base} загрузчик равен {@code null} (bootstrap).
     */
    public static ClassLoader loaderOf(Class<?> type) {
        return type.getClassLoader();
    }

    /** Класс со статическим блоком — для демонстрации инициализации. */
    public static class WithStaticInit {
        static {
            initialized = true;
        }

        public static final String NAME = "инициализирован";
    }
}
