package ru.sprbut.m04;

/**
 * Интерфейс, вокруг которого строится прокси.
 * <p>
 * {@link #greetTwice} объявлен здесь не для удобства: он вызывает соседний
 * метод через {@code this}, и на нём видно ограничение self-invocation.
 */
public interface Greeter {

    /**
     * Приветствие по имени.
     */
    String greet(String name);

    /**
     * Длина текста.
     */
    int length(String text);

    /**
     * Двойное приветствие — реализовано через вызов соседнего метода.
     */
    default String greetTwice(String name) {
        return greet(name) + " " + greet(name);
    }
}
