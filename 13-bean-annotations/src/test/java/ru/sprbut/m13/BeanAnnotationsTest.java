/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m13;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m13.componentvsbean.ComponentVsBean;
import ru.sprbut.m13.conditional.ConditionalConfig;
import ru.sprbut.m13.qualifiers.QualifierConfig;
import ru.sprbut.m13.scopes.ScopeConfig;

/**
 * Слайды 99–110: аннотации бина.
 * @since 1.0
 */
@DisplayName("Слайды 99–110: аннотации бина")
final class BeanAnnotationsTest {

    @Nested
/**
 * Слайд 101: @Scope.
 * @since 1.0
 */
    @DisplayName("Слайд 101: @Scope")
    final class Scopes {

        @BeforeEach
        void reset() {
            ScopeConfig.resetCounters();
        }

        @Test
        @DisplayName("singleton — один экземпляр на контейнер")
        void singletonIsCreatedOnce() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that singleton is created once",
                    context.getBean(ScopeConfig.SingletonBean.class),
                    Matchers.sameInstance(context.getBean(ScopeConfig.SingletonBean.class))
                );
                MatcherAssert.assertThat(
                    "cannot verify that singleton is created once",
                    ScopeConfig.SINGLETON_INSTANCES.get(),
                    Matchers.equalTo(1)
                );
            }
        }

        @Test
        @DisplayName("prototype — новый экземпляр на каждый запрос")
        void prototypeIsCreatedEveryTime() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                final ScopeConfig.PrototypeBean first = context.getBean(ScopeConfig.PrototypeBean.class);
                final ScopeConfig.PrototypeBean second = context.getBean(ScopeConfig.PrototypeBean.class);
                MatcherAssert.assertThat(
                    "cannot verify that prototype is created every time",
                    first,
                    Matchers.not(Matchers.sameInstance(second))
                );
                MatcherAssert.assertThat(
                    "second prototype cannot get a fresh serial",
                    second.serial(),
                    Matchers.greaterThan(first.serial())
                );
            }
        }

        @Test
        @DisplayName("Ловушка: prototype внутри singleton без прокси создаётся один раз")
        void prototypeInsideSingletonDegradesWithoutProxy() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                final var holder = context.getBean(ScopeConfig.HolderWithoutProxy.class);
                final int first = holder.prototypeSerial();
                final int second = holder.prototypeSerial();
                MatcherAssert.assertThat(
                    "prototype injected once cannot keep the same serial",
                    first,
                    Matchers.equalTo(second)
                );
            }
        }

        @Test
        @DisplayName("proxyMode = TARGET_CLASS возвращает prototype его настоящее поведение")
        void proxyModeRestoresPrototypeSemantics() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                final var holder = context.getBean(ScopeConfig.HolderWithProxy.class);
                MatcherAssert.assertThat(
                    "scoped proxy cannot fetch a fresh prototype per call",
                    holder.prototypeSerial(),
                    Matchers.not(Matchers.equalTo(holder.prototypeSerial()))
                );
            }
        }
    }

    @Nested
/**
 * Слайды 102–103: @Qualifier и @Primary.
 * @since 1.0
 */
    @DisplayName("Слайды 102–103: @Qualifier и @Primary")
    final class Qualifiers {

        @Test
        @DisplayName("Без уточнений выбирается @Primary")
        void primaryWinsByDefault() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that primary wins by default",
                    context.getBean(QualifierConfig.PrimaryConsumer.class) .gateway().name(),
                    Matchers.equalTo("card")
                );
            }
        }

        @Test
        @DisplayName("@Qualifier по имени бина перебивает @Primary")
        void qualifierBeatsPrimary() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that qualifier beats primary",
                    context.getBean(QualifierConfig.QualifiedConsumer.class) .gateway().name(),
                    Matchers.equalTo("cash")
                );
            }
        }

        @Test
        @DisplayName("@Qualifier работает и по значению аннотации, а не только по имени бина")
        void qualifierByTag() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that qualifier by tag",
                    context.getBean(QualifierConfig.TaggedConsumer.class) .gateway().name(),
                    Matchers.equalTo("sbp")
                );
            }
        }

        @Test
        @DisplayName("Список и карта внедряют все бины типа сразу — @Primary тут ни при чём")
        void collectionsInjectEverything() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                final var registry = context.getBean(QualifierConfig.GatewayRegistry.class);
                MatcherAssert.assertThat(
                    "cannot verify that collections inject everything",
                    registry.all(),
                    Matchers.hasSize(3)
                );
                MatcherAssert.assertThat(
                    "cannot verify that collections inject everything",
                    registry.byName().keySet(),
                    Matchers.containsInAnyOrder("cardGateway", "cashGateway", "sbpGateway")
                );
            }
        }
    }

    @Nested
