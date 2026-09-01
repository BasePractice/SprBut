/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m10.lombok;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m10.lombok.samples.Account;
import ru.sprbut.m10.lombok.samples.Fluent;
import ru.sprbut.m10.lombok.samples.Order;
import ru.sprbut.m10.lombok.samples.Partial;
import ru.sprbut.m10.lombok.samples.Service;

/**
 * Слайд 79–82: Lombok дописывает класс на этапе компиляции.
 * @since 1.0
 */
@DisplayName("Слайд 79–82: Lombok дописывает класс на этапе компиляции")
final class GeneratedTest {

    @Test
    @DisplayName("@Data порождает геттеры и сеттеры, которых нет в исходниках")
    void generatesAccessors() {
        MatcherAssert.assertThat(
            "@Data cannot generate the accessors",
            new Generated(CustomerEntity.class).methods(),
            Matchers.hasItems("getId", "setId")
        );
    }

    @Test
    @DisplayName("@Data порождает equals, hashCode и toString")
    void generatesObjectMethods() {
        MatcherAssert.assertThat(
            "@Data cannot generate the Object methods",
            new Generated(CustomerEntity.class).methods(),
            Matchers.hasItems("equals", "hashCode", "toString")
        );
    }

    @Test
    @DisplayName("@Value делает все поля финальными")
    void makesFieldsFinal() {
        MatcherAssert.assertThat(
            "@Value cannot make every field final",
            new Generated(CustomerDto.class).immutable(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("у неизменяемого класса сеттеров нет вовсе")
    void dontGenerateSettersForValue() {
        MatcherAssert.assertThat(
            "@Value cannot avoid generating setters",
            new Generated(CustomerDto.class).mutable(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("класс с @Data подчиняется соглашению JavaBeans")
    void staysJavaBean() {
        MatcherAssert.assertThat(
            "@Data class cannot satisfy the JavaBeans convention",
            new Generated(CustomerEntity.class).javaBean(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("уровень доступа сеттера задаётся аннотацией")
    void appliesAccessLevel() {
        MatcherAssert.assertThat(
            "access level cannot reach the generated setter",
            new Generated(Partial.class).access("setVisible"),
            Matchers.equalTo("protected")
        );
    }

    @Test
    @DisplayName("@Getter(NONE) отключает геттер для одного поля")
    void skipsDisabledGetter() {
        MatcherAssert.assertThat(
            "disabled getter cannot stay ungenerated",
            new Generated(Partial.class).methods(),
            Matchers.not(Matchers.hasItem("getHidden"))
        );
    }

    @Test
    @DisplayName("@RequiredArgsConstructor берёт только final-поля")
    void buildsConstructorFromFinalFields() {
        MatcherAssert.assertThat(
            "required args constructor cannot take only the final fields",
            new Generated(Service.class).constructors(),
            Matchers.contains(2)
        );
    }

    @Test
    @DisplayName("@EqualsAndHashCode(of = id) сравнивает только по идентификатору")
    void comparesByIdOnly() {
        MatcherAssert.assertThat(
            "equality cannot be limited to the identifier",
            new Account("A-1", "ivanov", "секрет"),
            Matchers.equalTo(new Account("A-1", "другой-логин", "другой-пароль"))
        );
    }

    @Test
    @DisplayName("@ToString(exclude) не пускает пароль в логи")
    void hidesExcludedField() {
        MatcherAssert.assertThat(
            "excluded field cannot stay out of toString",
            new Account("A-1", "ivanov", "секрет").toString(),
            Matchers.not(Matchers.containsString("секрет"))
        );
    }

    @Test
    @DisplayName("@Singular наполняет коллекцию по одному элементу")
    void fillsCollectionOneByOne() {
        MatcherAssert.assertThat(
            "singular builder cannot add items one by one",
            Order.builder().number("O-1").item("хлеб").item("молоко").build().getItems(),
            Matchers.contains("хлеб", "молоко")
        );
    }

    @Test
    @DisplayName("@Accessors(fluent) лишает класс статуса JavaBean")
    void breaksJavaBeanConvention() {
        MatcherAssert.assertThat(
            "fluent accessors cannot break the JavaBeans convention",
            new Generated(Fluent.class).javaBean(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("fluent-аксессоры работают, просто называются иначе")
    void keepsFluentAccessorsWorking() {
        MatcherAssert.assertThat(
            "fluent accessor cannot work under its own name",
            new Fluent().name("тест").name(),
            Matchers.equalTo("тест")
        );
    }
}
