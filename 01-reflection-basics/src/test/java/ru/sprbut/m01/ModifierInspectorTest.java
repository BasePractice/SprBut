package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 6: модификаторы доступа полей и методов")
class ModifierInspectorTest {

    @Test
    @DisplayName("Modifier.toString() расшифровывает битовую маску модификаторов")
    void describesFieldModifiers() throws NoSuchFieldException {
        Field id = Account.class.getDeclaredField("id");
        Field type = Account.class.getDeclaredField("TYPE");
        Field blocked = Account.class.getDeclaredField("blocked");

        assertThat(ModifierInspector.describe(id)).isEqualTo("private final");
        assertThat(ModifierInspector.describe(type)).isEqualTo("public static final");
        assertThat(ModifierInspector.describe(blocked)).isEqualTo("protected");
    }

    @Test
    @DisplayName("getDeclaredFields() видит private, getFields() — только public")
    void declaredVsPublicFields() {
        assertThat(ModifierInspector.declaredFieldNames(Account.class))
                .containsExactlyInAnyOrder("TYPE", "id", "owner", "balance", "blocked", "cachedLabel");

        assertThat(ModifierInspector.publicFieldNames(Account.class))
                .containsExactly("TYPE");
    }

    @Test
    @DisplayName("Приватные и статические поля отбираются по флагам модификаторов")
    void filtersByModifier() {
        assertThat(ModifierInspector.privateFieldNames(Account.class))
                .containsExactlyInAnyOrder("id", "owner", "balance");

        assertThat(ModifierInspector.staticFieldNames(Account.class))
                .containsExactly("TYPE");
    }

    @Test
    @DisplayName("final-поле распознаётся флагом Modifier.FINAL")
    void detectsFinal() throws NoSuchFieldException {
        assertThat(ModifierInspector.isFinal(Account.class.getDeclaredField("id"))).isTrue();
        assertThat(ModifierInspector.isFinal(Account.class.getDeclaredField("owner"))).isFalse();
    }

    @Test
    @DisplayName("getDeclaredMethods() перечисляет и приватные методы тоже")
    void listsDeclaredMethods() {
        assertThat(ModifierInspector.declaredMethodNames(Account.class))
                .contains("getId", "getOwner", "getBalance", "isBlocked")
                .contains("applyFee", "block", "describeType")
                .doesNotContain("equals", "hashCode");
    }
}
