package ru.sprbut.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("СХЕМА 1: узел Field")
class FieldApiTest {

    private Field field(String name) throws NoSuchFieldException {
        return Order.class.getDeclaredField(name);
    }

    @Test
    @DisplayName("getType() отдаёт сырой тип после стирания дженериков")
    void rawTypeIsErased() throws NoSuchFieldException {
        assertThat(FieldApi.rawType(field("items"))).isEqualTo(List.class);
        assertThat(FieldApi.rawType(field("total"))).isEqualTo(BigDecimal.class);
    }

    @Test
    @DisplayName("getGenericType() сохраняет параметры типа — они лежат в атрибуте Signature")
    void genericTypeKeepsParameters() throws NoSuchFieldException {
        assertThat(FieldApi.genericType(field("items"))).isInstanceOf(ParameterizedType.class);
        assertThat(FieldApi.genericType(field("items")).getTypeName())
                .isEqualTo("java.util.List<java.lang.String>");
    }

    @Test
    @DisplayName("Аргументы дженерика извлекаются по одному — так работает разбор типов в Jackson")
    void extractsTypeArguments() throws NoSuchFieldException {
        assertThat(FieldApi.typeArguments(field("items"))).containsExactly("String");
        assertThat(FieldApi.typeArguments(field("discounts"))).containsExactly("String", "BigDecimal");
    }

    @Test
    @DisplayName("У необобщённого поля список аргументов типа пуст")
    void plainFieldHasNoTypeArguments() throws NoSuchFieldException {
        assertThat(FieldApi.typeArguments(field("customer"))).isEmpty();
    }

    @Test
    @DisplayName("getDeclaringClass() указывает, где поле объявлено на самом деле")
    void knowsItsOwner() throws NoSuchFieldException {
        assertThat(FieldApi.owner(field("id"))).isEqualTo(Order.class);
    }

    @Test
    @DisplayName("Примитивные поля отличаются от ссылочных и имеют тип-обёртку")
    void distinguishesPrimitives() throws NoSuchFieldException {
        assertThat(FieldApi.isPrimitive(field("paid"))).isTrue();
        assertThat(FieldApi.isPrimitive(field("customer"))).isFalse();

        assertThat(FieldApi.boxed(boolean.class)).isEqualTo(Boolean.class);
        assertThat(FieldApi.boxed(int.class)).isEqualTo(Integer.class);
        assertThat(FieldApi.boxed(String.class)).isEqualTo(String.class);
    }
}
