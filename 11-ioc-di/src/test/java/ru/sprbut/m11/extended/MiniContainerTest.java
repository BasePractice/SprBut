/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m11.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Расширенный пример: собственный IoC-контейнер.
 * @since 1.0
 */
@DisplayName("Расширенный пример: собственный IoC-контейнер")
final class MiniContainerTest {

    private MiniContainer healthyContainer() {
        return new MiniContainer(
                Components.OrderFacade.class,
                Components.OrderService.class,
                Components.Repository.class,
                Components.Clock.class);
    }

    /**
     * Контейнер собирает граф.
     * @since 1.0
     */
    @Nested
    @DisplayName("Контейнер собирает граф")
    final class Wiring {

        @Test
        @DisplayName("Зависимости подбираются по типу и внедряются через конструктор")
        void injectsByType() {
            final Components.OrderFacade facade = MiniContainerTest.this.healthyContainer().getBean(Components.OrderFacade.class);
            MatcherAssert.assertThat(
                "container cannot wire the graph by type",
                facade.checkout("книга"),
                Matchers.equalTo("2026-07-30 книга")
            );
        }

        @Test
        @DisplayName("Бины — синглтоны: один и тот же экземпляр везде")
        void beansAreSingletons() {
            final MiniContainer container = MiniContainerTest.this.healthyContainer();
            MatcherAssert.assertThat(
                "container cannot keep beans singleton",
                container.getBean(Components.Repository.class),
                Matchers.sameInstance(container.getBean(Components.OrderService.class).repository())
            );
        }

        @Test
        @DisplayName("Порядок создания вычисляется из графа: сначала зависимости")
        void creationOrderFollowsTheGraph() {
            final MiniContainer container = MiniContainerTest.this.healthyContainer();
            container.getBean(Components.OrderFacade.class);
            MatcherAssert.assertThat(
                "creation order cannot follow the dependency graph",
                container.creationOrder(),
                Matchers.contains("repository", "clock", "orders", "orderFacade")
            );
        }

