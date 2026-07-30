package ru.sprbut.m05.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Расширенный пример: валидация на собственных аннотациях")
final class ValidatedTest {

    @Test
    @DisplayName("корректный объект нарушений не даёт")
    void acceptsValidObject() {
        assertThat(
            "valid object cannot pass without violations",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru")).verdict().valid(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("маркерная @NotBlank срабатывает на пробелах")
    void rejectsBlankValue() {
        assertThat(
            "marker annotation cannot reject a blank value",
            new Validated(new User("U-1", "   ", 33, "ivan@mail.ru")).verdict().fields(),
            contains("login")
        );
    }

    @Test
    @DisplayName("@MaxLength(10) читается без имени элемента")
    void appliesSingleValueAnnotation() {
        assertThat(
            "single value annotation cannot limit the length",
            new Validated(new User("U-1", "оченьдлинныйлогин", 33, "ivan@mail.ru"))
                .verdict().messages().toString(),
            containsString("превышает максимум 10")
        );
    }

    @Test
    @DisplayName("@Range использует заданные значения элементов")
    void appliesExplicitRange() {
        assertThat(
            "explicit range values cannot be applied",
            new Validated(new User("U-1", "ivan", 5, "ivan@mail.ru"))
                .verdict().messages().toString(),
            containsString("возраст вне диапазона")
        );
    }

    @Test
    @DisplayName("повторяемая @Matches проверяет все шаблоны, а не первый")
    void checksEveryRepeatedPattern() {
        assertThat(
            "repeatable annotation cannot check every pattern",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail")).verdict().messages(),
            hasItem(containsString("нет доменной зоны"))
        );
    }

    @Test
    @DisplayName("ограничения родительского класса действуют на наследника")
    void collectsConstraintsUpTheHierarchy() {
        assertThat(
            "parent constraints cannot reach the child",
            new Validated(new User(null, "ivan", 33, "ivan@mail.ru")).verdict().fields(),
            contains("id")
        );
    }

    @Test
    @DisplayName("поле без ограничений не проверяется вовсе")
    void dontCheckUnconstrainedField() {
        assertThat(
            "unconstrained field cannot stay out of the report",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru", null, null))
                .verdict().fields(),
            not(hasItem("free"))
        );
    }

    @Test
    @DisplayName("ограничение с retention CLASS движок не видит — оно молча не работает")
    void dontSeeClassRetainedConstraint() {
        assertThat(
            "class retained constraint cannot stay invisible",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru", null, "текст"))
                .verdict().fields(),
            not(hasItem("invisible"))
        );
    }

    @Test
    @DisplayName("вердикт собирает все нарушения разом, а не первое")
    void collectsAllViolations() {
        assertThat(
            "verdict cannot collect every violation at once",
            new Validated(new User(null, "", 5, "нет")).verdict().violations(),
            hasSize(5)
        );
    }

    @Test
    @DisplayName("режим «падать сразу» приносит с собой полный отчёт")
    void failsFastWithFullReport() {
        assertThat(
            "fail fast mode cannot carry the whole report",
            assertThrows(
                ConstraintsViolated.class,
                () -> new Validated(new User(null, "", 5, "нет")).check()
            ).verdict().violations(),
            hasSize(5)
        );
    }

    @Test
    @DisplayName("корректный объект режим «падать сразу» пропускает молча")
    void passesValidObjectSilently() {
        new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru")).check();
        assertThat(
            "valid object cannot pass the fail fast check silently",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru")).verdict().messages(),
            hasSize(0)
        );
    }
}
