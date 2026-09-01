/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m02.modern;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 19: record и Builder против избыточности и мутабельности.
 * @since 1.0
 */
@DisplayName("Слайд 19: record и Builder против избыточности и мутабельности")
final class ModernAlternativesTest {

    @Test
    @DisplayName("record даёт equals бесплатно — сравнение по значению, а не по ссылке")
    void comparesRecordsByValue() {
        MatcherAssert.assertThat(
            "record cannot compare by value",
            new CustomerRecord("C-1", "Иван", "Иванов", 42, true),
            Matchers.equalTo(new CustomerRecord("C-1", "Иван", "Иванов", 42, true))
        );
    }

    @Test
    @DisplayName("hashCode согласован с equals — иначе record был бы бесполезен в Set")
    void keepsHashCodeConsistent() {
        MatcherAssert.assertThat(
            "record hash code cannot match an equal instance",
            new CustomerRecord("C-1", "Иван", "Иванов", 42, true).hashCode(),
            Matchers.equalTo(new CustomerRecord("C-1", "Иван", "Иванов", 42, true).hashCode())
        );
    }

    @Test
    @DisplayName("toString печатает состояние, а не адрес объекта")
    void printsState() {
        MatcherAssert.assertThat(
            "record toString cannot print the state",
            new CustomerRecord("C-1", "Иван", "Иванов", 42, true).toString(),
            Matchers.containsString("C-1")
        );
    }

    @Test
    @DisplayName("компактный конструктор валидирует объект один раз — при создании")
    void validatesOnConstruction() {
        MatcherAssert.assertThat(
            "compact constructor cannot reject an empty id",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new CustomerRecord("", "Иван", "Иванов", 42, false)
            ).getMessage(),
            Matchers.containsString("id обязателен")
        );
    }

    @Test
    @DisplayName("отрицательный возраст тоже отбивается на входе")
    void rejectsNegativeAge() {
        MatcherAssert.assertThat(
            "compact constructor cannot reject a negative age",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new CustomerRecord("C-1", "Иван", "Иванов", -1, false)
            ).getMessage(),
            Matchers.containsString("отрицательным")
        );
    }

    @Test
    @DisplayName("изменение неизменяемого объекта даёт новый объект")
    void createsNewInstanceOnChange() {
        final CustomerRecord original = new CustomerRecord("C-1", "Иван", "Иванов", 42, false);
        MatcherAssert.assertThat(
            "with-method cannot produce a new instance",
            original.withVip(true),
            Matchers.not(Matchers.sameInstance(original))
        );
    }

    @Test
    @DisplayName("исходный объект при этом не меняется")
    void keepsOriginalUntouched() {
        final CustomerRecord original = new CustomerRecord("C-1", "Иван", "Иванов", 42, false);
        original.withVip(true);
        MatcherAssert.assertThat(
            "original record cannot stay untouched",
            original.vip(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("Builder собирает объект по частям")
    void buildsInParts() {
        MatcherAssert.assertThat(
            "builder cannot assemble the object part by part",
            ImmutableCustomer.builder()
                .id("C-1").firstName("Иван").lastName("Иванов").age(42).vip(true)
                .tags(List.of("gold"))
                .build()
                .getId(),
            Matchers.equalTo("C-1")
        );
    }

    @Test
    @DisplayName("собранная коллекция неизменяема")
    void keepsCollectionImmutable() {
        Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> ImmutableCustomer.builder().id("C-1").tags(List.of("gold")).build()
                .getTags().add("hack")
        );
    }

    @Test
    @DisplayName("защитная копия: правка исходного списка после build() ничего не меняет")
    void copiesCollectionDefensively() {
        final List<String> mutable = new ArrayList<>(List.of("gold"));
        final ImmutableCustomer customer =
            ImmutableCustomer.builder().id("C-1").tags(mutable).build();
        mutable.add("platinum");
        MatcherAssert.assertThat(
            "builder cannot copy the collection defensively",
            customer.getTags(),
            Matchers.contains("gold")
        );
    }

    @Test
    @DisplayName("валидация внутри builder срабатывает до сборки объекта")
    void validatesEagerly() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> ImmutableCustomer.builder().age(-5)
        );
    }

    @Test
    @DisplayName("обязательное поле проверяется в момент build()")
    void demandsMandatoryField() {
        MatcherAssert.assertThat(
            "builder cannot demand the mandatory field",
            Assertions.assertThrows(
                NullPointerException.class,
                () -> ImmutableCustomer.builder().build()
            ).getMessage(),
            Matchers.containsString("id обязателен")
        );
    }

    @Test
    @DisplayName("toBuilder() переносит состояние и даёт изменить одно поле")
    void copiesStateIntoBuilder() {
        MatcherAssert.assertThat(
            "toBuilder cannot carry the state over",
            ImmutableCustomer.builder()
                .id("C-1").firstName("Иван").age(42).tags(List.of("gold")).build()
                .toBuilder().vip(true).build()
                .getFirstName(),
            Matchers.equalTo("Иван")
        );
    }

    @Test
    @DisplayName("исходный объект после toBuilder() остаётся прежним")
    void keepsSourceUnchanged() {
        final ImmutableCustomer original =
            ImmutableCustomer.builder().id("C-1").firstName("Иван").age(42).build();
        original.toBuilder().vip(true).build();
        MatcherAssert.assertThat(
            "source object cannot survive toBuilder unchanged",
            original.isVip(),
            Matchers.equalTo(false)
        );
    }
}
