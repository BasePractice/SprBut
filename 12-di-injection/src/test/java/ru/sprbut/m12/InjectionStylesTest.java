/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m12;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;
import ru.sprbut.m12.injection.ConstructorInjected;
import ru.sprbut.m12.injection.FieldInjected;
import ru.sprbut.m12.injection.SetterInjected;
import ru.sprbut.m12.jakarta.JakartaInjected;
import ru.sprbut.m12.locator.ServiceLocatorDemo;

/**
 * Слайды 91–96 (СХЕМА 6): три способа внедрения рядом.
 * @since 1.0
 */
@DisplayName("Слайды 91–96 (СХЕМА 6): три способа внедрения рядом")
final class InjectionStylesTest {

    /**
     * Контекст один на весь класс: все сценарии сравнивают способы внедрения
     * в одном и том же контейнере, поднимать его заново на каждый тест значит
     * сравнивать разные контейнеры.
     * @since 1.0
     * @checkstyle ProhibitFieldsInTestClassesCheck (3 lines)
     */
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        this.context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @AfterEach
    void tearDown() {
        this.context.close();
    }

    /**
     * Все три способа работают одинаково.
     * @since 1.0
     */
    @Nested
    @DisplayName("Все три способа работают одинаково")
    final class Equivalence {

        @Test
        @DisplayName("Внедрение через конструктор даёт тот же результат")
        void constructorInjectionGivesTheResult() {
            MatcherAssert.assertThat(
                "constructor injection cannot give the same result",
                InjectionStylesTest.this.context
                    .getBean(ConstructorInjected.class)
                    .total(new BigDecimal("100"), true),
                Matchers.comparesEqualTo(new BigDecimal("108.00"))
            );
        }

        @Test
        @DisplayName("Внедрение через сеттер даёт тот же результат")
        void setterInjectionGivesTheResult() {
            MatcherAssert.assertThat(
                "setter injection cannot give the same result",
                InjectionStylesTest.this.context
                    .getBean(SetterInjected.class)
                    .total(new BigDecimal("100"), true),
                Matchers.comparesEqualTo(new BigDecimal("108.00"))
            );
        }

        @Test
        @DisplayName("Внедрение в поле даёт тот же результат")
        void fieldInjectionGivesTheResult() {
            MatcherAssert.assertThat(
                "field injection cannot give the same result",
                InjectionStylesTest.this.context
                    .getBean(FieldInjected.class)
                    .total(new BigDecimal("100"), true),
                Matchers.comparesEqualTo(new BigDecimal("108.00"))
            );
        }
    }

