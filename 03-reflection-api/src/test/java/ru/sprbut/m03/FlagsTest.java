package ru.sprbut.m03;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

@DisplayName("СХЕМА 1: узел Modifier")
final class FlagsTest {

    @Test
    @DisplayName("маска раскладывается на отдельные флаги")
    void listsFlags() throws NoSuchFieldException {
        assertThat(
            "modifier mask cannot be split into flags",
            new Flags(Order.class.getDeclaredField("id")).names(),
            contains("private", "final")
        );
    }

    @Test
    @DisplayName("volatile — такой же флаг, как остальные")
    void listsVolatile() throws NoSuchFieldException {
        assertThat(
            "volatile flag cannot appear among the others",
            new Flags(Order.class.getDeclaredField("paid")).names(),
            hasItems("private", "volatile")
        );
    }

    @Test
    @DisplayName("javap печатает модификаторы в каноническом порядке")
    void printsCanonicalOrder() throws NoSuchFieldException {
        assertThat(
            "canonical modifier order cannot be printed",
            new Flags(Order.class.getDeclaredField("STATUS_NEW")).text(),
            equalTo("public static final")
        );
    }

    @Test
    @DisplayName("package-private собственного бита не имеет — это отсутствие трёх других")
    void detectsPackagePrivate() {
        assertThat(
            "package private cannot be detected by the absence of the other flags",
            new Flags(0).packagePrivate(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("public package-private не является")
    void dontCallPublicPackagePrivate() {
        assertThat(
            "public member cannot avoid the package private verdict",
            new Flags(Modifier.PUBLIC).packagePrivate(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("volatile для класса недопустим — у каждого элемента своя маска допустимых флагов")
    void rejectsVolatileOnClass() {
        assertThat(
            "volatile cannot be rejected for a class",
            new Flags(Modifier.VOLATILE).validForClass(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("для поля volatile допустим")
    void acceptsVolatileOnField() {
        assertThat(
            "volatile cannot be accepted for a field",
            new Flags(Modifier.VOLATILE).validForField(),
            equalTo(true)
        );
    }
}
