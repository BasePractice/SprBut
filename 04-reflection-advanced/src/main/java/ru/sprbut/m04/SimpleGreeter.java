package ru.sprbut.m04;

/**
 * Реальная цель, вокруг которой строится прокси. Ничего не знает ни о прокси,
 * ни о логировании — в этом и смысл: класс не меняется.
 */
public final class SimpleGreeter implements Greeter {

    @Override
    public String greet(String name) {
        return "Привет, " + name;
    }

    @Override
    public int length(String text) {
        return text.length();
    }
}
