package ru.sprbut.m03;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("СХЕМА 1: узел Field")
final class FieldTypeTest {

    @Test
    @DisplayName("getType() отдаёт сырой тип после стирания дженериков")
    void erasesRawType() throws NoSuchFieldException {
        assertThat(
            "raw type cannot be erased to the bare interface",
            new FieldType(Order.class.getDeclaredField("items")).raw(),
            equalTo(List.class)
        );
    }

    @Test
    @DisplayName("getGenericType() сохраняет параметры типа — они лежат в атрибуте Signature")
    void keepsGenericParameters() throws NoSuchFieldException {
        assertThat(
            "generic type cannot keep its parameters",
            new FieldType(Order.class.getDeclaredField("items")).generic().getTypeName(),
            containsString("java.lang.String")
        );
    }

    @Test
    @DisplayName("аргументы дженерика извлекаются по одному — так работает разбор типов в Jackson")
    void extractsTypeArguments() throws NoSuchFieldException {
        assertThat(
            "type arguments cannot be extracted one by one",
            new FieldType(Order.class.getDeclaredField("discounts")).arguments(),
            contains("String", "BigDecimal")
        );
    }

    @Test
    @DisplayName("у необобщённого поля список аргументов типа пуст")
    void reportsNoArgumentsForPlainField() throws NoSuchFieldException {
        assertThat(
            "plain field cannot report an empty argument list",
            new FieldType(Order.class.getDeclaredField("customer")).arguments(),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("getDeclaringClass() указывает, где поле объявлено на самом деле")
    void knowsItsOwner() throws NoSuchFieldException {
        assertThat(
            "field cannot name its declaring class",
            new FieldType(Order.class.getDeclaredField("total")).owner(),
            equalTo(Order.class)
        );
    }

    @Test
    @DisplayName("примитивное поле отличается от ссылочного")
    void detectsPrimitiveField() throws NoSuchFieldException {
        assertThat(
            "primitive field cannot be told apart from a reference one",
            new FieldType(Order.class.getDeclaredField("paid")).primitive(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("ссылочное поле примитивным не считается")
    void dontCallReferenceFieldPrimitive() throws NoSuchFieldException {
        assertThat(
            "reference field cannot avoid the primitive flag",
            new FieldType(Order.class.getDeclaredField("total")).primitive(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("у примитива есть тип-обёртка — без неё проверка типов аргументов всегда ложна")
    void boxesPrimitive() {
        assertThat(
            "primitive cannot be boxed to its wrapper",
            new Boxed(boolean.class).type(),
            equalTo(Boolean.class)
        );
    }

    @Test
    @DisplayName("ссылочный тип остаётся собой")
    void keepsReferenceTypeAsIs() {
        assertThat(
            "reference type cannot survive boxing untouched",
            new Boxed(BigDecimal.class).type(),
            equalTo(BigDecimal.class)
        );
    }
}
