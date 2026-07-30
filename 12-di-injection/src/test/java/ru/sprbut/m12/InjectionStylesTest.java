package ru.sprbut.m12;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;
import ru.sprbut.m12.injection.ConstructorInjected;
import ru.sprbut.m12.injection.FieldInjected;
import ru.sprbut.m12.injection.SetterInjected;
import ru.sprbut.m12.jakarta.JakartaInjected;
import ru.sprbut.m12.locator.ServiceLocatorDemo;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайды 91–96 (СХЕМА 6): три способа внедрения рядом")
class InjectionStylesTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Nested
    @DisplayName("Все три способа работают одинаково")
    class Equivalence {

        @Test
        @DisplayName("Результат не зависит от способа внедрения")
        void resultsAreIdentical() {
            BigDecimal net = new BigDecimal("100");

            assertThat(context.getBean(ConstructorInjected.class).total(net, true))
                    .isEqualByComparingTo("108.00");
            assertThat(context.getBean(SetterInjected.class).total(net, true))
                    .isEqualByComparingTo("108.00");
            assertThat(context.getBean(FieldInjected.class).total(net, true))
                    .isEqualByComparingTo("108.00");
        }
    }

    @Nested
    @DisplayName("Слайд 92: почему конструктор предпочтителен")
    class ConstructorWins {

        @Test
        @DisplayName("Класс собирается обычным new — тест не требует контейнера вовсе")
        void testableWithoutContainer() {
            ConstructorInjected service = new ConstructorInjected(
                    new TaxService(), new DiscountService());

            assertThat(service.total(new BigDecimal("100"), false)).isEqualByComparingTo("120.00");
        }

        @Test
        @DisplayName("Поля final — объект неизменяем по построению")
        void fieldsAreFinal() {
            assertThat(java.util.Arrays.stream(ConstructorInjected.class.getDeclaredFields()))
                    .allMatch(f -> java.lang.reflect.Modifier.isFinal(f.getModifiers()));
        }

        @Test
        @DisplayName("Зависимости обязательны: другого конструктора просто нет")
        void dependenciesAreMandatory() {
            assertThat(ConstructorInjected.class.getDeclaredConstructors())
                    .singleElement()
                    .satisfies(c -> assertThat(c.getParameterCount()).isEqualTo(2));
        }

        @Test
        @DisplayName("@Autowired на единственном конструкторе не нужен со Spring 4.3")
        void autowiredIsOptionalOnSingleConstructor() {
            assertThat(ConstructorInjected.class.getDeclaredConstructors()[0]
                    .isAnnotationPresent(org.springframework.beans.factory.annotation.Autowired.class))
                    .isFalse();
            assertThat(context.getBean(ConstructorInjected.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Слайд 93: внедрение в поле мешает тестам без контейнера")
    class FieldInjectionProblems {

        @Test
        @DisplayName("Собранный руками объект падает: поля остались null")
        void newInstanceIsBroken() {
            FieldInjected service = new FieldInjected();

            assertThatThrownBy(() -> service.total(new BigDecimal("100"), false))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Единственный способ подставить зависимости без контейнера — рефлексия")
        void onlyReflectionCanFillTheFields() throws Exception {
            FieldInjected service = new FieldInjected();
            var field = FieldInjected.class.getDeclaredField("taxService");
            field.setAccessible(true);
            field.set(service, new TaxService());
            var discount = FieldInjected.class.getDeclaredField("discountService");
            discount.setAccessible(true);
            discount.set(service, new DiscountService());

            assertThat(service.total(new BigDecimal("100"), false)).isEqualByComparingTo("120.00");
        }

        @Test
        @DisplayName("Зависимости не видны в API: конструктор пустой")
        void dependenciesAreInvisible() {
            assertThat(FieldInjected.class.getDeclaredConstructors())
                    .singleElement()
                    .satisfies(c -> assertThat(c.getParameterCount()).isZero());
        }

        @Test
        @DisplayName("Поля не могут быть final")
        void fieldsCannotBeFinal() {
            assertThat(java.util.Arrays.stream(FieldInjected.class.getDeclaredFields()))
                    .noneMatch(f -> java.lang.reflect.Modifier.isFinal(f.getModifiers()));
        }
    }

    @Nested
    @DisplayName("Сеттер: единственный уместный случай — необязательная зависимость")
    class SetterInjection {

        @Test
        @DisplayName("@Autowired(required = false) оставляет поле пустым, если бина нет")
        void optionalDependencyMayBeAbsent() {
            try (var minimal = new AnnotationConfigApplicationContext()) {
                minimal.register(TaxService.class, SetterInjected.class);
                minimal.refresh();

                SetterInjected service = minimal.getBean(SetterInjected.class);

                assertThat(service.hasDiscountService()).isFalse();
                assertThat(service.total(new BigDecimal("100"), true)).isEqualByComparingTo("120.00");
            }
        }

        @Test
        @DisplayName("Когда бин есть, он внедряется")
        void optionalDependencyIsInjectedWhenPresent() {
            assertThat(context.getBean(SetterInjected.class).hasDiscountService()).isTrue();
        }

        @Test
        @DisplayName("Между new и вызовом сеттера объект невалиден")
        void objectIsInvalidBetweenNewAndSetter() {
            SetterInjected service = new SetterInjected();

            assertThatThrownBy(() -> service.total(BigDecimal.TEN, false))
                    .isInstanceOf(NullPointerException.class);

            service.setTaxService(new TaxService());
            assertThat(service.total(new BigDecimal("100"), false)).isEqualByComparingTo("120.00");
        }
    }

    @Nested
    @DisplayName("Слайд 95: Service Locator — антипаттерн")
    class ServiceLocator {

        @Test
        @DisplayName("Работает, но зависимость достаётся вручную из контейнера")
        void itWorksButHidesDependencies() {
            ServiceLocatorDemo demo = context.getBean(ServiceLocatorDemo.class);

            assertThat(demo.total(new BigDecimal("100"))).isEqualByComparingTo("120.00");
            assertThat(ServiceLocatorDemo.class.getDeclaredConstructors())
                    .singleElement()
                    .satisfies(c -> assertThat(c.getParameterCount()).isZero());
        }

        @Test
        @DisplayName("Вне контейнера класс неработоспособен в принципе")
        void uselessOutsideTheContainer() {
            ServiceLocatorDemo standalone = new ServiceLocatorDemo();

            assertThat(standalone.worksWithoutContainer()).isFalse();
            assertThatThrownBy(() -> standalone.total(BigDecimal.TEN))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Ошибка «нет бина» вылезает при вызове метода, а не при старте")
        void errorsSurfaceLate() {
            ServiceLocatorDemo demo = context.getBean(ServiceLocatorDemo.class);

            // контекст поднялся успешно, хотя такого бина нет
            assertThatThrownBy(() -> demo.lookup("несуществующийБин"))
                    .isInstanceOf(org.springframework.beans.factory.NoSuchBeanDefinitionException.class);
        }
    }

    @Nested
    @DisplayName("Слайд 96: jakarta-аннотации")
    class Jakarta {

        @Test
        @DisplayName("@Inject работает как @Autowired, @Resource — как @Resource")
        void jakartaAnnotationsAreSupported() {
            JakartaInjected service = context.getBean(JakartaInjected.class);

            assertThat(service.total(new BigDecimal("100"), true)).isEqualByComparingTo("108.00");
        }

        @Test
        @DisplayName("@Named задаёт имя бина")
        void namedSetsTheBeanName() {
            assertThat(context.containsBean("jakartaService")).isTrue();
            assertThat(context.getBean("jakartaService")).isInstanceOf(JakartaInjected.class);
        }

        @Test
        @DisplayName("Класс переносим: он не зависит от аннотаций Spring")
        void classIsPortable() {
            boolean usesSpringAnnotations = java.util.Arrays.stream(JakartaInjected.class.getAnnotations())
                    .anyMatch(a -> a.annotationType().getName().startsWith("org.springframework"));

            assertThat(usesSpringAnnotations)
                    .as("такой класс заработает и в Guice, и в Micronaut")
                    .isFalse();
        }
    }
}
