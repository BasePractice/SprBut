/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m01;

import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

/**
 * Слайд 7: чтение и запись полей, включая private.
 * @since 1.0
 */
@DisplayName("Слайд 7: чтение и запись полей, включая private")
final class ObjectFieldTest {

    @Test
    @DisplayName("setAccessible(true) открывает чтение private-поля без геттера")
    void readsPrivateField() {
        MatcherAssert.assertThat(
            "private field cannot be read without a getter",
            new ObjectField(
                new Account("ACC-1", "Иванов", new BigDecimal("100.00")), "owner"
            ).value(),
            Matchers.equalTo("Иванов")
        );
    }

    @Test
    @DisplayName("запись в private-поле идёт в обход сеттера — так Spring внедряет @Autowired")
    void writesPrivateField() {
        final Account account = new Account("ACC-2", "Иванов", new BigDecimal("100.00"));
        new ObjectField(account, "owner").assign("Петров");
        MatcherAssert.assertThat(
            "private field cannot be written past the setter",
            account.getOwner(),
            Matchers.equalTo("Петров")
        );
    }

    @Test
    @DisplayName("рефлексия пишет даже в private final поле, у которого сеттера быть не может")
    void writesPrivateFinalField() {
        final Account account = new Account("ACC-3", "Иванов", new BigDecimal("100.00"));
        new ObjectField(account, "id").assign("ACC-999");
        MatcherAssert.assertThat(
            "private final field cannot be overwritten reflectively",
            account.getId(),
            Matchers.equalTo("ACC-999")
        );
    }

    @Test
    @DisplayName("поиск поля поднимается по иерархии наследования")
    void findsInheritedField() {
        final class Savings extends Account {
            Savings() {
                super("S-1", "Сидоров", BigDecimal.TEN);
            }
        }
        MatcherAssert.assertThat(
            "field lookup cannot climb up to the parent class",
            new ObjectField(new Savings(), "owner").declaration().getDeclaringClass(),
            Matchers.equalTo(Account.class)
        );
    }

    @Test
    @DisplayName("несуществующее поле даёт понятную ошибку, а не NoSuchFieldException из глубины")
    void failsOnUnknownField() {
        MatcherAssert.assertThat(
            "unknown field cannot be reported with its own name",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectField(
                    new Account("ACC-4", "Иванов", BigDecimal.ONE), "nope"
                ).value()
            ).getMessage(),
            Matchers.containsString("nope")
        );
    }
}