    /**
     * Слайд 92: почему конструктор предпочтителен.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 92: почему конструктор предпочтителен")
    final class ConstructorWins {

        @Test
        @DisplayName("Класс собирается обычным new — тест не требует контейнера вовсе")
        void testableWithoutContainer() {
            MatcherAssert.assertThat(
                "cannot verify that testable without container",
                new ConstructorInjected(new TaxService(), new DiscountService())
                    .total(new BigDecimal("100"), false),
                Matchers.comparesEqualTo(new BigDecimal("120.00"))
            );
        }

        @Test
        @DisplayName("Поля final — объект неизменяем по построению")
        void fieldsAreFinal() {
            MatcherAssert.assertThat(
                "cannot verify that fields are final",
                Arrays.stream(ConstructorInjected.class.getDeclaredFields())
                    .allMatch(field -> Modifier.isFinal(field.getModifiers())),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Зависимости обязательны: другого конструктора просто нет")
        void dependenciesAreMandatory() {
            MatcherAssert.assertThat(
                "constructor cannot make the dependencies mandatory",
                ConstructorInjected.class.getDeclaredConstructors()[0].getParameterCount(),
                Matchers.equalTo(2)
            );
        }

        @Test
        @DisplayName("@Autowired на единственном конструкторе не нужен со Spring 4.3")
        void autowiredIsOptionalOnSingleConstructor() {
            MatcherAssert.assertThat(
                "cannot verify that autowired is optional on single constructor",
                ConstructorInjected.class.getDeclaredConstructors()[0]
                    .isAnnotationPresent(Autowired.class),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("и бин всё равно собирается контейнером")
        void beanIsAssembledWithoutAutowired() {
            MatcherAssert.assertThat(
                "bean cannot be assembled without the annotation",
                InjectionStylesTest.this.context.getBean(ConstructorInjected.class),
                Matchers.notNullValue()
            );
        }
    }

    /**
     * Слайд 93: внедрение в поле мешает тестам без контейнера.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 93: внедрение в поле мешает тестам без контейнера")
    final class FieldInjectionProblems {

        @Test
        @DisplayName("Собранный руками объект падает: поля остались null")
        void newInstanceIsBroken() {
            final FieldInjected service = new FieldInjected();
            Assertions.assertThrows(
                NullPointerException.class, () -> service.total(new BigDecimal("100"), false)
            );
        }

        @Test
        @DisplayName("Единственный способ подставить зависимости без контейнера — рефлексия")
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        void onlyReflectionCanFillTheFields() throws Exception {
            final FieldInjected service = new FieldInjected();
            final Field field = FieldInjected.class.getDeclaredField("taxes");
            field.setAccessible(true);
            field.set(service, new TaxService());
            final Field discount = FieldInjected.class.getDeclaredField("discounts");
            discount.setAccessible(true);
            discount.set(service, new DiscountService());
            MatcherAssert.assertThat(
                "cannot verify that only reflection can fill the fields",
                service.total(new BigDecimal("100"), false),
                Matchers.comparesEqualTo(new BigDecimal("120.00"))
            );
        }

        @Test
        @DisplayName("Зависимости не видны в API: конструктор пустой")
        void dependenciesAreInvisible() {
            MatcherAssert.assertThat(
                "field injected class cannot hide its dependencies from the constructor",
                FieldInjected.class.getDeclaredConstructors()[0].getParameterCount(),
                Matchers.equalTo(0)
            );
        }

        @Test
        @DisplayName("Поля не могут быть final")
        void fieldsCannotBeFinal() {
            MatcherAssert.assertThat(
                "cannot verify that fields cannot be final",
                Arrays.stream(FieldInjected.class.getDeclaredFields())
                    .anyMatch(field -> Modifier.isFinal(field.getModifiers())),
                Matchers.equalTo(false)
            );
        }
    }

    /**
     * Сеттер: единственный уместный случай — необязательная зависимость.
     * @since 1.0
     */
    @Nested
    @DisplayName("Сеттер: единственный уместный случай — необязательная зависимость")
    final class SetterInjection {

        @Test
        @DisplayName("@Autowired(required = false) оставляет поле пустым, если бина нет")
        void optionalDependencyMayBeAbsent() {
            try (
                AnnotationConfigApplicationContext minimal =
                    new AnnotationConfigApplicationContext()
            ) {
                minimal.register(TaxService.class, SetterInjected.class);
                minimal.refresh();
                MatcherAssert.assertThat(
                    "cannot verify that optional dependency may be absent",
                    minimal.getBean(SetterInjected.class).hasDiscountService(),
                    Matchers.equalTo(false)
                );
            }
        }

        @Test
        @DisplayName("без необязательной зависимости объект всё равно считает")
        void worksWithoutTheOptionalDependency() {
            try (
                AnnotationConfigApplicationContext minimal =
                    new AnnotationConfigApplicationContext()
            ) {
                minimal.register(TaxService.class, SetterInjected.class);
                minimal.refresh();
                MatcherAssert.assertThat(
                    "object cannot work without the optional dependency",
                    minimal.getBean(SetterInjected.class).total(new BigDecimal("100"), true),
                    Matchers.comparesEqualTo(new BigDecimal("120.00"))
                );
            }
        }

