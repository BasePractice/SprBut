package ru.sprbut.m05.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Расширенный пример: валидация на собственных аннотациях")
class ValidationEngineTest {

    /** Базовый класс: его ограничения должны действовать и на наследниках. */
    @SuppressWarnings("unused")
    static class BaseEntity {

        @Constraints.NotBlank
        String id;

        BaseEntity(String id) {
            this.id = id;
        }
    }

    @SuppressWarnings("unused")
    static class User extends BaseEntity {

        @Constraints.NotBlank
        @Constraints.MaxLength(10)
        String login;

        @Constraints.Range(min = 18, max = 120, message = "возраст вне допустимого")
        int age;

        @Constraints.Matches(regex = ".+@.+\\..+", message = "не похоже на e-mail")
        @Constraints.Matches(regex = "^[a-z@.]+$", message = "только строчные латинские")
        String email;

        @Constraints.Range(min = 0)
        String notANumber;

        @Constraints.InvisibleNotNull
        String forgottenRetention;

        String unconstrained;

        User(String id, String login, int age, String email) {
            super(id);
            this.login = login;
            this.age = age;
            this.email = email;
        }
    }

    private User valid() {
        return new User("U-1", "ivanov", 30, "ivanov@mail.ru");
    }

    @Nested
    @DisplayName("Ограничения работают по своим правилам")
    class Rules {

        @Test
        @DisplayName("Корректный объект нарушений не даёт")
        void validObjectPasses() {
            assertThat(ValidationEngine.validate(valid()).valid()).isTrue();
        }

        @Test
        @DisplayName("Маркерная @NotBlank срабатывает на null и на пробелы")
        void notBlankRejectsEmptiness() {
            User user = valid();
            user.login = "   ";

            assertThat(ValidationEngine.validate(user).invalidFields()).containsExactly("login");
            assertThat(ValidationEngine.validate(user).messages())
                    .anyMatch(m -> m.contains("значение обязательно"));
        }

        @Test
        @DisplayName("Single-value @MaxLength(10) читается без имени параметра")
        void maxLengthUsesValueElement() {
            User user = valid();
            user.login = "оченьдлинныйлогин";

            assertThat(ValidationEngine.validate(user).messages())
                    .anyMatch(m -> m.contains("превышает максимум 10"));
        }

        @Test
        @DisplayName("@Range использует и заданные значения, и defaults")
        void rangeUsesDefaultsAndExplicitValues() {
            User tooYoung = valid();
            tooYoung.age = 10;

            assertThat(ValidationEngine.validate(tooYoung).messages())
                    .anyMatch(m -> m.contains("возраст вне допустимого") && m.contains("[18, 120]"));
        }

        @Test
        @DisplayName("@Range на нечисловом поле — понятная ошибка вместо ClassCastException")
        void rangeOnNonNumericFieldIsReported() {
            User user = valid();
            user.notANumber = "не число";

            assertThat(ValidationEngine.validate(user).messages())
                    .anyMatch(m -> m.contains("@Range применим только к числам")
                            && m.contains("String"));
        }

        @Test
        @DisplayName("Повторяемая @Matches проверяет все шаблоны, а не первый")
        void repeatableConstraintChecksEveryPattern() {
            User user = valid();
            user.email = "IVANOV@MAIL.RU";

            assertThat(ValidationEngine.validate(user).messages())
                    .anyMatch(m -> m.contains("только строчные латинские"));

            // строка, нарушающая оба шаблона, даёт два отдельных нарушения
            user.email = "не-почта";
            assertThat(ValidationEngine.validate(user).violations())
                    .filteredOn(v -> v.field().equals("email"))
                    .hasSize(2)
                    .extracting(ValidationEngine.Violation::message)
                    .anyMatch(m -> m.contains("не похоже на e-mail"));

            // а корректный адрес не нарушает ни одного
            user.email = "ivanov@mail.ru";
            assertThat(ValidationEngine.validate(user).invalidFields()).doesNotContain("email");
        }

        @Test
        @DisplayName("Ограничения родительского класса действуют на наследника")
        void constraintsAreCollectedUpTheHierarchy() {
            User user = valid();
            user.id = null;

            assertThat(ValidationEngine.validate(user).invalidFields()).contains("id");
            assertThat(ValidationEngine.constrainedFields(User.class))
                    .extracting(java.lang.reflect.Field::getName)
                    .contains("login", "age", "email", "id");
        }

        @Test
        @DisplayName("Поле без ограничений не проверяется вовсе")
        void unconstrainedFieldIsIgnored() {
            User user = valid();
            user.unconstrained = null;

            assertThat(ValidationEngine.validate(user).invalidFields())
                    .doesNotContain("unconstrained");
        }
    }

    @Nested
    @DisplayName("Главная ловушка аннотаций")
    class RetentionTrap {

        @Test
        @DisplayName("Ограничение с retention CLASS движок не видит — оно молча не работает")
        void classRetentionIsInvisible() {
            User user = valid();
            user.forgottenRetention = null;

            assertThat(ValidationEngine.validate(user).invalidFields())
                    .doesNotContain("forgottenRetention");

            // аннотация в исходнике есть, а в runtime её нет
            assertThat(java.util.Arrays.stream(
                            ValidationEngine.constrainedFields(User.class).stream()
                                    .filter(f -> f.getName().equals("forgottenRetention"))
                                    .findFirst().orElseThrow()
                                    .getAnnotations())
                    .map(a -> a.annotationType().getSimpleName()))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("Режимы использования")
    class Modes {

        @Test
        @DisplayName("validate() собирает все нарушения разом")
        void collectsAllViolations() {
            User broken = new User(null, "", 5, "мусор");

            assertThat(ValidationEngine.validate(broken).invalidFields())
                    .contains("id", "login", "age", "email");
        }

        @Test
        @DisplayName("validateOrThrow() падает и приносит с собой полный отчёт")
        void failFastCarriesTheReport() {
            User broken = new User(null, "x", 5, "ivanov@mail.ru");

            assertThatThrownBy(() -> ValidationEngine.validateOrThrow(broken))
                    .isInstanceOf(ValidationEngine.ConstraintViolationException.class)
                    .satisfies(e -> assertThat(
                            ((ValidationEngine.ConstraintViolationException) e).result().invalidFields())
                            .contains("id", "age"));
        }

        @Test
        @DisplayName("Корректный объект validateOrThrow пропускает молча")
        void failFastPassesValidObjects() {
            ValidationEngine.validateOrThrow(valid());
        }
    }
}
