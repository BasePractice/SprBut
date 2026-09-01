/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m12.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m12.injection.ConstructorInjected;
import ru.sprbut.m12.injection.FieldInjected;
import ru.sprbut.m12.injection.SetterInjected;
import ru.sprbut.m12.jakarta.JakartaInjected;
import ru.sprbut.m12.locator.ServiceLocatorDemo;

/**
 * Расширенный пример: аудит точек внедрения.
 * @since 1.0
 */
@DisplayName("Расширенный пример: аудит точек внедрения")
final class InjectionAuditTest {

    @Test
    @DisplayName("внедрение через конструктор распознаётся основным способом")
    void detectsConstructorInjection() {
        MatcherAssert.assertThat(
            "constructor injection cannot be detected as the primary style",
            new InjectionAudit(ConstructorInjected.class).report().primary(),
            Matchers.equalTo(Style.CONSTRUCTOR)
        );
    }

    @Test
    @DisplayName("конструкторное внедрение замечаний не вызывает")
    void acceptsConstructorInjection() {
        MatcherAssert.assertThat(
            "constructor injection cannot pass without warnings",
            new InjectionAudit(ConstructorInjected.class).report().warnings(),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("внедрение в поле распознаётся")
    void detectsFieldInjection() {
        MatcherAssert.assertThat(
            "field injection cannot be detected",
            new InjectionAudit(FieldInjected.class).report().styles(),
            Matchers.hasItem(Style.FIELD)
        );
    }

    @Test
    @DisplayName("внедрение в поле получает замечание про невозможность new")
    void warnsAboutFieldInjection() {
        MatcherAssert.assertThat(
            "field injection cannot be warned about",
            new InjectionAudit(FieldInjected.class).report().warnings().toString(),
            Matchers.containsString("нельзя собрать обычным new")
        );
    }

    @Test
    @DisplayName("внедрение через сеттер распознаётся отдельным способом")
    void detectsSetterInjection() {
        MatcherAssert.assertThat(
            "setter injection cannot be detected",
            new InjectionAudit(SetterInjected.class).report().styles(),
            Matchers.hasItem(Style.SETTER)
        );
    }

    @Test
    @DisplayName("Service Locator ловится по ApplicationContextAware")
    void detectsServiceLocator() {
        MatcherAssert.assertThat(
            "service locator cannot be detected",
            new InjectionAudit(ServiceLocatorDemo.class).report().styles(),
            Matchers.hasItem(Style.SERVICE_LOCATOR)
        );
    }

    @Test
    @DisplayName("jakarta-аннотации учитываются наравне со спринговыми")
    void treatsJakartaAlike() {
        MatcherAssert.assertThat(
            "jakarta annotations cannot count as injection points",
            new InjectionAudit(JakartaInjected.class).report().styles(),
            Matchers.hasItem(Style.CONSTRUCTOR)
        );
    }

    @Test
    @DisplayName("конструкторное внедрение делает класс тестируемым без контейнера")
    void reportsConstructorInjectionTestable() {
        MatcherAssert.assertThat(
            "constructor injection cannot make the class testable",
            new InjectionAudit(ConstructorInjected.class).report().testable(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("внедрение в поле тестируемость отнимает")
    void reportsFieldInjectionUntestable() {
        MatcherAssert.assertThat(
            "field injection cannot take the testability away",
            new InjectionAudit(FieldInjected.class).report().testable(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("Service Locator тоже делает класс непроверяемым без контейнера")
    void reportsLocatorUntestable() {
        MatcherAssert.assertThat(
            "service locator cannot take the testability away",
            new InjectionAudit(ServiceLocatorDemo.class).report().testable(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("final-поля отличают полноценное конструкторное внедрение")
    void detectsImmutability() {
        MatcherAssert.assertThat(
            "final fields cannot mark a proper constructor injection",
            new InjectionAudit(ConstructorInjected.class).report().immutable(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("отчёт по классу с внедрением в поле чистым не бывает")
    void dontCallFieldInjectionClean() {
        MatcherAssert.assertThat(
            "field injection report cannot avoid being clean",
            new InjectionAudit(FieldInjected.class).report().clean(),
            Matchers.equalTo(false)
        );
    }
}
