/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.converters;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.support.DefaultConversionService;
import ru.sprbut.m28.dto.Role;

/**
 * Фабрика конвертеров строки в перечисления.
 * @since 1.0
 */
final class LenientEnumFactoryTest {

    @Test
    @DisplayName("константа находится без учёта регистра и пробелов")
    void ignoresCase() {
        MatcherAssert.assertThat(
            "константа не найдена по имени в нижнем регистре",
            new LenientEnumFactory().getConverter(Role.class).convert(" admin "),
            Matchers.is(Role.ADMIN)
        );
    }

    @Test
    @DisplayName("встроенное преобразование регистр не прощает")
    void builtinDemandsExactName() {
        Assertions.assertThrows(
            ConversionFailedException.class,
            () -> new DefaultConversionService().convert("admin", Role.class),
            "встроенное преобразование вдруг простило регистр"
        );
    }
}
