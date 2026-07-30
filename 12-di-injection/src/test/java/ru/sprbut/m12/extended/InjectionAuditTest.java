package ru.sprbut.m12.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m12.injection.ConstructorInjected;
import ru.sprbut.m12.injection.FieldInjected;
import ru.sprbut.m12.injection.SetterInjected;
import ru.sprbut.m12.jakarta.JakartaInjected;
import ru.sprbut.m12.locator.ServiceLocatorDemo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@DisplayName("Расширенный пример: аудит точек внедрения")
final class InjectionAuditTest {

    @Test
    @DisplayName("внедрение через конструктор распознаётся основным способом")
    void detectsConstructorInjection() {
        assertThat(
            "constructor injection cannot be detected as the primary style",
            new InjectionAudit(ConstructorInjected.class).report().primary(),
            equalTo(Style.CONSTRUCTOR)
        );
    }

    @Test
    @DisplayName("конструкторное внедрение замечаний не вызывает")
    void acceptsConstructorInjection() {
        assertThat(
            "constructor injection cannot pass without warnings",
            new InjectionAudit(ConstructorInjected.class).report().warnings(),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("внедрение в поле распознаётся")
    void detectsFieldInjection() {
        assertThat(
            "field injection cannot be detected",
            new InjectionAudit(FieldInjected.class).report().styles(),
            hasItem(Style.FIELD)
        );
    }

    @Test
    @DisplayName("внедрение в поле получает замечание про невозможность new")
    void warnsAboutFieldInjection() {
        assertThat(
            "field injection cannot be warned about",
            new InjectionAudit(FieldInjected.class).report().warnings().toString(),
            containsString("нельзя собрать обычным new")
        );
    }

    @Test
    @DisplayName("внедрение через сеттер распознаётся отдельным способом")
    void detectsSetterInjection() {
        assertThat(
            "setter injection cannot be detected",
            new InjectionAudit(SetterInjected.class).report().styles(),
            hasItem(Style.SETTER)
        );
    }

    @Test
    @DisplayName("Service Locator ловится по ApplicationContextAware")
    void detectsServiceLocator() {
        assertThat(
            "service locator cannot be detected",
            new InjectionAudit(ServiceLocatorDemo.class).report().styles(),
            hasItem(Style.SERVICE_LOCATOR)
        );
    }

    @Test
    @DisplayName("jakarta-аннотации учитываются наравне со спринговыми")
    void treatsJakartaAlike() {
        assertThat(
            "jakarta annotations cannot count as injection points",
            new InjectionAudit(JakartaInjected.class).report().styles(),
            hasItem(Style.CONSTRUCTOR)
        );
    }

    @Test
    @DisplayName("конструкторное внедрение делает класс тестируемым без контейнера")
    void reportsConstructorInjectionTestable() {
        assertThat(
            "constructor injection cannot make the class testable",
            new InjectionAudit(ConstructorInjected.class).report().testable(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("внедрение в поле тестируемость отнимает")
    void reportsFieldInjectionUntestable() {
        assertThat(
            "field injection cannot take the testability away",
            new InjectionAudit(FieldInjected.class).report().testable(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("Service Locator тоже делает класс непроверяемым без контейнера")
    void reportsLocatorUntestable() {
        assertThat(
            "service locator cannot take the testability away",
            new InjectionAudit(ServiceLocatorDemo.class).report().testable(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("final-поля отличают полноценное конструкторное внедрение")
    void detectsImmutability() {
        assertThat(
            "final fields cannot mark a proper constructor injection",
            new InjectionAudit(ConstructorInjected.class).report().immutable(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("отчёт по классу с внедрением в поле чистым не бывает")
    void dontCallFieldInjectionClean() {
        assertThat(
            "field injection report cannot avoid being clean",
            new InjectionAudit(FieldInjected.class).report().clean(),
            equalTo(false)
        );
    }
}
