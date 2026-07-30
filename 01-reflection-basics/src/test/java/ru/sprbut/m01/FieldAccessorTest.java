package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 7: чтение и запись полей, включая private")
class FieldAccessorTest {

    private final Account account = new Account("ACC-1", "Иванов", new BigDecimal("100.00"));

    @Test
    @DisplayName("setAccessible(true) открывает чтение private-поля без геттера")
    void readsPrivateField() {
        assertThat(FieldAccessor.read(account, "owner")).isEqualTo("Иванов");
        assertThat(FieldAccessor.read(account, "balance")).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Запись в private-поле в обход сеттера — так Spring внедряет @Autowired-поля")
    void writesPrivateField() {
        FieldAccessor.write(account, "owner", "Петров");

        assertThat(account.getOwner()).isEqualTo("Петров");
    }

    @Test
    @DisplayName("Рефлексия пишет даже в private final поле, у которого нет сеттера")
    void writesPrivateFinalField() {
        FieldAccessor.write(account, "id", "ACC-999");

        assertThat(account.getId()).isEqualTo("ACC-999");
    }

    @Test
    @DisplayName("Статическое поле читается без экземпляра: get(null)")
    void readsStaticField() {
        assertThat(FieldAccessor.readStatic(Account.class, "TYPE")).isEqualTo("CHECKING");
    }

    @Test
    @DisplayName("Поиск поля поднимается по иерархии наследования")
    void findsInheritedField() {
        class Savings extends Account {
            Savings() {
                super("S-1", "Сидоров", BigDecimal.TEN);
            }
        }

        assertThat(FieldAccessor.findField(Savings.class, "owner").getDeclaringClass())
                .isEqualTo(Account.class);
    }

    @Test
    @DisplayName("Несуществующее поле — понятная ошибка, а не NoSuchFieldException из глубины")
    void failsOnUnknownField() {
        assertThatThrownBy(() -> FieldAccessor.read(account, "nope"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nope");
    }
}
