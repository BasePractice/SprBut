package ru.sprbut.m09;

/**
 * Сервис <b>без интерфейса</b> — JDK-прокси такой класс проксировать не умеет.
 * <p>
 * Именно этот случай заставляет Spring переключаться на CGLIB: обёртка строится
 * подклассом, а не реализацией контракта. Отсюда же и требование, чтобы класс
 * не был {@code final}.
 */
public class AuditService {

    /**
     * Записывает событие.
     */
    public String record(String event) {
        return "записано: " + event;
    }

    /**
     * Число записей.
     */
    public int size() {
        return 0;
    }
}
