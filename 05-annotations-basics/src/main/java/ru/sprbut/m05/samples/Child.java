package ru.sprbut.m05.samples;

/**
 * Аннотация класса видна здесь благодаря {@code @Inherited},
 * а аннотация переопределённого метода — нет: методы не наследуют аннотации никогда.
 */
public class Child extends Parent {

    @Override
    public String action() {
        return "child";
    }
}
