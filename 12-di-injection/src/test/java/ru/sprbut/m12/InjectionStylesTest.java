/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m12;

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
     * Контекст.
     * @since 1.0
     */
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        this.context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    @AfterEach
    void tearDown() {
        this.context.close();
    }

    @Nested
/**
 * Все три способа работают одинаково.
 * @since 1.0
 */
    @DisplayName("Все три способа работают одинаково")
    final class Equivalence {

        @Test
        @DisplayName("Результат не зависит от способа внедрения")
        void resultsAreIdentical() {
            final BigDecimal net = new BigDecimal("100");
            MatcherAssert.assertThat(
                "cannot verify that results are identical",
                context.getBean(ConstructorInjected.class).total(net, true),
                Matchers.comparesEqualTo(new BigDecimal("108.00"))
            );
            MatcherAssert.assertThat(
                "cannot verify that results are identical",
                context.getBean(SetterInjected.class).total(net, true),
                Matchers.comparesEqualTo(new BigDecimal("108.00"))
            );
            MatcherAssert.assertThat(
                "cannot verify that results are identical",
                context.getBean(FieldInjected.class).total(net, true),
                Matchers.comparesEqualTo(new BigDecimal("108.00"))
            );
        }
    }

    @Nested
/**
 * Слайд 92: почему конструктор предпочтителен.
 * @since 1.0
 */
    @DisplayName("Слайд 92: почему конструктор предпочтителен")
    final class ConstructorWins {

        @Test
        @DisplayName("Класс собирается обычным new — тест не требует контейнера вовсе")
        void testableWithoutContainer() {
            final ConstructorInjected service = new ConstructorInjected(
                new TaxService(), new DiscountService()
            );
            MatcherAssert.assertThat(
                "cannot verify that testable without container",
                service.total(new BigDecimal("100"), false),
                Matchers.comparesEqualTo(new BigDecimal("120.00"))
            );
        }

        @Test
        @DisplayName("Поля final — объект неизменяем по построению")
        void fieldsAreFinal() {
            MatcherAssert.assertThat(
                "cannot verify that fields are final",
                Arrays.stream(ConstructorInjected.class.getDeclaredFields()).allMatch(f -> Modifier.isFinal(f.getModifiers())),
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
                ConstructorInjected.class.getDeclaredConstructors()[0] .isAnnotationPresent(Autowired.class),
                Matchers.equalTo(false)
            );
            MatcherAssert.assertThat(
                "cannot verify that autowired is optional on single constructor",
                context.getBean(ConstructorInjected.class),
                Matchers.notNullValue()
            );
        }
    }

    @Nested
/**
 * Слайд 93: внедрение в поле мешает тестам без контейнера.
 * @since 1.0
 */
    @DisplayName("Слайд 93: внедрение в поле мешает тестам без контейнера")
    final class FieldInjectionProblems {

        @Test
        @DisplayName("Собранный руками объект падает: поля остались null")
        void newInstanceIsBroken() {
            final FieldInjected service = new FieldInjected();
            Assertions.assertThrows(NullPointerException.class, () -> service.total(new BigDecimal("100"), false));
        }

        @Test
        @DisplayName("Единственный способ подставить зависимости без контейнера — рефлексия")
        void onlyReflectionCanFillTheFields() throws Exception {
            final FieldInjected service = new FieldInjected();
            final var field = FieldInjected.class.getDeclaredField("taxService");
            field.setAccessible(true);
            field.set(service, new TaxService());
            final var discount = FieldInjected.class.getDeclaredField("discountService");
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
                Arrays.stream(FieldInjected.class.getDeclaredFields()).anyMatch(f -> Modifier.isFinal(f.getModifiers())),
                Matchers.equalTo(false)
            );
        }
    }

    @Nested
