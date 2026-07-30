package ru.sprbut.m11.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Расширенный пример: собственный IoC-контейнер")
class MiniContainerTest {

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

            assertThat(facade.checkout("книга")).isEqualTo("2026-07-30 книга");
            assertThat(facade.service().repository().rows()).containsExactly("2026-07-30 книга");
        }

        @Test
        @DisplayName("Бины — синглтоны: один и тот же экземпляр везде")
        void beansAreSingletons() {
            MiniContainer container = healthyContainer();

            assertThat(container.getBean(Components.Repository.class))
                    .isSameAs(container.getBean(Components.Repository.class))
                    .isSameAs(container.getBean(Components.OrderService.class).repository());
        }

        @Test
        @DisplayName("Порядок создания вычисляется из графа: сначала зависимости")
        void creationOrderFollowsTheGraph() {
            MiniContainer container = healthyContainer();
            container.getBean(Components.OrderFacade.class);

            assertThat(container.creationOrder())
                    .containsExactly("repository", "clock", "orders", "orderFacade");
        }

        @Test
        @DisplayName("Пока бин не запрошен, он не создан — ленивость по умолчанию")
        void beansAreCreatedOnDemand() {
            MiniContainer container = healthyContainer();

            assertThat(container.isCreated("repository")).isFalse();
            container.getBean(Components.Repository.class);
            assertThat(container.isCreated("repository")).isTrue();
        }

        @Test
        @DisplayName("refresh() создаёт все бины сразу — как Spring поступает с синглтонами")
        void refreshCreatesEverything() {
            MiniContainer container = healthyContainer().refresh();

            assertThat(container.beanNames())
                    .containsExactlyInAnyOrder("orderFacade", "orders", "repository", "clock");
            assertThat(container.creationOrder()).hasSize(4);
        }

        @Test
        @DisplayName("Имя бина берётся из аннотации, иначе — из имени класса")
        void resolvesBeanNames() {
            MiniContainer container = healthyContainer();

            assertThat(container.beanNames()).contains("orders").doesNotContain("orderService");
            assertThat(container.getBean("orders")).isInstanceOf(Components.OrderService.class);
        }

        @Test
        @DisplayName("Поиск по интерфейсу находит реализацию")
        void findsByInterface() {
            MiniContainer container = new MiniContainer(Components.CardPayment.class);

            assertThat(container.getBean(Components.Payment.class).kind()).isEqualTo("card");
        }
    }

    @Nested
    @DisplayName("Ошибки, которые повторяет настоящий Spring")
    class Failures {

        @Test
        @DisplayName("Зависимости нет в контейнере — аналог NoSuchBeanDefinitionException")
        void missingDependency() {
            MiniContainer container = new MiniContainer(Components.NeedsUnmanaged.class);

            assertThatThrownBy(() -> container.getBean(Components.NeedsUnmanaged.class))
                    .isInstanceOf(MiniContainer.NoSuchBeanException.class)
                    .hasMessageContaining("UnmanagedDependency");
        }

        @Test
        @DisplayName("Кандидатов слишком много — аналог NoUniqueBeanDefinitionException")
        void ambiguousDependency() {
            MiniContainer container = new MiniContainer(
                    Components.CardPayment.class, Components.CashPayment.class);

            assertThatThrownBy(() -> container.getBean(Components.Payment.class))
                    .isInstanceOf(MiniContainer.NoUniqueBeanException.class)
                    .hasMessageContaining("cardPayment")
                    .hasMessageContaining("cashPayment")
                    .hasMessageContaining("Primary");
        }

        @Test
        @DisplayName("Цикл через конструкторы — аналог BeanCurrentlyInCreationException")
        void circularDependency() {
            MiniContainer container = new MiniContainer(
                    Components.AlphaService.class, Components.BetaService.class);

            assertThatThrownBy(() -> container.getBean(Components.AlphaService.class))
                    .isInstanceOf(MiniContainer.CircularDependencyException.class)
                    .hasMessageContaining("AlphaService")
                    .hasMessageContaining("BetaService");
        }

        @Test
        @DisplayName("Несколько конструкторов без явного указания — контейнер отказывается гадать")
        void ambiguousConstructor() {
            MiniContainer container = new MiniContainer(
                    Components.TwoConstructors.class, Components.Repository.class,
                    Components.Clock.class);

            assertThatThrownBy(() -> container.getBean(Components.TwoConstructors.class))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("несколько конструкторов");
        }

        @Test
        @DisplayName("Неуправляемый класс зарегистрировать нельзя")
        void unmanagedClassCannotBeRegistered() {
            assertThatThrownBy(() -> new MiniContainer(Components.UnmanagedDependency.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("@MiniComponent");
        }

        @Test
        @DisplayName("Дубликат имени бина обнаруживается сразу при регистрации")
        void duplicateBeanName() {
            assertThatThrownBy(() -> new MiniContainer(
                    Components.Repository.class, Components.Repository.class))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("уже занято");
        }

        @Test
        @DisplayName("Неизвестное имя бина — понятная ошибка со списком известных")
        void unknownBeanName() {
            assertThatThrownBy(() -> healthyContainer().getBean("нет-такого"))
                    .isInstanceOf(MiniContainer.NoSuchBeanException.class)
                    .hasMessageContaining("известны");
        }
    }

    @Nested
    @DisplayName("Единственный конструктор — правило Spring")
    class ConstructorSelection {

        @Test
        @DisplayName("Один конструктор используется без всяких аннотаций")
        void singleConstructorNeedsNoAnnotation() {
            assertThat(MiniContainer.selectConstructor(Components.OrderService.class)
                    .getParameterCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Имя бина по умолчанию — имя класса с маленькой буквы")
        void defaultNameIsDecapitalizedClassName() {
            assertThat(MiniContainer.defaultName(Components.Repository.class)).isEqualTo("repository");
        }
    }
}
