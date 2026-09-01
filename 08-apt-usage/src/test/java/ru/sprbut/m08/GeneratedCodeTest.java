/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m08.generated.ModuleRegistry;
import ru.sprbut.m08.model.Customer;
import ru.sprbut.m08.model.CustomerBuilder;
import ru.sprbut.m08.model.Order;
import ru.sprbut.m08.model.OrderMaker;
import ru.sprbut.m08.service.CustomerRepository;
import ru.sprbut.m08.service.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

/**
 * Тест сам по себе — доказательство работы APT: он импортирует классы,
 * которых нет ни в одном файле {@code src}. Если бы процессор не отработал,
 * тест не скомпилировался бы вовсе.
 * @since 1.0
 */
@DisplayName("Слайды 66–70: сгенерированный код — обычный код")
final class GeneratedCodeTest {

    @Nested
/**
 * Билдеры, сгенерированные @GenerateBuilder.
 * @since 1.0
 */
    @DisplayName("Билдеры, сгенерированные @GenerateBuilder")
    class Builders {

        @Test
        @DisplayName("CustomerBuilder существует и собирает объект")
        void builderBuildsObject() {
            final Customer customer = CustomerBuilder.create()
                    .id("C-1")
                    .name("Иванов")
                    .email("ivanov@mail.ru")
                    .age(42)
                    .vip(true)
                    .balance(new BigDecimal("100.50"))
                    .build();

            MatcherAssert.assertThat(
                "generated builder cannot assemble the object",
                customer.getBalance(),
                Matchers.comparesEqualTo(new BigDecimal("100.50"))
            );
        }

        @Test
        @DisplayName("Незаданные поля остаются со значениями по умолчанию Java")
        void unsetFieldsKeepJavaDefaults() {
            final Customer customer = CustomerBuilder.create().id("C-2").build();

            MatcherAssert.assertThat(
                "unset field cannot keep the Java default",
                customer.getName(),
                Matchers.nullValue()
            );
        }

        @Test
        @DisplayName("Методы билдера возвращают сам билдер — вызовы цепляются")
        void buildersAreFluent() {
            final CustomerBuilder builder = CustomerBuilder.create();

            MatcherAssert.assertThat(
                "builder method cannot return the builder itself",
                builder.id("C-3"),
                Matchers.sameInstance(builder)
            );
        }

        @Test
        @DisplayName("Суффикс имени взят из @GenerateBuilder(suffix = \"Maker\")")
        void suffixComesFromAnnotation() {
            final Order order = OrderMaker.create()
                    .number("ORD-1")
                    .customerId("C-1")
                    .total(new BigDecimal("999"))
                    .placedOn(LocalDate.of(2026, 7, 30))
                    .status("NEW")
                    .build();

            MatcherAssert.assertThat(
                "suffix element cannot rename the generated builder",
                OrderMaker.class.getSimpleName(),
                Matchers.equalTo("OrderMaker")
            );
        }

        @Test
        @DisplayName("билдер с суффиксом собирает объект как обычный")
        void suffixedBuilderWorks() {
            MatcherAssert.assertThat(
                "renamed builder cannot assemble the object",
                OrderMaker.create().number("ORD-1").build().getNumber(),
                Matchers.equalTo("ORD-1")
            );
        }

        @Test
        @DisplayName("Билдер финальный, с приватным конструктором и статической фабрикой")
        void builderShapeIsAsGenerated() {
            MatcherAssert.assertThat(
                "generated builder cannot hide its constructor",
                CustomerBuilder.class.getDeclaredConstructors(),
                Matchers.arrayWithSize(1)
            );
        }

        @Test
        @DisplayName("Статические поля исходного класса в билдер не попали")
        void staticFieldsAreSkipped() {
            MatcherAssert.assertThat(
                "static field cannot stay out of the generated builder",
                java.util.Arrays.stream(CustomerBuilder.class.getDeclaredMethods())
                    .map(java.lang.reflect.Method::getName).toList(),
                Matchers.containsInAnyOrder("create", "build", "id", "name", "email",
                    "age", "vip", "balance")
            );
        }
    }

