package ru.sprbut.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 32: стирание типов, getGenericType, ParameterizedType")
class GenericTypesTest {

    private Field field(String name) throws NoSuchFieldException {
        return GenericTypes.Holder.class.getField(name);
    }

    @Test
    @DisplayName("Значения стёрты, объявления — нет: getType() против getGenericType()")
    void erasureIsNotTotal() throws NoSuchFieldException {
        assertThat(GenericTypes.rawTypeOf(field("names"))).isEqualTo(List.class);
        assertThat(GenericTypes.typeArgumentsOf(field("names"))).containsExactly("java.lang.String");
    }

    @Test
    @DisplayName("Вложенные дженерики читаются целиком")
    void readsNestedGenerics() throws NoSuchFieldException {
        assertThat(GenericTypes.typeArgumentsOf(field("nested")))
                .containsExactly("java.lang.String", "java.util.List<java.lang.Integer>");
    }

    @Test
    @DisplayName("Обобщённый возвращаемый тип метода тоже сохранён")
    void readsGenericReturnType() throws NoSuchMethodException {
        assertThat(GenericTypes.returnTypeArgumentsOf(GenericTypes.Holder.class.getMethod("produce")))
                .containsExactly("T");
    }

    @Test
    @DisplayName("Дерево типов состоит из разных реализаций Type")
    void distinguishesTypeKinds() throws NoSuchFieldException {
        assertThat(GenericTypes.kindOf(field("plain").getGenericType())).isEqualTo("Class");
        assertThat(GenericTypes.kindOf(field("names").getGenericType())).isEqualTo("ParameterizedType");
        assertThat(GenericTypes.kindOf(field("typeVariable").getGenericType())).isEqualTo("TypeVariable");
        assertThat(GenericTypes.kindOf(field("genericArray").getGenericType()))
                .isEqualTo("GenericArrayType");

        Type covariantArg = ((ParameterizedType) field("covariant").getGenericType())
                .getActualTypeArguments()[0];
        assertThat(GenericTypes.kindOf(covariantArg)).isEqualTo("WildcardType");
    }

    @Test
    @DisplayName("У wildcard читается верхняя граница")
    void readsWildcardBound() throws NoSuchFieldException {
        Type arg = ((ParameterizedType) field("covariant").getGenericType()).getActualTypeArguments()[0];

        assertThat(GenericTypes.upperBoundOf((WildcardType) arg)).isEqualTo(Number.class);
    }

    @Test
    @DisplayName("У переменной типа читаются её границы")
    void readsTypeVariableBounds() {
        TypeVariable<?> variable = GenericTypes.Holder.class.getTypeParameters()[0];

        assertThat(variable.getName()).isEqualTo("T");
        assertThat(GenericTypes.boundsOf(variable))
                .containsExactly("java.lang.Comparable<T>");
    }

    @Test
    @DisplayName("Type token: анонимный подкласс сохраняет фактический параметр типа")
    void typeTokenCapturesGenerics() {
        var token = new GenericTypes.TypeToken<List<String>>() {
        };

        assertThat(token.type().getTypeName()).isEqualTo("java.util.List<java.lang.String>");
        assertThat(GenericTypes.kindOf(token.type())).isEqualTo("ParameterizedType");
    }

    @Test
    @DisplayName("Без параметров типа список аргументов пуст, а не null")
    void plainTypeHasNoArguments() throws NoSuchFieldException {
        assertThat(GenericTypes.typeArgumentsOf(field("plain"))).isEmpty();
    }
}
