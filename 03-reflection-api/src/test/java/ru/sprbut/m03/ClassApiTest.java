/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m03;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

/**
 * СХЕМА 1: Class — центр карты Reflection API.
 * @since 1.0
 */
@DisplayName("СХЕМА 1: Class — центр карты Reflection API")
final class ClassApiTest {

    @Test
    @DisplayName("из Class достаются поля")
    void listsFields() {
        MatcherAssert.assertThat(
            "class cannot yield its declared fields",
            new ClassApi(Order.class).fields(),
            Matchers.hasItems("id", "customer", "total")
        );
    }

    @Test
    @DisplayName("из Class достаются методы")
    void listsMethods() {
        MatcherAssert.assertThat(
            "class cannot yield its declared methods",
            new ClassApi(Order.class).methods(),
            Matchers.hasItems("getId", "addLines", "cancel")
        );
    }

    @Test
    @DisplayName("из Class достаются конструкторы")
    void countsConstructors() {
        MatcherAssert.assertThat(
            "class cannot yield its constructors",
            new ClassApi(Order.class).constructorCount(),
            Matchers.greaterThan(1)
        );
    }

    @Test
    @DisplayName("обычный класс распознаётся как class")
    void classifiesClass() {
        MatcherAssert.assertThat(
            "plain class cannot be classified",
            new TypeKind(Order.class).name(),
            Matchers.equalTo("class")
        );
    }

    @Test
    @DisplayName("аннотация — тоже интерфейс, поэтому проверять её надо раньше")
    void classifiesAnnotationBeforeInterface() {
        MatcherAssert.assertThat(
            "annotation cannot be classified before interface",
            new TypeKind(Retention.class).name(),
            Matchers.equalTo("annotation")
        );
    }

    @Test
    @DisplayName("enum — тоже класс, и его проверка тоже идёт раньше")
    void classifiesEnumBeforeClass() {
        MatcherAssert.assertThat(
            "enum cannot be classified before class",
            new TypeKind(Status.class).name(),
            Matchers.equalTo("enum")
        );
    }

    @Test
    @DisplayName("массив распознаётся отдельной категорией")
    void classifiesArray() {
        MatcherAssert.assertThat(
            "array cannot be classified",
            new TypeKind(String[].class).name(),
            Matchers.equalTo("array")
        );
    }

    @Test
    @DisplayName("getComponentType() раскрывает тип элемента массива")
    void readsArrayComponentType() {
        MatcherAssert.assertThat(
            "array cannot reveal its component type",
            new ClassApi(String[].class).componentType(),
            Matchers.equalTo(String.class)
        );
    }

    @Test
    @DisplayName("вложенный класс знает своего внешнего владельца")
    void knowsEnclosingClass() {
        MatcherAssert.assertThat(
            "nested class cannot name its owner",
            new ClassApi(Order.PaymentException.class).enclosing(),
            Matchers.equalTo(Order.class)
        );
    }

    @Test
    @DisplayName("иерархия наследования доходит до Object")
    void walksSuperChain() {
        MatcherAssert.assertThat(
            "super chain cannot reach Object",
            new ClassApi(ArrayList.class).superChain(),
            Matchers.contains("ArrayList", "AbstractList", "AbstractCollection", "Object")
        );
    }

    @Test
    @DisplayName("полный набор интерфейсов — основа подбора бина по типу")
    void collectsAllInterfaces() {
        MatcherAssert.assertThat(
            "inherited interfaces cannot be collected",
            new ClassApi(ArrayList.class).allInterfaces(),
            Matchers.hasItems("List", "Collection", "Iterable")
        );
    }

    @Test
    @DisplayName("isAssignableFrom читается «слева можно хранить справа»")
    void readsAssignabilityDirection() {
        MatcherAssert.assertThat(
            "assignability cannot be read left to right",
            new ClassApi(Number.class).canHold(Integer.class),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("обратное направление ложно — это и есть источник путаницы")
    void rejectsReversedAssignability() {
        MatcherAssert.assertThat(
            "reversed assignability cannot be false",
            new ClassApi(Integer.class).canHold(Number.class),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("у record есть отдельное API компонентов")
    void readsRecordComponents() {
        MatcherAssert.assertThat(
            "record components cannot be read",
            new ClassApi(Point.class).recordComponents(),
            Matchers.contains("x", "y")
        );
    }

    @Test
    @DisplayName("у enum читаются константы в порядке объявления")
    void readsEnumConstants() {
        MatcherAssert.assertThat(
            "enum constants cannot be read in declaration order",
            new ClassApi(Status.class).enumConstants(),
            Matchers.contains("NEW", "PAID")
        );
    }

    @Test
    @DisplayName("массив создаётся фабрикой Array.newInstance — new здесь неприменим")
    void createsArrayReflectively() {
        MatcherAssert.assertThat(
            "reflection cannot create an array of a runtime known type",
            ((Map<?, ?>[]) new ClassApi(Map.class).array(3)).length,
            Matchers.equalTo(3)
        );
    }

    @Test
    @DisplayName("не-record компонентов не имеет")
    void dontReadComponentsOfPlainClass() {
        MatcherAssert.assertThat(
            "plain class cannot report an empty component list",
            new ClassApi(Order.class).recordComponents(),
            Matchers.equalTo(List.of())
        );
    }

    /**
     * Статусы заказа — фикстура для чтения констант enum.
     * @since 1.0
     */
    private enum Status {

        /**
         * Новый заказ.
         */
        NEW,

        /**
         * Оплаченный заказ.
         */
        PAID
    }

    private record Point(int x, int y) {
    }
}
