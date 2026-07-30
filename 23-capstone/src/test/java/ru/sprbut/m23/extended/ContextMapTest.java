package ru.sprbut.m23.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@DisplayName("Расширенный итог: приложение объясняет само себя")
final class ContextMapTest {

    @Autowired
    private ContextMap map;

    @Test
    @DisplayName("карта показывает прикладные бины и не показывает инфраструктуру Spring")
    void dontListSpringInfrastructure() {
        assertThat(
            "context map cannot skip the Spring internals",
            this.map.cards().stream().map(BeanCard::type).toList(),
            not(hasItem("org.springframework.context.annotation.ConfigurationClassPostProcessor"))
        );
    }

    @Test
    @DisplayName("карта находит сервис среди прикладных бинов")
    void findsApplicationService() {
        assertThat(
            "context map cannot list the task service",
            this.map.cards().stream().map(BeanCard::name).toList(),
            hasItem("taskService")
        );
    }

    @Test
    @DisplayName("за прокси видно настоящий класс, а не сгенерированный")
    void unwrapsProxyToRealClass() {
        assertThat(
            "context map cannot see the real class behind the proxy",
            this.map.cards().stream()
                .filter(card -> "taskService".equals(card.name()))
                .map(BeanCard::type)
                .findFirst()
                .orElse("отсутствует"),
            equalTo("ru.sprbut.m23.service.TaskService")
        );
    }

    @Test
    @DisplayName("сервис с интерфейсом обёрнут JDK-прокси")
    void reportsJdkProxyForInterfacedBean() {
        assertThat(
            "service with an interface cannot be wrapped by a JDK proxy",
            this.map.proxy("taskService"),
            equalTo("jdk")
        );
    }

    @Test
    @DisplayName("бин без перехватываемых аннотаций не проксируется вовсе")
    void dontProxyPlainBean() {
        assertThat(
            "plain bean cannot stay unproxied",
            this.map.proxied("auditTrail"),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("рефлексия находит помеченные аудитом операции по их именам из аннотации")
    void findsAuditedOperations() {
        assertThat(
            "reflection cannot find the audited operation names",
            this.map.cards().stream()
                .filter(card -> "taskService".equals(card.name()))
                .flatMap(card -> card.audited().stream())
                .toList(),
            hasItem("task.finish")
        );
    }

    @Test
    @DisplayName("область видимости по умолчанию — singleton")
    void reportsSingletonScope() {
        assertThat(
            "default scope cannot be reported as singleton",
            this.map.cards().stream()
                .filter(card -> "auditTrail".equals(card.name()))
                .map(BeanCard::scope)
                .findFirst()
                .orElse("отсутствует"),
            equalTo("singleton")
        );
    }
}