/**
 * Сеттер: единственный уместный случай — необязательная зависимость.
 * @since 1.0
 */
    @DisplayName("Сеттер: единственный уместный случай — необязательная зависимость")
    final class SetterInjection {

        @Test
        @DisplayName("@Autowired(required = false) оставляет поле пустым, если бина нет")
        void optionalDependencyMayBeAbsent() {
            try (var minimal = new AnnotationConfigApplicationContext()) {
                minimal.register(TaxService.class, SetterInjected.class);
                minimal.refresh();
                final SetterInjected service = minimal.getBean(SetterInjected.class);
                MatcherAssert.assertThat(
                    "cannot verify that optional dependency may be absent",
                    service.hasDiscountService(),
                    Matchers.equalTo(false)
                );
                MatcherAssert.assertThat(
                    "cannot verify that optional dependency may be absent",
                    service.total(new BigDecimal("100"), true),
                    Matchers.comparesEqualTo(new BigDecimal("120.00"))
                );
            }
        }

        @Test
        @DisplayName("Когда бин есть, он внедряется")
        void optionalDependencyIsInjectedWhenPresent() {
            MatcherAssert.assertThat(
                "cannot verify that optional dependency is injected when present",
                context.getBean(SetterInjected.class).hasDiscountService(),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Между new и вызовом сеттера объект невалиден")
        void objectIsInvalidBetweenNewAndSetter() {
            final SetterInjected service = new SetterInjected();
            Assertions.assertThrows(NullPointerException.class, () -> service.total(BigDecimal.TEN, false));
            service.setTaxService(new TaxService());
            MatcherAssert.assertThat(
                "cannot verify that object is invalid between new and setter",
                service.total(new BigDecimal("100"), false),
                Matchers.comparesEqualTo(new BigDecimal("120.00"))
            );
        }
    }

    @Nested
/**
 * Слайд 95: Service Locator — антипаттерн.
 * @since 1.0
 */
    @DisplayName("Слайд 95: Service Locator — антипаттерн")
    final class ServiceLocator {

        @Test
        @DisplayName("Работает, но зависимость достаётся вручную из контейнера")
        void itWorksButHidesDependencies() {
            final ServiceLocatorDemo demo = context.getBean(ServiceLocatorDemo.class);
            MatcherAssert.assertThat(
                "cannot verify that it works but hides dependencies",
                demo.total(new BigDecimal("100")),
                Matchers.comparesEqualTo(new BigDecimal("120.00"))
            );
            MatcherAssert.assertThat(
                "service locator cannot hide its dependencies from the constructor",
                ServiceLocatorDemo.class.getDeclaredConstructors()[0].getParameterCount(),
                Matchers.equalTo(0)
            );
        }

        @Test
        @DisplayName("Вне контейнера класс неработоспособен в принципе")
        void uselessOutsideTheContainer() {
            final ServiceLocatorDemo standalone = new ServiceLocatorDemo();
            MatcherAssert.assertThat(
                "cannot verify that useless outside the container",
                standalone.worksWithoutContainer(),
                Matchers.equalTo(false)
            );
            Assertions.assertThrows(NullPointerException.class, () -> standalone.total(BigDecimal.TEN));
        }

        @Test
        @DisplayName("Ошибка «нет бина» вылезает при вызове метода, а не при старте")
        void errorsSurfaceLate() {
            final ServiceLocatorDemo demo = context.getBean(ServiceLocatorDemo.class);
            // контекст поднялся успешно, хотя такого бина нет
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> demo.lookup("несуществующийБин"));
        }
    }

    @Nested
/**
 * Слайд 96: jakarta-аннотации.
 * @since 1.0
 */
    @DisplayName("Слайд 96: jakarta-аннотации")
    final class Jakarta {

        @Test
        @DisplayName("@Inject работает как @Autowired, @Resource — как @Resource")
        void jakartaAnnotationsAreSupported() {
            final JakartaInjected service = context.getBean(JakartaInjected.class);
            MatcherAssert.assertThat(
                "cannot verify that jakarta annotations are supported",
                service.total(new BigDecimal("100"), true),
                Matchers.comparesEqualTo(new BigDecimal("108.00"))
            );
        }

        @Test
        @DisplayName("@Named задаёт имя бина")
        void namedSetsTheBeanName() {
            MatcherAssert.assertThat(
                "cannot verify that named sets the bean name",
                context.containsBean("jakartaService"),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "cannot verify that named sets the bean name",
                context.getBean("jakartaService"),
                Matchers.instanceOf(JakartaInjected.class)
            );
        }

        @Test
        @DisplayName("Класс переносим: он не зависит от аннотаций Spring")
        void classIsPortable() {
            final boolean usesSpringAnnotations = Arrays.stream(
                JakartaInjected.class.getAnnotations()
            )
                    .anyMatch(
                        a -> a.annotationType().getName().startsWith("org.springframework")
                    );
            MatcherAssert.assertThat(
                "portable class cannot stay free of Spring annotations",
                usesSpringAnnotations,
                Matchers.equalTo(false)
            );
        }
    }
}
