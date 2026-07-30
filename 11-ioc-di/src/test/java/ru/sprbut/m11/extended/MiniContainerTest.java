package ru.sprbut.m11.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Расширенный пример: собственный IoC-контейнер")
final class MiniContainerTest {

    private MiniContainer healthyContainer() {
        return new MiniContainer(
                Components.OrderFacade.class,
                Components.OrderService.class,
                Components.Repository.class,
                Components.Clock.class);
    }

    @Nested
    @DisplayName("Контейнер собирает граф")
    class Wiring {

        @Test
        @DisplayName("Зависимости подбираются по типу и внедряются через конструктор")
        void injectsByType() {
            Components.OrderFacade facade = healthyContainer().getBean(Components.OrderFacade.class);

            assertThat(
                "container cannot wire the graph by type",
                facade.checkout("книга"),
                equalTo("2026-07-30 книга")
            );
        }

        @Test
        @DisplayName("Бины — синглтоны: один и тот же экземпляр везде")
        void beansAreSingletons() {
            MiniContainer container = healthyContainer();

            assertThat(
                "container cannot keep beans singleton",
                container.getBean(Components.Repository.class),
                sameInstance(container.getBean(Components.OrderService.class).repository())
            );
        }

        @Test
        @DisplayName("Порядок создания вычисляется из графа: сначала зависимости")
        void creationOrderFollowsTheGraph() {
            MiniContainer container = healthyContainer();
            container.getBean(Components.OrderFacade.class);

            assertThat(
                "creation order cannot follow the dependency graph",
                container.creationOrder(),
                contains("repository", "clock", "orders", "orderFacade")
            );
        }

        @Test
        @DisplayName("Пока бин не запрошен, он не создан — ленивость по умолчанию")
        void beansAreCreatedOnDemand() {
            MiniContainer container = healthyContainer();

            assertThat(
                "unrequested bean cannot stay uncreated",
                container.isCreated("repository"),
                equalTo(false)
            );
        }

        @Test
        @DisplayName("после запроса бин создан")
        void createsOnRequest() {
            MiniContainer container = healthyContainer();
            container.getBean(Components.Repository.class);
            assertThat(
                "requested bean cannot be created",
                container.isCreated("repository"),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("refresh() создаёт все бины сразу — как Spring поступает с синглтонами")
        void refreshCreatesEverything() {
            MiniContainer container = healthyContainer().refresh();

            assertThat(
                "refresh cannot create every singleton at once",
                container.beanNames(),
                containsInAnyOrder("orderFacade", "orders", "repository", "clock")
            );
        }

        @Test
        @DisplayName("Имя бина берётся из аннотации, иначе — из имени класса")
        void resolvesBeanNames() {
            MiniContainer container = healthyContainer();

            assertThat(
                "annotation cannot define the bean name",
                container.beanNames(),
                hasItem("orders")
            );
        }

        @Test
        @DisplayName("по заданному имени находится нужный бин")
        void findsBeanByName() {
            assertThat(
                "named bean cannot be found by its name",
                healthyContainer().getBean("orders"),
                instanceOf(Components.OrderService.class)
            );
        }

        @Test
        @DisplayName("Поиск по интерфейсу находит реализацию")
        void findsByInterface() {
            MiniContainer container = new MiniContainer(Components.CardPayment.class);

            assertThat(
                "interface lookup cannot find the implementation",
                container.getBean(Components.Payment.class).kind(),
                equalTo("card")
            );
        }
    }

    @Nested
    @DisplayName("Ошибки, которые повторяет настоящий Spring")
    class Failures {

        @Test
        @DisplayName("Зависимости нет в контейнере — аналог NoSuchBeanDefinitionException")
        void missingDependency() {
            MiniContainer container = new MiniContainer(Components.NeedsUnmanaged.class);

            assertThat(
                "missing dependency cannot be named in the failure",
                assertThrows(
                    MiniContainer.NoSuchBeanException.class,
                    () -> container.getBean(Components.NeedsUnmanaged.class)
                ).getMessage(),
                containsString("UnmanagedDependency")
            );
        }

        @Test
        @DisplayName("Кандидатов слишком много — аналог NoUniqueBeanDefinitionException")
        void ambiguousDependency() {
            MiniContainer container = new MiniContainer(
                    Components.CardPayment.class, Components.CashPayment.class);

            assertThat(
                "ambiguous candidates cannot be listed in the failure",
                assertThrows(
                    MiniContainer.NoUniqueBeanException.class,
                    () -> container.getBean(Components.Payment.class)
                ).getMessage(),
                containsString("cardPayment")
            );
        }

        @Test
        @DisplayName("Цикл через конструкторы — аналог BeanCurrentlyInCreationException")
        void circularDependency() {
            MiniContainer container = new MiniContainer(
                    Components.AlphaService.class, Components.BetaService.class);

            assertThat(
                "circular dependency cannot name both beans",
                assertThrows(
                    MiniContainer.CircularDependencyException.class,
                    () -> container.getBean(Components.AlphaService.class)
                ).getMessage(),
                containsString("BetaService")
            );
        }

        @Test
        @DisplayName("Несколько конструкторов без явного указания — контейнер отказывается гадать")
        void ambiguousConstructor() {
            MiniContainer container = new MiniContainer(
                    Components.TwoConstructors.class, Components.Repository.class,
                    Components.Clock.class);

            assertThat(
                "ambiguous constructor cannot make the container refuse to guess",
                assertThrows(
                    IllegalStateException.class,
                    () -> container.getBean(Components.TwoConstructors.class)
                ).getMessage(),
                containsString("несколько конструкторов")
            );
        }

        @Test
        @DisplayName("Неуправляемый класс зарегистрировать нельзя")
        void unmanagedClassCannotBeRegistered() {
            assertThat(
                "unmanaged class cannot be rejected at registration",
                assertThrows(
                    IllegalArgumentException.class,
                    () -> new MiniContainer(Components.UnmanagedDependency.class)
                ).getMessage(),
                containsString("@MiniComponent")
            );
        }

        @Test
        @DisplayName("Дубликат имени бина обнаруживается сразу при регистрации")
        void duplicateBeanName() {
            assertThat(
                "duplicate bean name cannot be caught at registration",
                assertThrows(
                    IllegalStateException.class,
                    () -> new MiniContainer(Components.Repository.class, Components.Repository.class)
                ).getMessage(),
                containsString("уже занято")
            );
        }

        @Test
        @DisplayName("Неизвестное имя бина — понятная ошибка со списком известных")
        void unknownBeanName() {
            assertThat(
                "unknown bean name cannot be reported with the known ones",
                assertThrows(
                    MiniContainer.NoSuchBeanException.class,
                    () -> healthyContainer().getBean("нет-такого")
                ).getMessage(),
                containsString("известны")
            );
        }
    }

    @Nested
    @DisplayName("Единственный конструктор — правило Spring")
    class ConstructorSelection {

        @Test
        @DisplayName("Один конструктор используется без всяких аннотаций")
        void singleConstructorNeedsNoAnnotation() {
            assertThat(
                "single constructor cannot be used without an annotation",
                MiniContainer.selectConstructor(Components.OrderService.class).getParameterCount(),
                equalTo(2)
            );
        }

        @Test
        @DisplayName("Имя бина по умолчанию — имя класса с маленькой буквы")
        void defaultNameIsDecapitalizedClassName() {
            assertThat(
                "default bean name cannot be the decapitalised class name",
                MiniContainer.defaultName(Components.Repository.class),
                equalTo("repository")
            );
        }
    }
}