        @Test
        @DisplayName("Когда бин есть, он внедряется")
        void optionalDependencyIsInjectedWhenPresent() {
            MatcherAssert.assertThat(
                "cannot verify that optional dependency is injected when present",
                InjectionStylesTest.this.context
                    .getBean(SetterInjected.class)
                    .hasDiscountService(),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Между new и вызовом сеттера объект невалиден")
        void objectIsInvalidBetweenNewAndSetter() {
            final SetterInjected service = new SetterInjected();
            Assertions.assertThrows(
                NullPointerException.class, () -> service.total(BigDecimal.TEN, false)
            );
        }

        @Test
        @DisplayName("после вызова сеттера объект становится рабочим")
        void objectBecomesUsableAfterTheSetter() {
            final SetterInjected service = new SetterInjected();
            service.setTaxService(new TaxService());
            MatcherAssert.assertThat(
                "object cannot become usable after the setter",
                service.total(new BigDecimal("100"), false),
                Matchers.comparesEqualTo(new BigDecimal("120.00"))
            );
        }
    }

    /**
     * Слайд 95: Service Locator — антипаттерн.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 95: Service Locator — антипаттерн")
    final class ServiceLocator {

        @Test
        @DisplayName("Работает, но зависимость достаётся вручную из контейнера")
        void itWorksButHidesDependencies() {
            MatcherAssert.assertThat(
                "cannot verify that it works but hides dependencies",
                InjectionStylesTest.this.context
                    .getBean(ServiceLocatorDemo.class)
                    .total(new BigDecimal("100")),
                Matchers.comparesEqualTo(new BigDecimal("120.00"))
            );
        }

        @Test
        @DisplayName("зависимостей не видно в конструкторе — он пустой")
        void hidesDependenciesFromTheConstructor() {
            MatcherAssert.assertThat(
                "service locator cannot hide its dependencies from the constructor",
                ServiceLocatorDemo.class.getDeclaredConstructors()[0].getParameterCount(),
                Matchers.equalTo(0)
            );
        }

        @Test
        @DisplayName("Вне контейнера класс неработоспособен в принципе")
        void uselessOutsideTheContainer() {
            MatcherAssert.assertThat(
                "cannot verify that useless outside the container",
                new ServiceLocatorDemo().worksWithoutContainer(),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("вне контейнера любой вызов падает")
        void failsOutsideTheContainer() {
            final ServiceLocatorDemo standalone = new ServiceLocatorDemo();
            Assertions.assertThrows(
                NullPointerException.class, () -> standalone.total(BigDecimal.TEN)
            );
        }

        @Test
        @DisplayName("Ошибка «нет бина» вылезает при вызове метода, а не при старте")
        void errorsSurfaceLate() {
            final ServiceLocatorDemo demo =
                InjectionStylesTest.this.context.getBean(ServiceLocatorDemo.class);
            Assertions.assertThrows(
                NoSuchBeanDefinitionException.class, () -> demo.lookup("несуществующийБин")
            );
        }
    }

    /**
     * Слайд 96: jakarta-аннотации.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 96: jakarta-аннотации")
    final class Jakarta {

        @Test
        @DisplayName("@Inject работает как @Autowired, @Resource — как @Resource")
        void jakartaAnnotationsAreSupported() {
            MatcherAssert.assertThat(
                "cannot verify that jakarta annotations are supported",
                InjectionStylesTest.this.context
                    .getBean(JakartaInjected.class)
                    .total(new BigDecimal("100"), true),
                Matchers.comparesEqualTo(new BigDecimal("108.00"))
            );
        }

        @Test
        @DisplayName("@Named задаёт имя бина")
        void namedSetsTheBeanName() {
            MatcherAssert.assertThat(
                "cannot verify that named sets the bean name",
                InjectionStylesTest.this.context.containsBean("jakartaService"),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("под этим именем лежит именно наш класс")
        void namedBeanHasTheRightType() {
            MatcherAssert.assertThat(
                "named bean cannot have the declared type",
                InjectionStylesTest.this.context.getBean("jakartaService"),
                Matchers.instanceOf(JakartaInjected.class)
            );
        }

        @Test
        @DisplayName("Класс переносим: он не зависит от аннотаций Spring")
        void classIsPortable() {
            MatcherAssert.assertThat(
                "portable class cannot stay free of Spring annotations",
                Arrays.stream(JakartaInjected.class.getAnnotations())
                    .map(marker -> marker.annotationType().getName())
                    .anyMatch(name -> name.startsWith("org.springframework")),
                Matchers.equalTo(false)
            );
        }
    }
}
