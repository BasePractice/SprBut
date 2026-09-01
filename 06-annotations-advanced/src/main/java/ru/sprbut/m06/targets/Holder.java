/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// поля публичны и названы по смыслу: предмет разговора — откуда читается
// аннотация, а не инкапсуляция
// @checkstyle VisibilityModifierCheck disable
// @checkstyle MemberNameCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.targets;

import java.util.List;

/**
 * Носитель аннотаций новых целей: на переменной типа, на самом типе поля
 * и внутри аргумента дженерика.
 *
 * <p>Три случая отличаются тем, <b>откуда</b> аннотация читается, и это главное,
 * что стоит вынести из слайдов 48–49.</p>
 *
 * @param <T> Параметр типа
 * @since 1.0
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

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Holder() {
        // нечего инициализировать
    }
}
