/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m25.scopes;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Слайд «Типичные ошибки»: prototype внутри singleton.
 * @since 1.0
 */
@DisplayName("Слайд «Типичные ошибки»: prototype внутри singleton")
final class PlainScopeConfigTest {

    @Test
    @DisplayName("prototype, внедрённый в singleton, застывает на первом экземпляре")
    void dontRenewPrototypeInsideSingleton() {
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(PlainScopeConfig.class)
        ) {
            final Gate gate = context.getBean(Gate.class);
            MatcherAssert.assertThat(
                "prototype injected once cannot keep handing out the same number",
                gate.admit(),
                Matchers.equalTo(gate.admit())
            );
        }
    }

    @Test
    @DisplayName("сам контейнер отдаёт новый prototype на каждый запрос")
    void renewsPrototypeOnEachLookup() {
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(PlainScopeConfig.class)
        ) {
            MatcherAssert.assertThat(
                "direct lookup cannot produce a fresh prototype instance",
                context.getBean(Ticket.class).number(),
                Matchers.not(Matchers.equalTo(context.getBean(Ticket.class).number()))
            );
        }
    }

    @Test
    @DisplayName("proxyMode = TARGET_CLASS возвращает prototype его смысл")
    void renewsPrototypeThroughProxy() {
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(ProxiedScopeConfig.class)
        ) {
            final Gate gate = context.getBean(Gate.class);
            MatcherAssert.assertThat(
                "scoped proxy cannot fetch a new ticket per call",
                gate.admit(),
                Matchers.not(Matchers.equalTo(gate.admit()))
            );
        }
    }
}
