package ru.sprbut.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("СХЕМА 1: узел Modifier")
class ModifierApiTest {

    @Test
    @DisplayName("volatile и synchronized — такие же флаги, как public и final")
    void readsAllFlags() throws Exception {
        assertThat(ModifierApi.flagsOf(Order.class.getDeclaredField("paid")))
                .containsExactly("private", "volatile");
        assertThat(ModifierApi.flagsOf(Order.class.getDeclaredMethod("cancel")))
                .containsExactly("public", "synchronized");
        assertThat(ModifierApi.flagsOf(Order.class.getDeclaredField("STATUS_NEW")))
                .containsExactly("public", "static", "final");
    }

    @Test
    @DisplayName("package-private — это отсутствие битов, а не отдельный бит")
    void packagePrivateHasNoBit() {
        assertThat(ModifierApi.isPackagePrivate(0)).isTrue();
        assertThat(ModifierApi.isPackagePrivate(Modifier.PUBLIC)).isFalse();
        assertThat(ModifierApi.isPackagePrivate(Modifier.PRIVATE)).isFalse();
        assertThat(ModifierApi.isPackagePrivate(Modifier.STATIC | Modifier.FINAL)).isTrue();
    }

    @Test
    @DisplayName("Modifier.toString() печатает флаги в каноническом порядке javap")
    void describesInCanonicalOrder() {
        assertThat(ModifierApi.describe(Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC))
                .isEqualTo("public static final");
    }

    @Test
    @DisplayName("Наборы допустимых модификаторов различаются для класса и поля")
    void masksDifferPerElementKind() {
        assertThat(ModifierApi.isValidForClass(Modifier.PUBLIC | Modifier.ABSTRACT)).isTrue();
        assertThat(ModifierApi.isValidForClass(Modifier.VOLATILE)).isFalse();

        assertThat(ModifierApi.isValidForField(Modifier.PRIVATE | Modifier.VOLATILE)).isTrue();
        assertThat(ModifierApi.isValidForField(Modifier.SYNCHRONIZED)).isFalse();
    }

    @Test
    @DisplayName("Флаги складываются побитово — это одно int-число, а не коллекция")
    void modifiersAreBits() {
        int mods = Order.class.getModifiers();

        assertThat(mods & Modifier.PUBLIC).isNotZero();
        assertThat(mods & Modifier.ABSTRACT).isZero();
        assertThat(ModifierApi.flags(mods)).containsExactly("public");
    }
}
