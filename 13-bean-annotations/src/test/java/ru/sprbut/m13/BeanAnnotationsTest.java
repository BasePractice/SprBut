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

import static org.assertj.core.api.Assertions.assertThat;

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
                assertThat(context.getBean(ScopeConfig.SingletonBean.class))
                        .isSameAs(context.getBean(ScopeConfig.SingletonBean.class));
                assertThat(ScopeConfig.SINGLETON_INSTANCES.get()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("prototype — новый экземпляр на каждый запрос")
        void prototypeIsCreatedEveryTime() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                ScopeConfig.PrototypeBean first = context.getBean(ScopeConfig.PrototypeBean.class);
                ScopeConfig.PrototypeBean second = context.getBean(ScopeConfig.PrototypeBean.class);

                assertThat(first).isNotSameAs(second);
                assertThat(second.serial()).isGreaterThan(first.serial());
            }
        }

        @Test
        @DisplayName("Ловушка: prototype внутри singleton без прокси создаётся один раз")
        void prototypeInsideSingletonDegradesWithoutProxy() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                var holder = context.getBean(ScopeConfig.HolderWithoutProxy.class);

                int first = holder.prototypeSerial();
                int second = holder.prototypeSerial();

                assertThat(first)
                        .as("зависимость внедрена один раз при создании владельца")
                        .isEqualTo(second);
            }
        }

        @Test
        @DisplayName("proxyMode = TARGET_CLASS возвращает prototype его настоящее поведение")
        void proxyModeRestoresPrototypeSemantics() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                var holder = context.getBean(ScopeConfig.HolderWithProxy.class);

                assertThat(holder.prototypeSerial()).isNotEqualTo(holder.prototypeSerial());
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
                assertThat(context.getBean(QualifierConfig.PrimaryConsumer.class)
                        .gateway().name()).isEqualTo("card");
            }
        }

        @Test
        @DisplayName("@Qualifier по имени бина перебивает @Primary")
        void qualifierBeatsPrimary() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(context.getBean(QualifierConfig.QualifiedConsumer.class)
                        .gateway().name()).isEqualTo("cash");
            }
        }

        @Test
        @DisplayName("@Qualifier работает и по значению аннотации, а не только по имени бина")
        void qualifierByTag() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(context.getBean(QualifierConfig.TaggedConsumer.class)
                        .gateway().name()).isEqualTo("sbp");
            }
        }

        @Test
        @DisplayName("Список и карта внедряют все бины типа сразу — @Primary тут ни при чём")
        void collectionsInjectEverything() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                var registry = context.getBean(QualifierConfig.GatewayRegistry.class);

                assertThat(registry.all()).hasSize(3);
                assertThat(registry.byName().keySet())
                        .containsExactlyInAnyOrder("cardGateway", "cashGateway", "sbpGateway");
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
                assertThat(context.containsBean("featureBean")).isFalse();
            }
        }

        @Test
        @DisplayName("@Conditional: со свойством бин появляется")
        void conditionalBeanAppearsWhenPropertySet() {
            try (var context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment().getSystemProperties().put("sprbut.feature.enabled", "true");
                context.register(ConditionalConfig.class);
                context.refresh();

                assertThat(context.containsBean("featureBean")).isTrue();
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

                assertThat(context.containsBean("devOnlyBean")).isTrue();
                assertThat(context.containsBean("notDevBean")).isFalse();
            }
        }

        @Test
        @DisplayName("@Profile(\"!dev\") — отрицание профиля")
        void negatedProfile() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(context.containsBean("notDevBean")).isTrue();
                assertThat(context.containsBean("devOnlyBean")).isFalse();
            }
        }

        @Test
        @DisplayName("@Lazy откладывает создание до первого обращения")
        void lazyBeanIsCreatedOnDemand() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(ConditionalConfig.CREATED).doesNotContain("lazyBean");

                context.getBean("lazyBean");

                assertThat(ConditionalConfig.CREATED).contains("lazyBean");
            }
        }

        @Test
        @DisplayName("@DependsOn задаёт порядок там, где зависимости в коде нет")
        void dependsOnOrdersCreation() {
            try (var ignored = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(ConditionalConfig.CREATED.indexOf("schemaInitializer"))
                        .isLessThan(ConditionalConfig.CREATED.indexOf("cacheWarmer"));
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
                assertThat(context.getBean(ComponentVsBean.OwnService.class).describe())
                        .isEqualTo("свой класс, найден сканированием");
            }
        }

        @Test
        @DisplayName("Чужой класс регистрируется @Bean-методом — аннотацию ставить некуда")
        void thirdPartyClassNeedsABeanMethod() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                assertThat(context.getBean(ComponentVsBean.ThirdPartyClient.class).describe())
                        .isEqualTo("чужой класс: https://api.example.com (3000 мс)");
                assertThat(ComponentVsBean.ThirdPartyClient.class.getAnnotations()).isEmpty();
            }
        }

        @Test
        @DisplayName("@Bean умеет регистрировать даже классы из JDK")
        void beanMethodWorksForJdkClasses() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                assertThat(context.getBean(java.util.TimeZone.class).getID())
                        .isEqualTo("Europe/Moscow");
            }
        }

        @Test
        @DisplayName("Технически оба дают одинаковое определение бина")
        void bothProduceBeanDefinitions() {
            try (var context = new AnnotationConfigApplicationContext(ComponentVsBean.Config.class)) {
                // имя бина от @Component — от имени класса, от @Bean — от имени метода
                assertThat(context.getBeanDefinitionNames())
                        .contains("componentVsBean.OwnService", "thirdPartyClient", "applicationTimeZone");
            }
        }
    }
}
