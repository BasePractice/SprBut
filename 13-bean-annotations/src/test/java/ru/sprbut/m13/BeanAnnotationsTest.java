package ru.sprbut.m13;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m13.componentvsbean.ComponentVsBean;
import ru.sprbut.m13.conditional.ConditionalConfig;
import ru.sprbut.m13.qualifiers.QualifierConfig;
import ru.sprbut.m13.scopes.ScopeConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайды 99–110: аннотации бина")
class BeanAnnotationsTest {

    @Nested
    @DisplayName("Слайд 101: @Scope")
    class Scopes {

        @BeforeEach
        void reset() {
            ScopeConfig.resetCounters();
        }

        @Test
        @DisplayName("singleton — один экземпляр на контейнер")
        void singletonIsCreatedOnce() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                assertThat(
                    "cannot verify that singleton is created once",
                    context.getBean(ScopeConfig.SingletonBean.class),
                    sameInstance(context.getBean(ScopeConfig.SingletonBean.class))
                );
                assertThat(
                    "cannot verify that singleton is created once",
                    ScopeConfig.SINGLETON_INSTANCES.get(),
                    equalTo(1)
                );
            }
        }

        @Test
        @DisplayName("prototype — новый экземпляр на каждый запрос")
        void prototypeIsCreatedEveryTime() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                ScopeConfig.PrototypeBean first = context.getBean(ScopeConfig.PrototypeBean.class);
                ScopeConfig.PrototypeBean second = context.getBean(ScopeConfig.PrototypeBean.class);

                assertThat(
                    "cannot verify that prototype is created every time",
                    first,
                    not(sameInstance(second))
                );
                assertThat(
                    "second prototype cannot get a fresh serial",
                    second.serial(),
                    greaterThan(first.serial())
                );
            }
        }

        @Test
        @DisplayName("Ловушка: prototype внутри singleton без прокси создаётся один раз")
        void prototypeInsideSingletonDegradesWithoutProxy() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                var holder = context.getBean(ScopeConfig.HolderWithoutProxy.class);

                int first = holder.prototypeSerial();
                int second = holder.prototypeSerial();

                assertThat(
                    "prototype injected once cannot keep the same serial",
                    first,
                    equalTo(second)
                );
            }
        }

        @Test
        @DisplayName("proxyMode = TARGET_CLASS возвращает prototype его настоящее поведение")
        void proxyModeRestoresPrototypeSemantics() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                var holder = context.getBean(ScopeConfig.HolderWithProxy.class);

                assertThat(
                    "scoped proxy cannot fetch a fresh prototype per call",
                    holder.prototypeSerial(),
                    not(equalTo(holder.prototypeSerial()))
                );
            }
        }
    }

    @Nested
    @DisplayName("Слайды 102–103: @Qualifier и @Primary")
    class Qualifiers {

        @Test
        @DisplayName("Без уточнений выбирается @Primary")
        void primaryWinsByDefault() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(
                    "cannot verify that primary wins by default",
                    context.getBean(QualifierConfig.PrimaryConsumer.class) .gateway().name(),
                    equalTo("card")
                );
            }
        }

        @Test
        @DisplayName("@Qualifier по имени бина перебивает @Primary")
        void qualifierBeatsPrimary() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(
                    "cannot verify that qualifier beats primary",
                    context.getBean(QualifierConfig.QualifiedConsumer.class) .gateway().name(),
                    equalTo("cash")
                );
            }
        }

        @Test
        @DisplayName("@Qualifier работает и по значению аннотации, а не только по имени бина")
        void qualifierByTag() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(
                    "cannot verify that qualifier by tag",
                    context.getBean(QualifierConfig.TaggedConsumer.class) .gateway().name(),
                    equalTo("sbp")
                );
            }
        }

        @Test
        @DisplayName("Список и карта внедряют все бины типа сразу — @Primary тут ни при чём")
        void collectionsInjectEverything() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                var registry = context.getBean(QualifierConfig.GatewayRegistry.class);

                assertThat(
                    "cannot verify that collections inject everything",
                    registry.all(),
                    hasSize(3)
                );
                assertThat(
                    "cannot verify that collections inject everything",
                    registry.byName().keySet(),
                    containsInAnyOrder("cardGateway", "cashGateway", "sbpGateway")
                );
            }
        }
    }

    @Nested
    @DisplayName("Слайды 104–106: @Conditional, @Profile, @Lazy, @DependsOn")
    class Conditions {

        @BeforeEach
        void reset() {
            ConditionalConfig.reset();
        }

        @Test
        @DisplayName("@Conditional: без свойства бина нет вовсе")
        void conditionalBeanIsAbsentByDefault() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(
                    "cannot verify that conditional bean is absent by default",
                    context.containsBean("featureBean"),
                    equalTo(false)
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

                assertThat(
                    "cannot verify that conditional bean appears when property set",
                    context.containsBean("featureBean"),
                    equalTo(true)
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

                assertThat(
                    "cannot verify that profile selects beans",
                    context.containsBean("devOnlyBean"),
                    equalTo(true)
                );
                assertThat(
                    "cannot verify that profile selects beans",
                    context.containsBean("notDevBean"),
                    equalTo(false)
                );
            }
        }

        @Test
        @DisplayName("@Profile(\"!dev\") — отрицание профиля")
        void negatedProfile() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(
                    "cannot verify that negated profile",
                    context.containsBean("notDevBean"),
                    equalTo(true)
                );
                assertThat(
                    "cannot verify that negated profile",
                    context.containsBean("devOnlyBean"),
                    equalTo(false)
                );
            }
        }

        @Test
        @DisplayName("@Lazy откладывает создание до первого обращения")
        void lazyBeanIsCreatedOnDemand() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(
                    "lazy bean cannot stay uncreated until requested",
                    ConditionalConfig.CREATED,
                    not(hasItem("lazyBean"))
                );

                context.getBean("lazyBean");

                assertThat(
                    "cannot verify that lazy bean is created on demand",
                    ConditionalConfig.CREATED,
                    hasItems("lazyBean")
                );
            }
        }

        @Test
        @DisplayName("@DependsOn задаёт порядок там, где зависимости в коде нет")
        void dependsOnOrdersCreation() {
            try (var ignored = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(
                    "@DependsOn cannot order the creation",
                    ConditionalConfig.CREATED.indexOf("schemaInitializer"),
                    lessThan(ConditionalConfig.CREATED.indexOf("cacheWarmer"))
                );
            }
        }
    }

    @Nested
    @DisplayName("Слайд 108: @Component vs @Bean")
    class ComponentOrBean {

        @Test
        @DisplayName("Свой класс находится сканированием")
        void ownClassIsScanned() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                assertThat(
                    "cannot verify that own class is scanned",
                    context.getBean(ComponentVsBean.OwnService.class).describe(),
                    equalTo("свой класс, найден сканированием")
                );
            }
        }

        @Test
        @DisplayName("Чужой класс регистрируется @Bean-методом — аннотацию ставить некуда")
        void thirdPartyClassNeedsABeanMethod() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                assertThat(
                    "cannot verify that third party class needs a bean method",
                    context.getBean(ComponentVsBean.ThirdPartyClient.class).describe(),
                    equalTo("чужой класс: https://api.example.com (3000 мс)")
                );
                assertThat(
                    "third party class cannot stay free of our annotations",
                    ComponentVsBean.ThirdPartyClient.class.getAnnotations(),
                    emptyArray()
                );
            }
        }

        @Test
        @DisplayName("@Bean умеет регистрировать даже классы из JDK")
        void beanMethodWorksForJdkClasses() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                assertThat(
                    "cannot verify that bean method works for jdk classes",
                    context.getBean(java.util.TimeZone.class).getID(),
                    equalTo("Europe/Moscow")
                );
            }
        }

        @Test
        @DisplayName("Технически оба дают одинаковое определение бина")
        void bothProduceBeanDefinitions() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                // имя бина от @Component — от имени класса, от @Bean — от имени метода
                assertThat(
                    "both styles cannot produce bean definitions",
                    java.util.Arrays.asList(context.getBeanDefinitionNames()),
                    hasItems("thirdPartyClient", "applicationTimeZone")
                );
            }
        }
    }
}
