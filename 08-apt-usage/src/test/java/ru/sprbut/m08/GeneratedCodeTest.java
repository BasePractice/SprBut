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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тест сам по себе — доказательство работы APT: он импортирует классы,
 * которых нет ни в одном файле {@code src}. Если бы процессор не отработал,
 * тест не скомпилировался бы вовсе.
 */
@DisplayName("Слайды 66–70: сгенерированный код — обычный код")
class GeneratedCodeTest {

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

            assertThat(customer.getId()).isEqualTo("C-1");
            assertThat(customer.getName()).isEqualTo("Иванов");
            assertThat(customer.isVip()).isTrue();
            assertThat(customer.getBalance()).isEqualByComparingTo("100.50");
        }

        @Test
        @DisplayName("Незаданные поля остаются со значениями по умолчанию Java")
        void unsetFieldsKeepJavaDefaults() {
            Customer customer = CustomerBuilder.create().id("C-2").build();

            assertThat(customer.getName()).isNull();
            assertThat(customer.getAge()).isZero();
            assertThat(customer.isVip()).isFalse();
        }

        @Test
        @DisplayName("Методы билдера возвращают сам билдер — вызовы цепляются")
        void buildersAreFluent() {
            CustomerBuilder builder = CustomerBuilder.create();

            assertThat(builder.id("C-3")).isSameAs(builder);
            assertThat(builder.name("Петров")).isSameAs(builder);
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

            assertThat(OrderMaker.class.getSimpleName()).isEqualTo("OrderMaker");
            assertThat(order.getNumber()).isEqualTo("ORD-1");
            assertThat(order.getPlacedOn()).isEqualTo(LocalDate.of(2026, 7, 30));
        }

        @Test
        @DisplayName("Билдер финальный, с приватным конструктором и статической фабрикой")
        void builderShapeIsAsGenerated() {
            assertThat(java.lang.reflect.Modifier.isFinal(CustomerBuilder.class.getModifiers())).isTrue();
            assertThat(CustomerBuilder.class.getDeclaredConstructors()).hasSize(1);
            assertThat(java.lang.reflect.Modifier.isPrivate(
                    CustomerBuilder.class.getDeclaredConstructors()[0].getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Статические поля исходного класса в билдер не попали")
        void staticFieldsAreSkipped() {
            assertThat(CustomerBuilder.class.getDeclaredMethods())
                    .extracting(java.lang.reflect.Method::getName)
                    .containsExactlyInAnyOrder("create", "build", "id", "name", "email",
                            "age", "vip", "balance");
        }
    }

    @Nested
    @DisplayName("Реестр, сгенерированный JavaPoet")
    class Registry {

        @Test
        @DisplayName("В реестр попали все три класса с @Registered")
        void containsEveryRegisteredClass() {
            assertThat(ModuleRegistry.names())
                    .containsExactlyInAnyOrder("customers", "orderRepository", "audit");
            assertThat(ModuleRegistry.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("Имя берётся из value, иначе — имя класса с маленькой буквы")
        void namesFollowTheDeclaredRule() {
            assertThat(ModuleRegistry.create("customers")).isInstanceOf(CustomerRepository.class);
            assertThat(ModuleRegistry.create("audit")).isInstanceOf(AuditLog.class);
            assertThat(ModuleRegistry.create("orderRepository")).isInstanceOf(OrderRepository.class);
        }

        @Test
        @DisplayName("Каждый вызов create() даёт новый экземпляр — это фабрика, а не синглтон")
        void createReturnsNewInstances() {
            assertThat(ModuleRegistry.create("audit")).isNotSameAs(ModuleRegistry.create("audit"));
        }

        @Test
        @DisplayName("Неизвестное имя даёт понятную ошибку")
        void unknownNameIsRejected() {
            assertThatThrownBy(() -> ModuleRegistry.create("нет-такого"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("В реестре нет записи: нет-такого");
        }

        @Test
        @DisplayName("Пакет и имя класса заданы опциями -A в pom.xml")
        void packageAndClassNameCameFromProcessorOptions() {
            assertThat(ModuleRegistry.class.getName())
                    .isEqualTo("ru.sprbut.m08.generated.ModuleRegistry");
        }

        @Test
        @DisplayName("Объекты создаются конструктором, а не рефлексией — это работает в native image")
        void createsWithoutReflection() {
            // В сгенерированном коде лежит Xxx::new, а не Class.forName(...).newInstance()
            Object created = ModuleRegistry.create("customers");

            assertThat(created).isNotNull();
            assertThat(((CustomerRepository) created).count()).isZero();
        }
    }

    @Nested
    @DisplayName("Аннотации с retention SOURCE")
    class SourceRetention {

        @Test
        @DisplayName("@GenerateBuilder и @Registered в байткоде отсутствуют")
        void annotationsLeaveNoTraceInBytecode() {
            assertThat(Customer.class.getAnnotations()).isEmpty();
            assertThat(CustomerRepository.class.getAnnotations()).isEmpty();
        }

        @Test
        @DisplayName("Поэтому зависимость на процессор нужна только на этапе компиляции")
        void processorIsCompileTimeOnly() {
            // scope=provided в pom.xml: в runtime-classpath приложения этих классов нет
            assertThat(Customer.class.getDeclaredAnnotations()).isEmpty();
        }
    }
}
