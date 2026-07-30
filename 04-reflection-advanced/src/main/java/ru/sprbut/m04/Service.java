package ru.sprbut.m04;

/**
 * Носитель точек внедрения: конструктор и метод-сеттер, оба с параметрами
 * помеченными и непомеченными.
 * <p>
 * Нужен, чтобы у {@link Parameters} было что разбирать, — и заодно показывает,
 * что контейнеру безразлично, где находится точка внедрения.
 */
@SuppressWarnings("unused")
public final class Service {

    private final String name;

    private final int retries;

    public Service(@Injected("appName") String name, @Injected int retries, boolean debug) {
        this.name = name;
        this.retries = retries;
    }

    /**
     * Настройка через метод — вторая разновидность точки внедрения.
     */
    public void configure(@Injected("timeout") long millis, String label) {
        // параметры разбираются рефлексией, тело здесь роли не играет
    }
}
