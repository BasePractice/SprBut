package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд 7: статическое поле читается без экземпляра")
final class StaticFieldTest {

    @Test
    @DisplayName("значение статического поля берётся через get(null)")
    void readsStaticField() {
        assertThat(
            "static field cannot be read without an instance",
            new StaticField(Account.class, "TYPE").value(),
            equalTo("CHECKING")
        );
    }
}
