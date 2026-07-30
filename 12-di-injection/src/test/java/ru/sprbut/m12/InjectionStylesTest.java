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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
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

            assertThat(
                "cannot verify that results are identical",
                context.getBean(ConstructorInjected.class).total(net, true),
                comparesEqualTo(new java.math.BigDecimal("108.00"))
            );
            assertThat(
                "cannot verify that results are identical",
                context.getBean(SetterInjected.class).total(net, true),
                comparesEqualTo(new java.math.BigDecimal("108.00"))
            );
            assertThat(
                "cannot verify that results are identical",
                context.getBean(FieldInjected.class).total(net, true),
                comparesEqualTo(new java.math.BigDecimal("108.00"))
            );
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

            assertThat(
                "cannot verify that testable without container",
                service.total(new BigDecimal("100"), false),
                comparesEqualTo(new java.math.BigDecimal("120.00"))
            );
        }

        @Test
        @DisplayName("Поля final — объект неизменяем по построению")
        void fieldsAreFinal() {
            assertThat(
                "cannot verify that fields are final",
                java.util.Arrays.stream(ConstructorInjected.class.getDeclaredFields()).allMatch(f -> java.lang.reflect.Modifier.isFinal(f.getModifiers())),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("Зависимости обязательны: другого конструктора просто нет")
        void dependenciesAreMandatory() {
            assertThat(
                "constructor cannot make the dependencies mandatory",
                ConstructorInjected.class.getDeclaredConstructors()[0].getParameterCount(),
                equalTo(2)
            );
        }

        @Test
        @DisplayName("@Autowired на единственном конструкторе не нужен со Spring 4.3")
        void autowiredIsOptionalOnSingleConstructor() {
            assertThat(
                "cannot verify that autowired is optional on single constructor",
                ConstructorInjected.class.getDeclaredConstructors()[0] .isAnnotationPresent(org.springframework.beans.factory.annotation.Autowired.class),
                equalTo(false)
            );
            assertThat(
                "cannot verify that autowired is optional on single constructor",
                context.getBean(ConstructorInjected.class),
                notNullValue()
            );
        }
    }

    @Nested
    @DisplayName("Слайд 93: внедрение в поле мешает тестам без контейнера")
    class FieldInjectionProblems {

        @Test
        @DisplayName("Собранный руками объект падает: поля остались null")
        void newInstanceIsBroken() {
            FieldInjected service = new FieldInjected();

            assertThrows(NullPointerException.class, () -> service.total(new BigDecimal("100"), false));
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

            assertThat(
                "cannot verify that only reflection can fill the fields",
                service.total(new BigDecimal("100"), false),
                comparesEqualTo(new java.math.BigDecimal("120.00"))
            );
        }

        @Test
        @DisplayName("Зависимости не видны в API: конструктор пустой")
        void dependenciesAreInvisible() {
            assertThat(
                "field injected class cannot hide its dependencies from the constructor",
                FieldInjected.class.getDeclaredConstructors()[0].getParameterCount(),
                equalTo(0)
            );
        }

        @Test
        @DisplayName("Поля не могут быть final")
        void fieldsCannotBeFinal() {
            assertThat(
                "cannot verify that fields cannot be final",
                java.util.Arrays.stream(FieldInjected.class.getDeclaredFields()).anyMatch(f -> java.lang.reflect.Modifier.isFinal(f.getModifiers())),
                equalTo(false)
            );
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

                assertThat(
                    "cannot verify that optional dependency may be absent",
                    service.hasDiscountService(),
                    equalTo(false)
                );
                assertThat(
                    "cannot verify that optional dependency may be absent",
                    service.total(new BigDecimal("100"), true),
                    comparesEqualTo(new java.math.BigDecimal("120.00"))
                );
            }
        }

        @Test
        @DisplayName("Когда бин есть, он внедряется")
        void optionalDependencyIsInjectedWhenPresent() {
            assertThat(
                "cannot verify that optional dependency is injected when present",
                context.getBean(SetterInjected.class).hasDiscountService(),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("Между new и вызовом сеттера объект невалиден")
        void objectIsInvalidBetweenNewAndSetter() {
            SetterInjected service = new SetterInjected();

            assertThrows(NullPointerException.class, () -> service.total(BigDecimal.TEN, false));

            service.setTaxService(new TaxService());
            assertThat(
                "cannot verify that object is invalid between new and setter",
                service.total(new BigDecimal("100"), false),
                comparesEqualTo(new java.math.BigDecimal("120.00"))
            );
        }
    }

    @Nested
    @DisplayName("Слайд 95: Service Locator — антипаттерн")
    class ServiceLocator {

        @Test
        @DisplayName("Работает, но зависимость достаётся вручную из контейнера")
        void itWorksButHidesDependencies() {
            ServiceLocatorDemo demo = context.getBean(ServiceLocatorDemo.class);

            assertThat(
                "cannot verify that it works but hides dependencies",
                demo.total(new BigDecimal("100")),
                comparesEqualTo(new java.math.BigDecimal("120.00"))
            );
            assertThat(
                "service locator cannot hide its dependencies from the constructor",
                ServiceLocatorDemo.class.getDeclaredConstructors()[0].getParameterCount(),
                equalTo(0)
            );
        }

        @Test
        @DisplayName("Вне контейнера класс неработоспособен в принципе")
        void uselessOutsideTheContainer() {
            ServiceLocatorDemo standalone = new ServiceLocatorDemo();

            assertThat(
                "cannot verify that useless outside the container",
                standalone.worksWithoutContainer(),
                equalTo(false)
            );
            assertThrows(NullPointerException.class, () -> standalone.total(BigDecimal.TEN));
        }

        @Test
        @DisplayName("Ошибка «нет бина» вылезает при вызове метода, а не при старте")
        void errorsSurfaceLate() {
            ServiceLocatorDemo demo = context.getBean(ServiceLocatorDemo.class);

            // контекст поднялся успешно, хотя такого бина нет
            assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, () -> demo.lookup("несуществующийБин"));
        }
    }

    @Nested
    @DisplayName("Слайд 96: jakarta-аннотации")
    class Jakarta {

        @Test
        @DisplayName("@Inject работает как @Autowired, @Resource — как @Resource")
        void jakartaAnnotationsAreSupported() {
            JakartaInjected service = context.getBean(JakartaInjected.class);

            assertThat(
                "cannot verify that jakarta annotations are supported",
                service.total(new BigDecimal("100"), true),
                comparesEqualTo(new java.math.BigDecimal("108.00"))
            );
        }

        @Test
        @DisplayName("@Named задаёт имя бина")
        void namedSetsTheBeanName() {
            assertThat(
                "cannot verify that named sets the bean name",
                context.containsBean("jakartaService"),
                equalTo(true)
            );
            assertThat(
                "cannot verify that named sets the bean name",
                context.getBean("jakartaService"),
                instanceOf(JakartaInjected.class)
            );
        }

        @Test
        @DisplayName("Класс переносим: он не зависит от аннотаций Spring")
        void classIsPortable() {
            boolean usesSpringAnnotations = java.util.Arrays.stream(JakartaInjected.class.getAnnotations())
                    .anyMatch(a -> a.annotationType().getName().startsWith("org.springframework"));

            assertThat(
                "portable class cannot stay free of Spring annotations",
                usesSpringAnnotations,
                equalTo(false)
            );
        }
    }
}
