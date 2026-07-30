package ru.sprbut.m04;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд 32: стирание типов не абсолютно")
final class GenericTypeTest {

    @Test
    @DisplayName("параметры типа поля сохранены в атрибуте Signature")
    void keepsFieldTypeArguments() throws NoSuchFieldException {
        assertThat(
            "field type arguments cannot survive erasure",
            new GenericType(Holder.class.getField("names").getGenericType()).arguments(),
            contains("java.lang.String")
        );
    }

    @Test
    @DisplayName("вложенный дженерик остаётся вложенным")
    void keepsNestedTypeArguments() throws NoSuchFieldException {
        assertThat(
            "nested generic cannot stay nested",
            new GenericType(Holder.class.getField("nested").getGenericType()).arguments(),
            contains("java.lang.String", "java.util.List<java.lang.Integer>")
        );
    }

    @Test
    @DisplayName("у необобщённого поля параметров типа нет")
    void reportsNoArgumentsForPlainField() throws NoSuchFieldException {
        assertThat(
            "plain field cannot report an empty argument list",
            new GenericType(Holder.class.getField("plain").getGenericType()).arguments(),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("параметризованный тип распознаётся как ParameterizedType")
    void classifiesParameterizedType() throws NoSuchFieldException {
        assertThat(
            "parameterized type cannot be classified",
            new GenericType(Holder.class.getField("names").getGenericType()).kind(),
            equalTo("ParameterizedType")
        );
    }

    @Test
    @DisplayName("wildcard — отдельный род узла")
    void classifiesWildcard() throws NoSuchFieldException {
        Type argument = ((ParameterizedType) Holder.class.getField("covariant").getGenericType())
            .getActualTypeArguments()[0];
        assertThat(
            "wildcard cannot be classified as its own kind",
            new GenericType(argument).kind(),
            equalTo("WildcardType")
        );
    }

    @Test
    @DisplayName("переменная типа — тоже отдельный род узла")
    void classifiesTypeVariable() throws NoSuchFieldException {
        assertThat(
            "type variable cannot be classified as its own kind",
            new GenericType(Holder.class.getField("typeVariable").getGenericType()).kind(),
            equalTo("TypeVariable")
        );
    }

    @Test
    @DisplayName("обобщённый массив отличается от обычного")
    void classifiesGenericArray() throws NoSuchFieldException {
        assertThat(
            "generic array cannot be told apart from a plain one",
            new GenericType(Holder.class.getField("genericArray").getGenericType()).kind(),
            equalTo("GenericArrayType")
        );
    }

    @Test
    @DisplayName("верхняя граница wildcard читается напрямую")
    void readsWildcardBound() throws NoSuchFieldException {
        Type argument = ((ParameterizedType) Holder.class.getField("covariant").getGenericType())
            .getActualTypeArguments()[0];
        assertThat(
            "wildcard upper bound cannot be read",
            new GenericType(argument).bounds(),
            contains("java.lang.Number")
        );
    }

    @Test
    @DisplayName("границы переменной типа читаются так же")
    void readsTypeVariableBounds() throws NoSuchFieldException {
        assertThat(
            "type variable bounds cannot be read",
            new GenericType(Holder.class.getField("typeVariable").getGenericType()).bounds(),
            contains("java.lang.Comparable<T>")
        );
    }

    @Test
    @DisplayName("тип возвращаемого значения тоже сохраняет параметры")
    void keepsReturnTypeArguments() throws NoSuchMethodException {
        assertThat(
            "return type arguments cannot survive erasure",
            new GenericType(Holder.class.getMethod("produce").getGenericReturnType()).arguments(),
            contains("T")
        );
    }

    @Test
    @DisplayName("анонимный подкласс ловит фактический параметр типа")
    void capturesTypeByToken() {
        assertThat(
            "type token cannot capture the actual type argument",
            new TypeToken<List<String>>() {
            }.type().getTypeName(),
            equalTo("java.util.List<java.lang.String>")
        );
    }
}
