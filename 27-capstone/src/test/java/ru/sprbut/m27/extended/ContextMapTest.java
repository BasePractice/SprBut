/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m27.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Расширенный итог: приложение объясняет само себя.
 * @since 1.0
 */
@SpringBootTest
@DisplayName("Расширенный итог: приложение объясняет само себя")
final class ContextMapTest {

    /**
     * Отображение.
     */
    @Autowired
    private ContextMap map;

    @Test
    @DisplayName("карта показывает прикладные бины и не показывает инфраструктуру Spring")
    void dontListSpringInfrastructure() {
        MatcherAssert.assertThat(
            "context map cannot skip the Spring internals",
            this.map.cards().stream().map(BeanCard::type).toList(),
            Matchers.not(
                Matchers.hasItem(
                    "org.springframework.context.annotation.ConfigurationClassPostProcessor"
                )
            )
        );
    }

    @Test
    @DisplayName("карта находит сервис среди прикладных бинов")
    void findsApplicationService() {
        MatcherAssert.assertThat(
            "context map cannot list the task service",
            this.map.cards().stream().map(BeanCard::name).toList(),
            Matchers.hasItem("taskService")
        );
    }

    @Test
    @DisplayName("за прокси видно настоящий класс, а не сгенерированный")
    void unwrapsProxyToRealClass() {
        MatcherAssert.assertThat(
            "context map cannot see the real class behind the proxy",
            this.map.cards().stream()
                .filter(card -> "taskService".equals(card.name()))
                .map(BeanCard::type)
                .findFirst()
                .orElse("отсутствует"),
            Matchers.equalTo("ru.sprbut.m27.service.TaskService")
        );
    }

    @Test
    @DisplayName("сервис с интерфейсом обёрнут JDK-прокси")
    void reportsJdkProxyForInterfacedBean() {
        MatcherAssert.assertThat(
            "service with an interface cannot be wrapped by a JDK proxy",
            this.map.proxy("taskService"),
            Matchers.equalTo("jdk")
        );
    }

    @Test
    @DisplayName("бин без перехватываемых аннотаций не проксируется вовсе")
    void dontProxyPlainBean() {
        MatcherAssert.assertThat(
            "plain bean cannot stay unproxied",
            this.map.proxied("auditTrail"),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("рефлексия находит помеченные аудитом операции по их именам из аннотации")
    void findsAuditedOperations() {
        MatcherAssert.assertThat(
            "reflection cannot find the audited operation names",
            this.map.cards().stream()
                .filter(card -> "taskService".equals(card.name()))
                .flatMap(card -> card.audited().stream())
                .toList(),
            Matchers.hasItem("task.finish")
        );
    }

    @Test
    @DisplayName("область видимости по умолчанию — singleton")
    void reportsSingletonScope() {
        MatcherAssert.assertThat(
            "default scope cannot be reported as singleton",
            this.map.cards().stream()
                .filter(card -> "auditTrail".equals(card.name()))
                .map(BeanCard::scope)
                .findFirst()
                .orElse("отсутствует"),
            Matchers.equalTo("singleton")
        );
    }
}
