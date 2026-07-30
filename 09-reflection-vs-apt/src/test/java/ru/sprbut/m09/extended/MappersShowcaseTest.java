package ru.sprbut.m09.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m09.model.UserEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@DisplayName("Расширенный пример: СХЕМА 4 — compile-time против runtime")
final class MappersShowcaseTest {

    private static UserEntity entity() {
        return new UserEntity("U-1", "Иван", "Иванов", 42, true);
    }

    @Test
    @DisplayName("механизм не меняет поведение — только свойства")
    void agreeOnResult() {
        assertThat(
            "three mechanisms cannot agree on the result",
            new Mappers().agree(entity()),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("реализаций ровно три — по одной на механизм со слайда")
    void coversThreeMechanisms() {
        assertThat(
            "showcase cannot cover all three mechanisms",
            new Mappers().list(),
            hasSize(3)
        );
    }

    @Test
    @DisplayName("каждая реализация объявляет свою стратегию")
    void namesEveryStrategy() {
        assertThat(
            "every mapper cannot name its own strategy",
            new Mappers().strategies(),
            hasSize(3)
        );
    }

    @Test
    @DisplayName("замер даёт число для каждой реализации")
    void measuresEveryMapper() {
        assertThat(
            "benchmark cannot measure the reflective mapper",
            new Benchmark(new Mappers(), entity()).timings(100),
            hasKey("ReflectiveMapper")
        );
    }

    @Test
    @DisplayName("рефлексии для native image нужны подсказки на каждое свойство")
    void demandsHintsForReflection() {
        assertThat(
            "reflective mapper cannot demand runtime hints",
            new RequiredHints().accessors(),
            not(emptyIterable())
        );
    }

    @Test
    @DisplayName("подсказка называет конкретный метод, а не класс целиком")
    void namesExactAccessor() {
        assertThat(
            "hint cannot name the exact accessor",
            new RequiredHints().accessors(),
            hasItem("UserEntity#getFirstName")
        );
    }

    @Test
    @DisplayName("сгенерированному коду подсказки не нужны вовсе")
    void demandsNoHintsForGeneratedCode() {
        assertThat(
            "generated mapper cannot avoid runtime hints entirely",
            new RequiredHints().byMapper().get("GeneratedStyleMapper"),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("для байткода вопрос подсказок не имеет смысла — класса ещё нет")
    void reportsBytecodeAsInapplicable() {
        assertThat(
            "bytecode mapper cannot be reported as inapplicable for native image",
            new RequiredHints().byMapper().get("BytecodeMapper"),
            hasSize(1)
        );
    }
}
