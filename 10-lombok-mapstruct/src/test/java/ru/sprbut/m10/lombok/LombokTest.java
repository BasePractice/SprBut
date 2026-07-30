package ru.sprbut.m10.lombok;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 79: Lombok — генератор, который меняет существующий класс")
class LombokTest {

    private CustomerEntity entity() {
        return new CustomerEntity("C-1", "Иван", "Иванов",
                LocalDate.of(1984, 3, 15), new BigDecimal("100.00"), true, "служебное");
    }

    @Nested
    @DisplayName("@Data и @Value")
    class DataAndValue {

        @Test
        @DisplayName("В исходнике методов нет, в байткоде они есть")
        void methodsExistOnlyInBytecode() {
            assertThat(LombokUnderTheHood.generatedMethodNames(CustomerEntity.class))
                    .contains("getId", "setId", "getFirstName", "setFirstName", "isVip", "setVip")
                    .contains("equals", "hashCode", "toString");
        }

        @Test
        @DisplayName("@NoArgsConstructor и @AllArgsConstructor дают два конструктора")
        void bothConstructorsAreGenerated() {
            assertThat(LombokUnderTheHood.constructorArities(CustomerEntity.class))
                    .containsExactly(0, 7);
        }

        @Test
        @DisplayName("equals/hashCode/toString действительно работают")
        void valueSemanticsWork() {
            assertThat(entity()).isEqualTo(entity()).hasSameHashCodeAs(entity());
            assertThat(entity().toString()).contains("C-1", "Иван");
        }

        @Test
        @DisplayName("@Value делает все поля final и убирает сеттеры")
        void valueIsImmutable() {
            assertThat(LombokUnderTheHood.allFieldsAreFinal(CustomerDto.class)).isTrue();
            assertThat(LombokUnderTheHood.hasSetters(CustomerDto.class)).isFalse();
            assertThat(java.lang.reflect.Modifier.isFinal(CustomerDto.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("@Builder(toBuilder = true) даёт сборку и «изменение» неизменяемого объекта")
        void builderWorks() {
            CustomerDto dto = CustomerDto.builder()
                    .id("C-1")
                    .fullName("Иван Иванов")
                    .age(42)
                    .balance(BigDecimal.TEN)
                    .status("VIP")
                    .build();

            CustomerDto downgraded = dto.toBuilder().status("STANDARD").build();

            assertThat(dto.getStatus()).isEqualTo("VIP");
            assertThat(downgraded.getStatus()).isEqualTo("STANDARD");
            assertThat(downgraded.getFullName()).isEqualTo("Иван Иванов");
        }

        @Test
        @DisplayName("@Data остаётся JavaBean, поэтому совместим со Spring и Hibernate")
        void dataStaysAJavaBean() {
            assertThat(LombokUnderTheHood.looksLikeJavaBean(CustomerEntity.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Точечные аннотации")
    class Targeted {

        @Test
        @DisplayName("@Setter(AccessLevel.PROTECTED) меняет видимость сгенерированного метода")
        void accessLevelIsRespected() {
            assertThat(LombokUnderTheHood.accessLevelOf(LombokFeatures.Partial.class, "getVisible"))
                    .isEqualTo("public");
            assertThat(LombokUnderTheHood.accessLevelOf(LombokFeatures.Partial.class, "setVisible"))
                    .isEqualTo("protected");
        }

        @Test
        @DisplayName("@Getter(AccessLevel.NONE) отменяет генерацию для отдельного поля")
        void noneSuppressesGeneration() {
            assertThat(LombokUnderTheHood.generatedMethodNames(LombokFeatures.Partial.class))
                    .doesNotContain("getHidden");
            assertThat(new LombokFeatures.Partial().peekHidden()).isEqualTo("не виден снаружи");
        }

        @Test
        @DisplayName("@RequiredArgsConstructor берёт только final-поля")
        void requiredArgsUsesFinalFieldsOnly() {
            assertThat(LombokUnderTheHood.constructorArities(LombokFeatures.Service.class))
                    .containsExactly(2);

            LombokFeatures.Service service = new LombokFeatures.Service("платежи", 3);
            assertThat(service.getName()).isEqualTo("платежи");
            assertThat(service.getMutableState()).isEqualTo("меняется");
        }

        @Test
        @DisplayName("@ToString(exclude) прячет поле, @EqualsAndHashCode(of) сужает сравнение")
        void toStringAndEqualsCanBeNarrowed() {
            var a = new LombokFeatures.Account("A-1", "ivanov", "секрет");
            var b = new LombokFeatures.Account("A-1", "другой-логин", "другой-пароль");

            assertThat(a.toString()).contains("ivanov").doesNotContain("секрет");
            assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        }

        @Test
        @DisplayName("@Singular наполняет коллекцию по одному и делает её неизменяемой")
        void singularBuildsImmutableCollection() {
            LombokFeatures.Order order = LombokFeatures.Order.builder()
                    .number("ORD-1")
                    .item("молоко")
                    .item("хлеб")
                    .build();

            assertThat(order.getItems()).containsExactly("молоко", "хлеб");
            assertThatThrownBy(() -> order.getItems().add("взлом"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("@Accessors(fluent) удобен, но ломает соглашение JavaBeans")
        void fluentAccessorsBreakTheConvention() {
            LombokFeatures.Fluent fluent = new LombokFeatures.Fluent().name("тест").size(3);

            assertThat(fluent.name()).isEqualTo("тест");
            assertThat(fluent.size()).isEqualTo(3);

            assertThat(LombokUnderTheHood.looksLikeJavaBean(LombokFeatures.Fluent.class))
                    .as("нет getXxx — Introspector и биндинг Spring такой класс не увидят")
                    .isFalse();
        }
    }
}
