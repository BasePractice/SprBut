package ru.sprbut.m08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m08.generated.ModuleRegistry;
import ru.sprbut.m08.model.Customer;
import ru.sprbut.m08.model.CustomerBuilder;
import ru.sprbut.m08.model.Order;
import ru.sprbut.m08.model.OrderMaker;
import ru.sprbut.m08.service.AuditLog;
import ru.sprbut.m08.service.CustomerRepository;
import ru.sprbut.m08.service.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тест сам по себе — доказательство работы APT: он импортирует классы,
 * которых нет ни в одном файле {@code src}. Если бы процессор не отработал,
 * тест не скомпилировался бы вовсе.
 */
@DisplayName("Слайды 66–70: сгенерированный код — обычный код")
final class GeneratedCodeTest {

    @Nested
    @DisplayName("Билдеры, сгенерированные @GenerateBuilder")
    class Builders {

        @Test
        @DisplayName("CustomerBuilder существует и собирает объект")
        void builderBuildsObject() {
            Customer customer = CustomerBuilder.create()
                    .id("C-1")
                    .name("Иванов")
                    .email("ivanov@mail.ru")
                    .age(42)
                    .vip(true)
                    .balance(new BigDecimal("100.50"))
                    .build();

            assertThat(
                "generated builder cannot assemble the object",
                customer.getBalance(),
                comparesEqualTo(new BigDecimal("100.50"))
            );
        }

        @Test
        @DisplayName("Незаданные поля остаются со значениями по умолчанию Java")
        void unsetFieldsKeepJavaDefaults() {
            Customer customer = CustomerBuilder.create().id("C-2").build();

            assertThat(
                "unset field cannot keep the Java default",
                customer.getName(),
                nullValue()
            );
        }

        @Test
        @DisplayName("Методы билдера возвращают сам билдер — вызовы цепляются")
        void buildersAreFluent() {
            CustomerBuilder builder = CustomerBuilder.create();

            assertThat(
                "builder method cannot return the builder itself",
                builder.id("C-3"),
                sameInstance(builder)
            );
        }

        @Test
        @DisplayName("Суффикс имени взят из @GenerateBuilder(suffix = \"Maker\")")
        void suffixComesFromAnnotation() {
            Order order = OrderMaker.create()
                    .number("ORD-1")
                    .customerId("C-1")
                    .total(new BigDecimal("999"))
                    .placedOn(LocalDate.of(2026, 7, 30))
                    .status("NEW")
                    .build();

            assertThat(
                "suffix element cannot rename the generated builder",
                OrderMaker.class.getSimpleName(),
                equalTo("OrderMaker")
            );
        }

        @Test
        @DisplayName("билдер с суффиксом собирает объект как обычный")
        void suffixedBuilderWorks() {
            assertThat(
                "renamed builder cannot assemble the object",
                OrderMaker.create().number("ORD-1").build().getNumber(),
                equalTo("ORD-1")
            );
        }

        @Test
        @DisplayName("Билдер финальный, с приватным конструктором и статической фабрикой")
        void builderShapeIsAsGenerated() {
            assertThat(
                "generated builder cannot hide its constructor",
                CustomerBuilder.class.getDeclaredConstructors(),
                arrayWithSize(1)
            );
        }

        @Test
        @DisplayName("Статические поля исходного класса в билдер не попали")
        void staticFieldsAreSkipped() {
            assertThat(
                "static field cannot stay out of the generated builder",
                java.util.Arrays.stream(CustomerBuilder.class.getDeclaredMethods())
                    .map(java.lang.reflect.Method::getName).toList(),
                containsInAnyOrder("create", "build", "id", "name", "email",
                    "age", "vip", "balance")
            );
        }
    }

    @Nested
    @DisplayName("Реестр, сгенерированный JavaPoet")
    class Registry {

        @Test
        @DisplayName("В реестр попали все три класса с @Registered")
        void containsEveryRegisteredClass() {
            assertThat(
                "registry cannot contain every annotated class",
                ModuleRegistry.names(),
                containsInAnyOrder("customers", "orderRepository", "audit")
            );
        }

        @Test
        @DisplayName("Имя берётся из value, иначе — имя класса с маленькой буквы")
        void namesFollowTheDeclaredRule() {
            assertThat(
                "explicit name cannot map to its own class",
                ModuleRegistry.create("customers"),
                instanceOf(CustomerRepository.class)
            );
        }

        @Test
        @DisplayName("без value имя выводится из имени класса")
        void derivesNameFromClass() {
            assertThat(
                "default name cannot map to its own class",
                ModuleRegistry.create("orderRepository"),
                instanceOf(OrderRepository.class)
            );
        }

        @Test
        @DisplayName("Каждый вызов create() даёт новый экземпляр — это фабрика, а не синглтон")
        void createReturnsNewInstances() {
            assertThat(
                "factory cannot produce a new instance each time",
                ModuleRegistry.create("audit"),
                not(sameInstance(ModuleRegistry.create("audit")))
            );
        }

        @Test
        @DisplayName("Неизвестное имя даёт понятную ошибку")
        void unknownNameIsRejected() {
            assertThat(
                "unknown name cannot be reported clearly",
                assertThrows(
                    IllegalArgumentException.class, () -> ModuleRegistry.create("нет-такого")
                ).getMessage(),
                equalTo("В реестре нет записи: нет-такого")
            );
        }

        @Test
        @DisplayName("Пакет и имя класса заданы опциями -A в pom.xml")
        void packageAndClassNameCameFromProcessorOptions() {
            assertThat(
                "processor options cannot define the generated class name",
                ModuleRegistry.class.getName(),
                equalTo("ru.sprbut.m08.generated.ModuleRegistry")
            );
        }

        @Test
        @DisplayName("Объекты создаются конструктором, а не рефлексией — это работает в native image")
        void createsWithoutReflection() {
            // В сгенерированном коде лежит Xxx::new, а не Class.forName(...).newInstance()
            Object created = ModuleRegistry.create("customers");

            assertThat(
                "generated factory cannot create the object without reflection",
                ((CustomerRepository) created).count(),
                equalTo(0)
            );
        }
    }

    @Nested
    @DisplayName("Аннотации с retention SOURCE")
    class SourceRetention {

        @Test
        @DisplayName("@GenerateBuilder и @Registered в байткоде отсутствуют")
        void annotationsLeaveNoTraceInBytecode() {
            assertThat(
                "source retained annotation cannot vanish from the bytecode",
                Customer.class.getAnnotations(),
                emptyArray()
            );
        }

        @Test
        @DisplayName("Поэтому зависимость на процессор нужна только на этапе компиляции")
        void processorIsCompileTimeOnly() {
            // scope=provided в pom.xml: в runtime-classpath приложения этих классов нет
            assertThat(
                "processor dependency cannot stay compile time only",
                Customer.class.getDeclaredAnnotations(),
                emptyArray()
            );
        }
    }
}
