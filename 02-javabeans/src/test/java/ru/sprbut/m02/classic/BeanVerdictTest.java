/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m02.classic;

import java.io.Serializable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.modern.CustomerRecord;
import ru.sprbut.m02.modern.ImmutableCustomer;

/**
 * Слайды 12–16: проверка соглашения JavaBeans.
 * @since 1.0
 */
@DisplayName("Слайды 12–16: проверка соглашения JavaBeans")
final class BeanVerdictTest {

    @SuppressWarnings("unused")
    private static final class NoDefaultCtor implements Serializable {

        /**
         * Имя.
         * @since 1.0
         */
        private String name;

        NoDefaultCtor(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("unused")
    public static final class SetterWithoutGetter implements Serializable {

        /**
         * Секрет.
         * @since 1.0
         */
        private String secret;

        public void setSecret(final String secret) {
            this.secret = secret;
        }
    }

    @SuppressWarnings("unused")
    public static final class NotSerializable {

        /**
         * Имя.
         */
        private String name;

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    @Test
    @DisplayName("классический бин выполняет все четыре пункта соглашения")
    void acceptsClassicBean() {
        MatcherAssert.assertThat(
            "classic bean cannot satisfy the strict convention",
            new BeanVerdict(CustomerBean.class, true).violations(),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("без публичного конструктора без параметров класс — не бин")
    void dontAcceptMissingNoArgConstructor() {
        MatcherAssert.assertThat(
            "class without a no-arg constructor cannot be rejected",
            new BeanVerdict(NoDefaultCtor.class).violations(),
            Matchers.hasItem("нет публичного конструктора без параметров")
        );
    }

    @Test
    @DisplayName("setter без парного getter нарушает соглашение")
    void dontAcceptSetterWithoutGetter() {
        MatcherAssert.assertThat(
            "setter without a getter cannot be reported",
            new BeanVerdict(SetterWithoutGetter.class).violations(),
            Matchers.hasItem("у свойства 'secret' есть setter, но нет getter")
        );
    }

    @Test
    @DisplayName("строгое соглашение требует Serializable")
    void demandsSerializableWhenStrict() {
        MatcherAssert.assertThat(
            "strict convention cannot demand Serializable",
            new BeanVerdict(NotSerializable.class, true).violations(),
            Matchers.hasItem("класс не реализует Serializable")
        );
    }

    @Test
    @DisplayName("Spring того же Serializable не требует — слайд это прямо оговаривает")
    void dontDemandSerializableForSpring() {
        MatcherAssert.assertThat(
            "Spring style convention cannot ignore Serializable",
            new BeanVerdict(NotSerializable.class).valid(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("record — не JavaBean: нет ни конструктора без параметров, ни getXxx")
    void dontAcceptRecord() {
        MatcherAssert.assertThat(
            "record cannot fail the JavaBeans convention",
            new BeanVerdict(CustomerRecord.class).violations(),
            Matchers.hasItem("нет публичного конструктора без параметров")
        );
    }

    @Test
    @DisplayName("неизменяемый класс с билдером тоже не бин")
    void dontAcceptImmutable() {
        MatcherAssert.assertThat(
            "immutable class cannot fail the constructor requirement",
            new BeanVerdict(ImmutableCustomer.class).constructible(),
            Matchers.equalTo(false)
        );
    }
}
