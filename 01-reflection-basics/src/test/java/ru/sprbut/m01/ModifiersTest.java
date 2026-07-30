package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд 6: расшифровка битовой маски модификаторов")
final class ModifiersTest {

    @Test
    @DisplayName("private final читается словами")
    void describesPrivateFinal() throws NoSuchFieldException {
        assertThat(
            "modifier mask cannot be read as private final",
            new Modifiers(Account.class.getDeclaredField("id")).text(),
            equalTo("private final")
        );
    }

    @Test
    @DisplayName("public static final читается словами")
    void describesPublicStaticFinal() throws NoSuchFieldException {
        assertThat(
            "modifier mask cannot be read as public static final",
            new Modifiers(Account.class.getDeclaredField("TYPE")).text(),
            equalTo("public static final")
        );
    }

    @Test
    @DisplayName("поле без модификаторов доступа даёт пустую строку")
    void describesPackagePrivate() throws NoSuchFieldException {
        assertThat(
            "package private field cannot yield an empty description",
            new Modifiers(Account.class.getDeclaredField("cachedLabel")).text(),
            equalTo("transient")
        );
    }

    @Test
    @DisplayName("final распознаётся отдельным флагом")
    void detectsFinal() throws NoSuchFieldException {
        assertThat(
            "final flag cannot be detected",
            new Modifiers(Account.class.getDeclaredField("id")).isFinal(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("изменяемое поле финальным не считается")
    void dontMarkMutableFieldFinal() {
        assertThat(
            "mutable field cannot avoid the final flag",
            new Modifiers(new Declared(Account.class).field("owner")).isFinal(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("static распознаётся отдельным флагом")
    void detectsStatic() {
        assertThat(
            "static flag cannot be detected",
            new Modifiers(new Declared(Account.class).field("TYPE")).isStatic(),
            equalTo(true)
        );
    }
}
