package ru.sprbut.m22.reflection;

/**
 * Расширение, которое загружается по имени класса и никем не вызывается напрямую.
 * <p>
 * На JVM это работает всегда. В native image — только если класс объявлен
 * в {@code RuntimeHints}: иначе ни самого класса, ни его конструктора в образе нет.
 */
public final class CsvPlugin implements Plugin {

    @Override
    public String name() {
        return "csv";
    }
}
