package ru.sprbut.m09;

import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Записывает факт вызова, после чего управление уходит в оригинальный метод.
 * <p>
 * {@code @This} даёт ссылку на сам сгенерированный объект — по его имени видно,
 * что это подкласс, а не исходный класс.
 */
public final class LoggingInterceptor {

    private LoggingInterceptor() {
    }

    /**
     * Перехват перед вызовом оригинала.
     */
    public static void before(@This Object self) {
        Intercepted.add("вызов у " + self.getClass().getSimpleName());
    }
}
