package ru.sprbut.m02.modern;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд 19: record и Builder против избыточности и мутабельности")
final class ModernAlternativesTest {

    @Test
    @DisplayName("record даёт equals бесплатно — сравнение по значению, а не по ссылке")
    void comparesRecordsByValue() {
        assertThat(
            "record cannot compare by value",
            new CustomerRecord("C-1", "Иван", "Иванов", 42, true),
            equalTo(new CustomerRecord("C-1", "Иван", "Иванов", 42, true))
        );
    }

    @Test
    @DisplayName("hashCode согласован с equals — иначе record был бы бесполезен в Set")
    void keepsHashCodeConsistent() {
        assertThat(
            "record hash code cannot match an equal instance",
            new CustomerRecord("C-1", "Иван", "Иванов", 42, true).hashCode(),
            equalTo(new CustomerRecord("C-1", "Иван", "Иванов", 42, true).hashCode())
        );
    }

    @Test
    @DisplayName("toString печатает состояние, а не адрес объекта")
    void printsState() {
        assertThat(
            "record toString cannot print the state",
            new CustomerRecord("C-1", "Иван", "Иванов", 42, true).toString(),
            containsString("C-1")
        );
    }

    @Test
    @DisplayName("компактный конструктор валидирует объект один раз — при создании")
    void validatesOnConstruction() {
        assertThat(
            "compact constructor cannot reject an empty id",
            assertThrows(
                IllegalArgumentException.class,
                () -> new CustomerRecord("", "Иван", "Иванов", 42, false)
            ).getMessage(),
            containsString("id обязателен")
        );
    }

    @Test
    @DisplayName("отрицательный возраст тоже отбивается на входе")
    void rejectsNegativeAge() {
        assertThat(
            "compact constructor cannot reject a negative age",
            assertThrows(
                IllegalArgumentException.class,
                () -> new CustomerRecord("C-1", "Иван", "Иванов", -1, false)
            ).getMessage(),
            containsString("отрицательным")
        );
    }

    @Test
    @DisplayName("изменение неизменяемого объекта даёт новый объект")
    void createsNewInstanceOnChange() {
        CustomerRecord original = new CustomerRecord("C-1", "Иван", "Иванов", 42, false);
        assertThat(
            "with-method cannot produce a new instance",
            original.withVip(true),
            not(sameInstance(original))
        );
    }

    @Test
    @DisplayName("исходный объект при этом не меняется")
    void keepsOriginalUntouched() {
        CustomerRecord original = new CustomerRecord("C-1", "Иван", "Иванов", 42, false);
        original.withVip(true);
        assertThat(
            "original record cannot stay untouched",
            original.vip(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("Builder собирает объект по частям")
    void buildsInParts() {
        assertThat(
            "builder cannot assemble the object part by part",
            ImmutableCustomer.builder()
                .id("C-1").firstName("Иван").lastName("Иванов").age(42).vip(true)
                .tags(List.of("gold"))
                .build()
                .getId(),
            equalTo("C-1")
        );
    }

    @Test
    @DisplayName("собранная коллекция неизменяема")
    void keepsCollectionImmutable() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> ImmutableCustomer.builder().id("C-1").tags(List.of("gold")).build()
                .getTags().add("hack")
        );
    }

    @Test
    @DisplayName("защитная копия: правка исходного списка после build() ничего не меняет")
    void copiesCollectionDefensively() {
        List<String> mutable = new ArrayList<>(List.of("gold"));
        ImmutableCustomer customer =
            ImmutableCustomer.builder().id("C-1").tags(mutable).build();
        mutable.add("platinum");
        assertThat(
            "builder cannot copy the collection defensively",
            customer.getTags(),
            contains("gold")
        );
    }

    @Test
    @DisplayName("валидация внутри builder срабатывает до сборки объекта")
    void validatesEagerly() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ImmutableCustomer.builder().age(-5)
        );
    }

    @Test
    @DisplayName("обязательное поле проверяется в момент build()")
    void demandsMandatoryField() {
        assertThat(
            "builder cannot demand the mandatory field",
            assertThrows(
                NullPointerException.class,
                () -> ImmutableCustomer.builder().build()
            ).getMessage(),
            containsString("id обязателен")
        );
    }

    @Test
    @DisplayName("toBuilder() переносит состояние и даёт изменить одно поле")
    void copiesStateIntoBuilder() {
        assertThat(
            "toBuilder cannot carry the state over",
            ImmutableCustomer.builder()
                .id("C-1").firstName("Иван").age(42).tags(List.of("gold")).build()
                .toBuilder().vip(true).build()
                .getFirstName(),
            equalTo("Иван")
        );
    }

    @Test
    @DisplayName("исходный объект после toBuilder() остаётся прежним")
    void keepsSourceUnchanged() {
        ImmutableCustomer original =
            ImmutableCustomer.builder().id("C-1").firstName("Иван").age(42).build();
        original.toBuilder().vip(true).build();
        assertThat(
            "source object cannot survive toBuilder unchanged",
            original.isVip(),
            equalTo(false)
        );
    }
}
