package ru.sprbut.m02.classic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.modern.CustomerRecord;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

@DisplayName("Слайд 16: java.beans.Introspector — штатная работа с соглашением")
final class IntrospectedTest {

    @Test
    @DisplayName("Introspector сам находит свойства по парам методов")
    void discoversProperties() {
        assertThat(
            "introspector cannot discover the bean properties",
            new Introspected(CustomerBean.class).names(),
            hasItems("id", "firstName", "lastName", "age", "vip", "fullName")
        );
    }

    @Test
    @DisplayName("служебное свойство class в отчёт не попадает")
    void hidesClassProperty() {
        assertThat(
            "service property class cannot be filtered out",
            new Introspected(CustomerBean.class).names(),
            not(hasItem("class"))
        );
    }

    @Test
    @DisplayName("read-write свойство требует и getter, и setter")
    void splitsReadWrite() {
        assertThat(
            "read-write list cannot demand both accessors",
            new Introspected(CustomerBean.class).readWrite(),
            not(hasItem("fullName"))
        );
    }

    @Test
    @DisplayName("вычисляемое свойство доступно только на чтение")
    void splitsReadOnly() {
        assertThat(
            "computed property cannot be listed as read only",
            new Introspected(CustomerBean.class).readOnly(),
            contains("fullName")
        );
    }

    @Test
    @DisplayName("тип свойства известен заранее — на этом строится конвертация значений")
    void knowsPropertyType() {
        assertThat(
            "property type cannot be known ahead of the value",
            new Introspected(CustomerBean.class).descriptor("age").orElseThrow().getPropertyType(),
            equalTo(int.class)
        );
    }

    @Test
    @DisplayName("у record Introspector не видит ни одного свойства")
    void dontIntrospectRecord() {
        assertThat(
            "record cannot stay invisible to the introspector",
            new Introspected(CustomerRecord.class).names(),
            emptyIterable()
        );
    }
}