/**
 * Слайды 104–106: @Conditional, @Profile, @Lazy, @DependsOn.
 * @since 1.0
 */
    @DisplayName("Слайды 104–106: @Conditional, @Profile, @Lazy, @DependsOn")
    final class Conditions {

        @BeforeEach
        void reset() {
            ConditionalConfig.reset();
        }

        @Test
        @DisplayName("@Conditional: без свойства бина нет вовсе")
        void conditionalBeanIsAbsentByDefault() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that conditional bean is absent by default",
                    context.containsBean("featureBean"),
                    Matchers.equalTo(false)
                );
            }
        }

        @Test
        @DisplayName("@Conditional: со свойством бин появляется")
        void conditionalBeanAppearsWhenPropertySet() {
            try (var context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment().getSystemProperties().put("sprbut.feature.enabled", "true");
                context.register(ConditionalConfig.class);
                context.refresh();
                MatcherAssert.assertThat(
                    "cannot verify that conditional bean appears when property set",
                    context.containsBean("featureBean"),
                    Matchers.equalTo(true)
                );
            } finally {
                System.clearProperty("sprbut.feature.enabled");
            }
        }

        @Test
        @DisplayName("@Profile выбирает набор бинов по активному профилю")
        void profileSelectsBeans() {
            try (var context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment().setActiveProfiles("dev");
                context.register(ConditionalConfig.class);
                context.refresh();
                MatcherAssert.assertThat(
                    "cannot verify that profile selects beans",
                    context.containsBean("devOnlyBean"),
                    Matchers.equalTo(true)
                );
                MatcherAssert.assertThat(
                    "cannot verify that profile selects beans",
                    context.containsBean("notDevBean"),
                    Matchers.equalTo(false)
                );
            }
        }

        @Test
        @DisplayName("@Profile(\"!dev\") — отрицание профиля")
        void negatedProfile() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that negated profile",
                    context.containsBean("notDevBean"),
                    Matchers.equalTo(true)
                );
                MatcherAssert.assertThat(
                    "cannot verify that negated profile",
                    context.containsBean("devOnlyBean"),
                    Matchers.equalTo(false)
                );
            }
        }

        @Test
        @DisplayName("@Lazy откладывает создание до первого обращения")
        void lazyBeanIsCreatedOnDemand() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                MatcherAssert.assertThat(
                    "lazy bean cannot stay uncreated until requested",
                    ConditionalConfig.CREATED,
                    Matchers.not(Matchers.hasItem("lazyBean"))
                );
                context.getBean("lazyBean");
                MatcherAssert.assertThat(
                    "cannot verify that lazy bean is created on demand",
                    ConditionalConfig.CREATED,
                    Matchers.hasItems("lazyBean")
                );
            }
        }

        @Test
        @DisplayName("@DependsOn задаёт порядок там, где зависимости в коде нет")
        void dependsOnOrdersCreation() {
            try (var ignored = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                MatcherAssert.assertThat(
                    "@DependsOn cannot order the creation",
                    ConditionalConfig.CREATED.indexOf("schemaInitializer"),
                    Matchers.lessThan(ConditionalConfig.CREATED.indexOf("cacheWarmer"))
                );
            }
        }
    }

    @Nested
/**
 * Слайд 108: @Component vs @Bean.
 * @since 1.0
 */
    @DisplayName("Слайд 108: @Component vs @Bean")
    final class ComponentOrBean {

        @Test
        @DisplayName("Свой класс находится сканированием")
        void ownClassIsScanned() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that own class is scanned",
                    context.getBean(ComponentVsBean.OwnService.class).describe(),
                    Matchers.equalTo("свой класс, найден сканированием")
                );
            }
        }

        @Test
        @DisplayName("Чужой класс регистрируется @Bean-методом — аннотацию ставить некуда")
        void thirdPartyClassNeedsABeanMethod() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that third party class needs a bean method",
                    context.getBean(ComponentVsBean.ThirdPartyClient.class).describe(),
                    Matchers.equalTo("чужой класс: https://api.example.com (3000 мс)")
                );
                MatcherAssert.assertThat(
                    "third party class cannot stay free of our annotations",
                    ComponentVsBean.ThirdPartyClient.class.getAnnotations(),
                    Matchers.emptyArray()
                );
            }
        }

        @Test
        @DisplayName("@Bean умеет регистрировать даже классы из JDK")
        void beanMethodWorksForJdkClasses() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that bean method works for jdk classes",
                    context.getBean(java.util.TimeZone.class).getID(),
                    Matchers.equalTo("Europe/Moscow")
                );
            }
        }

        @Test
        @DisplayName("Технически оба дают одинаковое определение бина")
        void bothProduceBeanDefinitions() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                // имя бина от @Component — от имени класса, от @Bean — от имени метода
                MatcherAssert.assertThat(
                    "both styles cannot produce bean definitions",
                    java.util.Arrays.asList(context.getBeanDefinitionNames()),
                    Matchers.hasItems("thirdPartyClient", "applicationTimeZone")
                );
            }
        }
    }
}
