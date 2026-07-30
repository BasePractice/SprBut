package ru.sprbut.m06.targets;

import java.util.List;

/**
 * Носитель аннотаций новых целей: на переменной типа, на самом типе поля
 * и внутри аргумента дженерика.
 * <p>
 * Три случая отличаются тем, <b>откуда</b> аннотация читается, и это главное,
 * что стоит вынести из слайдов 48–49.
 */
@SuppressWarnings("unused")
public class Holder<@Comparablish T> {

    /**
     * Аннотация стоит на самом типе поля.
     */
    public @NonNull String direct;

    /**
     * А здесь — на аргументе дженерика, что доступно только {@code TYPE_USE}.
     */
    public List<@NonNull String> insideGenerics;

    /**
     * Поле без аннотаций — отрицательный контроль.
     */
    public List<String> plain;
}
