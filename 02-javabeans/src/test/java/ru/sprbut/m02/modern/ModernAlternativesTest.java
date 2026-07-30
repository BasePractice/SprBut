package ru.sprbut.m02.modern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 19: record и Builder против избыточности и мутабельности")
class ModernAlternativesTest {

    @Test
    @DisplayName("record даёт equals/hashCode/toString бесплатно")
    void recordGeneratesValueSemantics() {
        CustomerRecord a = new CustomerRecord("C-1", "Иван", "Иванов", 42, true);
        CustomerRecord b = new CustomerRecord("C-1", "Иван", "Иванов", 42, true);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).contains("C-1", "Иван");
    }

    @Test
    @DisplayName("Компактный конструктор валидирует объект один раз — при создании")
    void recordValidatesOnConstruction() {
        assertThatThrownBy(() -> new CustomerRecord("", "Иван", "Иванов", 42, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id обязателен");

        assertThatThrownBy(() -> new CustomerRecord("C-1", "Иван", "Иванов", -1, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("отрицательным");
    }

    @Test
    @DisplayName("Изменение неизменяемого объекта — это новый объект")
    void withCreatesNewInstance() {
        CustomerRecord original = new CustomerRecord("C-1", "Иван", "Иванов", 42, false);
        CustomerRecord vip = original.withVip(true);

        assertThat(original.vip()).isFalse();
        assertThat(vip.vip()).isTrue();
        assertThat(vip).isNotSameAs(original);
        assertThat(vip.fullName()).isEqualTo("Иван Иванов");
    }

    @Test
    @DisplayName("Builder собирает объект по частям, результат остаётся неизменяемым")
    void builderProducesImmutableObject() {
        ImmutableCustomer customer = ImmutableCustomer.builder()
                .id("C-1")
                .firstName("Иван")
                .lastName("Иванов")
                .age(42)
                .vip(true)
                .tags(List.of("gold"))
                .build();

        assertThat(customer.getId()).isEqualTo("C-1");
        assertThat(customer.getTags()).containsExactly("gold");
        assertThatThrownBy(() -> customer.getTags().add("hack"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Защитная копия: изменение исходного списка после build() ничего не меняет")
    void builderCopiesCollections() {
        List<String> mutable = new ArrayList<>(List.of("gold"));
        ImmutableCustomer customer = ImmutableCustomer.builder().id("C-1").tags(mutable).build();

        mutable.add("platinum");

        assertThat(customer.getTags()).containsExactly("gold");
    }

    @Test
    @DisplayName("Валидация внутри builder срабатывает до сборки объекта")
    void builderValidatesEagerly() {
        assertThatThrownBy(() -> ImmutableCustomer.builder().age(-5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ImmutableCustomer.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id обязателен");
    }

    @Test
    @DisplayName("toBuilder() даёт удобный аналог 'изменить одно поле'")
    void toBuilderCopiesState() {
        ImmutableCustomer original = ImmutableCustomer.builder()
                .id("C-1").firstName("Иван").age(42).tags(List.of("gold")).build();

        ImmutableCustomer promoted = original.toBuilder().vip(true).build();

        assertThat(promoted.getFirstName()).isEqualTo("Иван");
        assertThat(promoted.getTags()).containsExactly("gold");
        assertThat(promoted.isVip()).isTrue();
        assertThat(original.isVip()).isFalse();
    }
}