        @Test
        @DisplayName("Пока бин не запрошен, он не создан — ленивость по умолчанию")
        void beansAreCreatedOnDemand() {
            final MiniContainer container = MiniContainerTest.this.healthyContainer();
            MatcherAssert.assertThat(
                "unrequested bean cannot stay uncreated",
                container.isCreated("repository"),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("после запроса бин создан")
        void createsOnRequest() {
            final MiniContainer container = MiniContainerTest.this.healthyContainer();
            container.getBean(Components.Repository.class);
            MatcherAssert.assertThat(
                "requested bean cannot be created",
                container.isCreated("repository"),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("refresh() создаёт все бины сразу — как Spring поступает с синглтонами")
        void refreshCreatesEverything() {
            final MiniContainer container = MiniContainerTest.this.healthyContainer().refresh();
            MatcherAssert.assertThat(
                "refresh cannot create every singleton at once",
                container.beanNames(),
                Matchers.containsInAnyOrder("orderFacade", "orders", "repository", "clock")
            );
        }

        @Test
        @DisplayName("Имя бина берётся из аннотации, иначе — из имени класса")
        void resolvesBeanNames() {
            final MiniContainer container = MiniContainerTest.this.healthyContainer();
            MatcherAssert.assertThat(
                "annotation cannot define the bean name",
                container.beanNames(),
                Matchers.hasItem("orders")
            );
        }

        @Test
        @DisplayName("по заданному имени находится нужный бин")
        void findsBeanByName() {
            MatcherAssert.assertThat(
                "named bean cannot be found by its name",
                MiniContainerTest.this.healthyContainer().getBean("orders"),
                Matchers.instanceOf(Components.OrderService.class)
            );
        }

        @Test
        @DisplayName("Поиск по интерфейсу находит реализацию")
        void findsByInterface() {
            final MiniContainer container = new MiniContainer(Components.CardPayment.class);
            MatcherAssert.assertThat(
                "interface lookup cannot find the implementation",
                container.getBean(Components.Payment.class).kind(),
                Matchers.equalTo("card")
            );
        }
    }

    /**
     * Ошибки, которые повторяет настоящий Spring.
     * @since 1.0
     */
    @Nested
    @DisplayName("Ошибки, которые повторяет настоящий Spring")
    final class Failures {

        @Test
        @DisplayName("Зависимости нет в контейнере — аналог NoSuchBeanDefinitionException")
        void missingDependency() {
            final MiniContainer container = new MiniContainer(Components.NeedsUnmanaged.class);
            MatcherAssert.assertThat(
                "missing dependency cannot be named in the failure",
                Assertions.assertThrows(
                    MiniContainer.NoSuchBeanException.class,
                    () -> container.getBean(Components.NeedsUnmanaged.class)
                ).getMessage(),
                Matchers.containsString("UnmanagedDependency")
            );
        }

        @Test
        @DisplayName("Кандидатов слишком много — аналог NoUniqueBeanDefinitionException")
        void ambiguousDependency() {
            final MiniContainer container = new MiniContainer(
                Components.CardPayment.class, Components.CashPayment.class
            );
            MatcherAssert.assertThat(
                "ambiguous candidates cannot be listed in the failure",
                Assertions.assertThrows(
                    MiniContainer.NoUniqueBeanException.class,
                    () -> container.getBean(Components.Payment.class)
                ).getMessage(),
                Matchers.containsString("cardPayment")
            );
        }

        @Test
        @DisplayName("Цикл через конструкторы — аналог BeanCurrentlyInCreationException")
        void circularDependency() {
            final MiniContainer container = new MiniContainer(
                Components.AlphaService.class, Components.BetaService.class
            );
            MatcherAssert.assertThat(
                "circular dependency cannot name both beans",
                Assertions.assertThrows(
                    MiniContainer.CircularDependencyException.class,
                    () -> container.getBean(Components.AlphaService.class)
                ).getMessage(),
                Matchers.containsString("BetaService")
            );
        }

        @Test
        @DisplayName("Несколько конструкторов без явного указания — контейнер отказывается гадать")
        void ambiguousConstructor() {
            final MiniContainer container = new MiniContainer(
                Components.TwoConstructors.class, Components.Repository.class,
                Components.Clock.class
            );
            MatcherAssert.assertThat(
                "ambiguous constructor cannot make the container refuse to guess",
                Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> container.getBean(Components.TwoConstructors.class)
                ).getMessage(),
                Matchers.containsString("несколько конструкторов")
            );
        }

        @Test
        @DisplayName("Неуправляемый класс зарегистрировать нельзя")
        void unmanagedClassCannotBeRegistered() {
            MatcherAssert.assertThat(
                "unmanaged class cannot be rejected at registration",
                Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new MiniContainer(Components.UnmanagedDependency.class)
                ).getMessage(),
                Matchers.containsString("@MiniComponent")
            );
        }

        @Test
        @DisplayName("Дубликат имени бина обнаруживается сразу при регистрации")
        void duplicateBeanName() {
            MatcherAssert.assertThat(
                "duplicate bean name cannot be caught at registration",
                Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> new MiniContainer(Components.Repository.class, Components.Repository.class)
                ).getMessage(),
                Matchers.containsString("уже занято")
            );
        }

        @Test
        @DisplayName("Неизвестное имя бина — понятная ошибка со списком известных")
        void unknownBeanName() {
            MatcherAssert.assertThat(
                "unknown bean name cannot be reported with the known ones",
                Assertions.assertThrows(
                    MiniContainer.NoSuchBeanException.class,
                    () -> MiniContainerTest.this.healthyContainer().getBean("нет-такого")
                ).getMessage(),
                Matchers.containsString("известны")
            );
        }
    }

    /**
     * Единственный конструктор — правило Spring.
     * @since 1.0
     */
    @Nested
    @DisplayName("Единственный конструктор — правило Spring")
    final class ConstructorSelection {

        @Test
        @DisplayName("Один конструктор используется без всяких аннотаций")
        void singleConstructorNeedsNoAnnotation() {
            MatcherAssert.assertThat(
                "single constructor cannot be used without an annotation",
                MiniContainer.selectConstructor(Components.OrderService.class).getParameterCount(),
                Matchers.equalTo(2)
            );
        }

        @Test
        @DisplayName("Имя бина по умолчанию — имя класса с маленькой буквы")
        void defaultNameIsDecapitalizedClassName() {
            MatcherAssert.assertThat(
                "default bean name cannot be the decapitalised class name",
                MiniContainer.defaultName(Components.Repository.class),
                Matchers.equalTo("repository")
            );
        }
    }
}