    @Nested
/**
 * Реестр, сгенерированный JavaPoet.
 * @since 1.0
 */
    @DisplayName("Реестр, сгенерированный JavaPoet")
    class Registry {

        @Test
        @DisplayName("В реестр попали все три класса с @Registered")
        void containsEveryRegisteredClass() {
            MatcherAssert.assertThat(
                "registry cannot contain every annotated class",
                ModuleRegistry.names(),
                Matchers.containsInAnyOrder("customers", "orderRepository", "audit")
            );
        }

        @Test
        @DisplayName("Имя берётся из value, иначе — имя класса с маленькой буквы")
        void namesFollowTheDeclaredRule() {
            MatcherAssert.assertThat(
                "explicit name cannot map to its own class",
                ModuleRegistry.create("customers"),
                Matchers.instanceOf(CustomerRepository.class)
            );
        }

        @Test
        @DisplayName("без value имя выводится из имени класса")
        void derivesNameFromClass() {
            MatcherAssert.assertThat(
                "default name cannot map to its own class",
                ModuleRegistry.create("orderRepository"),
                Matchers.instanceOf(OrderRepository.class)
            );
        }

        @Test
        @DisplayName("Каждый вызов create() даёт новый экземпляр — это фабрика, а не синглтон")
        void createReturnsNewInstances() {
            MatcherAssert.assertThat(
                "factory cannot produce a new instance each time",
                ModuleRegistry.create("audit"),
                Matchers.not(Matchers.sameInstance(ModuleRegistry.create("audit")))
            );
        }

        @Test
        @DisplayName("Неизвестное имя даёт понятную ошибку")
        void unknownNameIsRejected() {
            MatcherAssert.assertThat(
                "unknown name cannot be reported clearly",
                Assertions.assertThrows(
                    IllegalArgumentException.class, () -> ModuleRegistry.create("нет-такого")
                ).getMessage(),
                Matchers.equalTo("В реестре нет записи: нет-такого")
            );
        }

        @Test
        @DisplayName("Пакет и имя класса заданы опциями -A в pom.xml")
        void packageAndClassNameCameFromProcessorOptions() {
            MatcherAssert.assertThat(
                "processor options cannot define the generated class name",
                ModuleRegistry.class.getName(),
                Matchers.equalTo("ru.sprbut.m08.generated.ModuleRegistry")
            );
        }

        @Test
        @DisplayName("Объекты создаются конструктором, а не рефлексией — это работает в native image")
        void createsWithoutReflection() {
            // В сгенерированном коде лежит Xxx::new, а не Class.forName(...).newInstance()
            final Object created = ModuleRegistry.create("customers");

            MatcherAssert.assertThat(
                "generated factory cannot create the object without reflection",
                ((CustomerRepository) created).count(),
                Matchers.equalTo(0)
            );
        }
    }

    @Nested
/**
 * Аннотации с retention SOURCE.
 * @since 1.0
 */
    @DisplayName("Аннотации с retention SOURCE")
    class SourceRetention {

        @Test
        @DisplayName("@GenerateBuilder и @Registered в байткоде отсутствуют")
        void annotationsLeaveNoTraceInBytecode() {
            MatcherAssert.assertThat(
                "source retained annotation cannot vanish from the bytecode",
                Customer.class.getAnnotations(),
                Matchers.emptyArray()
            );
        }

        @Test
        @DisplayName("Поэтому зависимость на процессор нужна только на этапе компиляции")
        void processorIsCompileTimeOnly() {
            // scope=provided в pom.xml: в runtime-classpath приложения этих классов нет
            MatcherAssert.assertThat(
                "processor dependency cannot stay compile time only",
                Customer.class.getDeclaredAnnotations(),
                Matchers.emptyArray()
            );
        }
    }
}
