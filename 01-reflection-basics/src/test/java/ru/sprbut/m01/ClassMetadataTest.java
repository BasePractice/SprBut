/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m01;

import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

/**
 * Слайды 3–5: метаданные класса в runtime.
 * @since 1.0
 */
@DisplayName("Слайды 3–5: метаданные класса в runtime")
final class ClassMetadataTest {

    @Test
    @DisplayName("полное имя включает пакет")
    void readsFullName() {
        MatcherAssert.assertThat(
            "class metadata cannot report the fully qualified name",
            new ClassMetadata(new Account("ACC-1", "Иванов", new BigDecimal("100.00"))).fullName(),
            Matchers.equalTo("ru.sprbut.m01.model.Account")
        );
    }

    @Test
    @DisplayName("короткое имя пакет не включает")
    void readsSimpleName() {
        MatcherAssert.assertThat(
            "class metadata cannot report the simple name",
            new ClassMetadata(new Account("ACC-2", "Петров", BigDecimal.ONE)).simpleName(),
            Matchers.equalTo("Account")
        );
    }

    @Test
    @DisplayName("имя пакета читается отдельно от имени класса")
    void readsPackageName() {
        MatcherAssert.assertThat(
            "class metadata cannot report the package name",
            new ClassMetadata(Account.class).packageName(),
            Matchers.equalTo("ru.sprbut.m01.model")
        );
    }

    @Test
    @DisplayName("иерархия наследования доходит до Object")
    void walksHierarchyUpToObject() {
        MatcherAssert.assertThat(
            "hierarchy cannot reach Object",
            new ClassMetadata(Account.class).hierarchy(),
            Matchers.contains("Account", "Object")
        );
    }

    @Test
    @DisplayName("иерархия перечисляет все промежуточные классы")
    void listsIntermediateClasses() {
        MatcherAssert.assertThat(
            "hierarchy cannot list the intermediate superclasses",
            new ClassMetadata(ArrayList.class).hierarchy(),
            Matchers.contains("ArrayList", "AbstractList", "AbstractCollection", "Object")
        );
    }

    @Test
    @DisplayName("getInterfaces() отдаёт только напрямую реализованные интерфейсы")
    void readsDirectInterfaces() {
        MatcherAssert.assertThat(
            "direct interfaces cannot be listed",
            new ClassMetadata(ArrayList.class).interfaces(),
            Matchers.hasItems("List", "RandomAccess", "Cloneable")
        );
    }

    @Test
    @DisplayName("класс без интерфейсов даёт пустой список, а не null")
    void reportsNoInterfaces() {
        MatcherAssert.assertThat(
            "class without interfaces cannot yield an empty list",
            new ClassMetadata(Account.class).interfaces(),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("Class — единственный объект на загруженный класс")
    @SuppressWarnings("PMD.InstantiationToGetClass")
    void keepsSingleClassObject() {
        MatcherAssert.assertThat(
            "two ways of getting Class cannot lead to the same object",
            Account.class,
            Matchers.sameInstance(new Account("ACC-3", "Сидоров", BigDecimal.TEN).getClass())
        );
    }

    @Test
    @DisplayName("обычный класс инстанцировать можно")
    void detectsInstantiableClass() {
        MatcherAssert.assertThat(
            "plain class cannot be recognised as instantiable",
            new ClassMetadata(Account.class).instantiable(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("интерфейс инстанцировать нельзя")
    void dontInstantiateInterface() {
        MatcherAssert.assertThat(
            "interface cannot be rejected as non instantiable",
            new ClassMetadata(List.class).instantiable(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("абстрактный класс инстанцировать нельзя")
    void dontInstantiateAbstractClass() {
        MatcherAssert.assertThat(
            "abstract class cannot be rejected as non instantiable",
            new ClassMetadata(AbstractList.class).instantiable(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("примитив инстанцировать нельзя")
    void dontInstantiatePrimitive() {
        MatcherAssert.assertThat(
            "primitive cannot be rejected as non instantiable",
            new ClassMetadata(int.class).instantiable(),
            Matchers.equalTo(false)
        );
    }
}
