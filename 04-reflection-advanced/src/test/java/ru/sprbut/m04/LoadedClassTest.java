/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 30: три способа получить Class.
 * @since 1.0
 */
@DisplayName("Слайд 30: три способа получить Class")
final class LoadedClassTest {

    @Test
    @DisplayName("литерал и getClass() дают один и тот же объект")
    void keepsSingleClassObject() {
        MatcherAssert.assertThat(
            "literal and getClass cannot lead to the same object",
            "текст".getClass(),
            Matchers.sameInstance(String.class)
        );
    }

    @Test
    @DisplayName("getClass() отдаёт фактический тип, а не объявленный")
    void readsActualType() {
        final Object declared = "текст";
        MatcherAssert.assertThat(
            "getClass cannot report the actual type",
            declared.getClass(),
            Matchers.sameInstance(String.class)
        );
    }

    @Test
    @DisplayName("forName загружает класс по строке, известной только в runtime")
    void loadsByName() throws ClassNotFoundException {
        MatcherAssert.assertThat(
            "class named by string cannot be loaded",
            new LoadedClass("java.util.ArrayList").type(),
            Matchers.sameInstance(java.util.ArrayList.class)
        );
    }

    @Test
    @DisplayName("несуществующее имя даёт ClassNotFoundException")
    void dontLoadUnknownName() {
        Assertions.assertThrows(
            ClassNotFoundException.class,
            () -> new LoadedClass("ru.sprbut.NoSuchClass").type()
        );
    }

    @Test
    @DisplayName("загрузка без инициализации даёт тот же Class")
    void loadsWithoutInitialization() throws ClassNotFoundException {
        MatcherAssert.assertThat(
            "dormant loading cannot yield the same class",
            new LoadedClass("java.util.ArrayList").dormant(getClass().getClassLoader()),
            Matchers.sameInstance(java.util.ArrayList.class)
        );
    }

    @Test
    @DisplayName("у примитива свой Class, и он не равен классу обёртки")
    void separatesPrimitiveFromWrapper() {
        MatcherAssert.assertThat(
            "primitive class cannot differ from its wrapper",
            int.class,
            Matchers.not(Matchers.equalTo(Integer.class))
        );
    }

    @Test
    @DisplayName("Integer.TYPE — это и есть int.class")
    void equatesTypeConstantToPrimitive() {
        MatcherAssert.assertThat(
            "Integer.TYPE cannot be the primitive class itself",
            Integer.TYPE,
            Matchers.sameInstance(int.class)
        );
    }

    @Test
    @DisplayName("имя класса массива записано в JVM-нотации")
    void printsJvmArrayName() {
        MatcherAssert.assertThat(
            "array class cannot use the JVM notation",
            String[].class.getName(),
            Matchers.equalTo("[Ljava.lang.String;")
        );
    }

    @Test
    @DisplayName("у классов из java.base загрузчик равен null — это bootstrap")
    void reportsBootstrapLoader() {
        MatcherAssert.assertThat(
            "bootstrap loaded class cannot report a null loader",
            String.class.getClassLoader(),
            Matchers.nullValue()
        );
    }
}
