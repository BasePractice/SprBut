package ru.sprbut.m01;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд 7: чтение и запись полей, включая private")
final class ObjectFieldTest {

    @Test
    @DisplayName("setAccessible(true) открывает чтение private-поля без геттера")
    void readsPrivateField() {
        assertThat(
            "private field cannot be read without a getter",
            new ObjectField(
                new Account("ACC-1", "Иванов", new BigDecimal("100.00")), "owner"
            ).value(),
            equalTo("Иванов")
        );
    }

    @Test
    @DisplayName("запись в private-поле идёт в обход сеттера — так Spring внедряет @Autowired")
    void writesPrivateField() {
        Account account = new Account("ACC-2", "Иванов", new BigDecimal("100.00"));
        new ObjectField(account, "owner").assign("Петров");
        assertThat(
            "private field cannot be written past the setter",
            account.getOwner(),
            equalTo("Петров")
        );
    }

    @Test
    @DisplayName("рефлексия пишет даже в private final поле, у которого сеттера быть не может")
    void writesPrivateFinalField() {
        Account account = new Account("ACC-3", "Иванов", new BigDecimal("100.00"));
        new ObjectField(account, "id").assign("ACC-999");
        assertThat(
            "private final field cannot be overwritten reflectively",
            account.getId(),
            equalTo("ACC-999")
        );
    }

    @Test
    @DisplayName("поиск поля поднимается по иерархии наследования")
    void findsInheritedField() {
        final class Savings extends Account {
            Savings() {
                super("S-1", "Сидоров", BigDecimal.TEN);
            }
        }
        assertThat(
            "field lookup cannot climb up to the parent class",
            new ObjectField(new Savings(), "owner").declaration().getDeclaringClass(),
            equalTo(Account.class)
        );
    }

    @Test
    @DisplayName("несуществующее поле даёт понятную ошибку, а не NoSuchFieldException из глубины")
    void failsOnUnknownField() {
        assertThat(
            "unknown field cannot be reported with its own name",
            assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectField(
                    new Account("ACC-4", "Иванов", BigDecimal.ONE), "nope"
                ).value()
            ).getMessage(),
            containsString("nope")
        );
    }
}
