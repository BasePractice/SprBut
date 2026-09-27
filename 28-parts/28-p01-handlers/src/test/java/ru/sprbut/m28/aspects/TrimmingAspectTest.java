/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.aspects;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.AopConfigException;
import ru.sprbut.m28.web.SearchController;
import ru.sprbut.m28.web.UserController;

/**
 * Аспект, обрезающий строковые аргументы.
 * @since 1.0
 */
final class TrimmingAspectTest {

    @Test
    @DisplayName("метод получает аргументы уже обрезанными")
    void trimsArguments() {
        final AspectJProxyFactory factory = new AspectJProxyFactory(new SearchController());
        factory.addAspect(new TrimmingAspect());
        MatcherAssert.assertThat(
            "аргументы дошли до метода с пробелами",
            factory.<SearchController>getProxy().search("  книги ", " Казань  "),
            Matchers.is("[книги] в [Казань]")
        );
    }

    @Test
    @DisplayName("final контроллер под аспект не завернуть")
    void cannotProxyFinalController() {
        final AspectJProxyFactory factory = new AspectJProxyFactory(new UserController());
        factory.setProxyTargetClass(true);
        factory.addAspect(new TrimmingAspect());
        Assertions.assertThrows(
            AopConfigException.class,
            factory::getProxy,
            "прокси для final класса вдруг построился"
        );
    }
}
