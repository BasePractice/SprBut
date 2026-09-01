/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m13;

import java.util.Arrays;
import java.util.TimeZone;
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

    /**
     * Слайд 101: @Scope.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 101: @Scope")
    final class Scopes {

        @BeforeEach
        void reset() {
            ScopeConfig.resetCounters();
        }

        @Test
        @DisplayName("singleton — один экземпляр на контейнер")
        void singletonIsCreatedOnce() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScopeConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that singleton is created once",
                    context.getBean(ScopeConfig.SingletonBean.class),
                    Matchers.sameInstance(context.getBean(ScopeConfig.SingletonBean.class))
                );
            }
        }

        @Test
        @DisplayName("singleton создаётся ровно один раз за жизнь контейнера")
        void singletonIsInstantiatedOnce() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScopeConfig.class)
            ) {
                context.getBean(ScopeConfig.SingletonBean.class);
                MatcherAssert.assertThat(
                    "singleton cannot be instantiated exactly once",
                    ScopeConfig.SINGLETONS.get(),
                    Matchers.equalTo(1)
                );
            }
        }

        @Test
        @DisplayName("prototype — новый экземпляр на каждый запрос")
        void prototypeIsCreatedEveryTime() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScopeConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that prototype is created every time",
                    context.getBean(ScopeConfig.PrototypeBean.class),
                    Matchers.not(
                        Matchers.sameInstance(context.getBean(ScopeConfig.PrototypeBean.class))
                    )
                );
            }
        }

        @Test
        @DisplayName("каждый следующий prototype получает свежий номер")
        void prototypeGetsFreshSerial() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScopeConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "second prototype cannot get a fresh serial",
                    context.getBean(ScopeConfig.PrototypeBean.class).serial()
                        < context.getBean(ScopeConfig.PrototypeBean.class).serial(),
                    Matchers.equalTo(true)
                );
            }
        }

        @Test
        @DisplayName("Ловушка: prototype внутри singleton без прокси создаётся один раз")
        void prototypeInsideSingletonDegradesWithoutProxy() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScopeConfig.class)
            ) {
                final ScopeConfig.HolderWithoutProxy holder =
                    context.getBean(ScopeConfig.HolderWithoutProxy.class);
                MatcherAssert.assertThat(
                    "prototype injected once cannot keep the same serial",
                    holder.prototypeSerial(),
                    Matchers.equalTo(holder.prototypeSerial())
                );
            }
        }

        @Test
        @DisplayName("proxyMode = TARGET_CLASS возвращает prototype его настоящее поведение")
        void proxyModeRestoresPrototypeSemantics() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScopeConfig.class)
            ) {
                final ScopeConfig.HolderWithProxy holder =
                    context.getBean(ScopeConfig.HolderWithProxy.class);
                MatcherAssert.assertThat(
                    "scoped proxy cannot fetch a fresh prototype per call",
                    holder.prototypeSerial(),
                    Matchers.not(Matchers.equalTo(holder.prototypeSerial()))
                );
            }
        }
    }

    /**
     * Слайды 102–103: @Qualifier и @Primary.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайды 102–103: @Qualifier и @Primary")
    final class Qualifiers {

        @Test
        @DisplayName("Без уточнений выбирается @Primary")
        void primaryWinsByDefault() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(QualifierConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that primary wins by default",
                    context.getBean(QualifierConfig.PrimaryConsumer.class)
                        .gateway()
                        .name(),
                    Matchers.equalTo("card")
                );
            }
        }

        @Test
        @DisplayName("@Qualifier по имени бина перебивает @Primary")
        void qualifierBeatsPrimary() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(QualifierConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that qualifier beats primary",
                    context.getBean(QualifierConfig.QualifiedConsumer.class)
                        .gateway()
                        .name(),
                    Matchers.equalTo("cash")
                );
            }
        }

        @Test
        @DisplayName("@Qualifier работает и по значению аннотации, а не только по имени бина")
        void qualifierByTag() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(QualifierConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that qualifier by tag",
                    context.getBean(QualifierConfig.TaggedConsumer.class)
                        .gateway()
                        .name(),
                    Matchers.equalTo("sbp")
                );
            }
        }

        @Test
        @DisplayName("Список и карта внедряют все бины типа сразу — @Primary тут ни при чём")
        void collectionsInjectEverything() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(QualifierConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that collections inject everything",
                    context.getBean(QualifierConfig.GatewayRegistry.class).all(),
                    Matchers.hasSize(3)
                );
            }
        }

        @Test
        @DisplayName("карта внедрения даёт имена бинов ключами")
        void mapInjectionKeepsBeanNames() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(QualifierConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "map injection cannot keep the bean names as keys",
                    context.getBean(QualifierConfig.GatewayRegistry.class)
                        .byName()
                        .keySet(),
                    Matchers.containsInAnyOrder("cardGateway", "cashGateway", "sbpGateway")
                );
            }
        }
    }

    /**
     * Слайды 104–106: @Conditional, @Profile, @Lazy, @DependsOn.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайды 104–106: @Conditional, @Profile, @Lazy, @DependsOn")
    final class Conditions {

        @BeforeEach
        void reset() {
            ConditionalConfig.reset();
        }

        @Test
        @DisplayName("@Conditional: без свойства бина нет вовсе")
        void conditionalBeanIsAbsentByDefault() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ConditionalConfig.class)
            ) {
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext()
            ) {
                context.getEnvironment()
                    .getSystemProperties()
                    .put("sprbut.feature.enabled", "true");
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext()
            ) {
                context.getEnvironment().setActiveProfiles("dev");
                context.register(ConditionalConfig.class);
                context.refresh();
                MatcherAssert.assertThat(
                    "cannot verify that profile selects beans",
                    context.containsBean("devOnlyBean"),
                    Matchers.equalTo(true)
                );
            }
        }

        @Test
        @DisplayName("@Profile прячет бины чужого профиля")
        void profileHidesForeignBeans() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext()
            ) {
                context.getEnvironment().setActiveProfiles("dev");
                context.register(ConditionalConfig.class);
                context.refresh();
                MatcherAssert.assertThat(
                    "profile cannot hide the beans of the other profile",
                    context.containsBean("notDevBean"),
                    Matchers.equalTo(false)
                );
            }
        }

        @Test
        @DisplayName("@Profile(\"!dev\") — отрицание профиля")
        void negatedProfile() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ConditionalConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that negated profile",
                    context.containsBean("notDevBean"),
                    Matchers.equalTo(true)
                );
            }
        }

        @Test
        @DisplayName("бин профиля dev без активного профиля не создаётся")
        void devProfileBeanIsAbsent() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ConditionalConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "dev profile bean cannot stay absent",
                    context.containsBean("devOnlyBean"),
                    Matchers.equalTo(false)
                );
            }
        }

        @Test
        @DisplayName("@Lazy откладывает создание до первого обращения")
        void lazyBeanIsCreatedOnDemand() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ConditionalConfig.class)
            ) {
                context.getBean("lazyBean");
                MatcherAssert.assertThat(
                    "cannot verify that lazy bean is created on demand",
                    ConditionalConfig.CREATED,
                    Matchers.hasItems("lazyBean")
                );
            }
        }

        @Test
        @DisplayName("@Lazy: до первого обращения бина нет")
        void lazyBeanIsNotCreatedAtStartup() {
            try (
                AnnotationConfigApplicationContext ignored =
                    new AnnotationConfigApplicationContext(ConditionalConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "lazy bean cannot stay uncreated until requested",
                    ConditionalConfig.CREATED,
                    Matchers.not(Matchers.hasItem("lazyBean"))
                );
            }
        }

        @Test
        @DisplayName("@DependsOn задаёт порядок там, где зависимости в коде нет")
        void dependsOnOrdersCreation() {
            try (
                AnnotationConfigApplicationContext ignored =
                    new AnnotationConfigApplicationContext(ConditionalConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "@DependsOn cannot order the creation",
                    ConditionalConfig.CREATED.indexOf("schemaInitializer"),
                    Matchers.lessThan(ConditionalConfig.CREATED.indexOf("cacheWarmer"))
                );
            }
        }
    }

    /**
     * Слайд 108: @Component vs @Bean.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 108: @Component vs @Bean")
    final class ComponentOrBean {

        @Test
        @DisplayName("Свой класс находится сканированием")
        void ownClassIsScanned() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)
            ) {
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that third party class needs a bean method",
                    context.getBean(ComponentVsBean.ThirdPartyClient.class).describe(),
                    Matchers.equalTo("чужой класс: https://api.example.com (3000 мс)")
                );
            }
        }

        @Test
        @DisplayName("на чужом классе нет и не может быть наших аннотаций")
        void thirdPartyClassStaysUnannotated() {
            MatcherAssert.assertThat(
                "third party class cannot stay free of our annotations",
                ComponentVsBean.ThirdPartyClient.class.getAnnotations(),
                Matchers.emptyArray()
            );
        }

        @Test
        @DisplayName("@Bean умеет регистрировать даже классы из JDK")
        void beanMethodWorksForJdkClasses() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that bean method works for jdk classes",
                    context.getBean(TimeZone.class).getID(),
                    Matchers.equalTo("Europe/Moscow")
                );
            }
        }

        @Test
        @DisplayName("Технически оба дают одинаковое определение бина")
        void bothProduceBeanDefinitions() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)
            ) {
                MatcherAssert.assertThat(
                    "both styles cannot produce bean definitions",
                    Arrays.asList(context.getBeanDefinitionNames()),
                    Matchers.hasItems("thirdPartyClient", "applicationTimeZone")
                );
            }
        }
    }
}
