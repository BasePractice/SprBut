/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m01.extended;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Расширенный пример: JSON-сериализатор целиком на рефлексии.
 * @since 1.0
 */
@DisplayName("Расширенный пример: JSON-сериализатор целиком на рефлексии")
final class JsonTest {

    /**
     * Значение {@code BLOCKED}.
     */
    /**
     * Значение {@code ACTIVE}.
     */
    private enum Status { ACTIVE, BLOCKED }

    @SuppressWarnings("unused")
    private static class Base {

        /**
         * Поле.
         */
        private final String baseField = "база";
    }

    @SuppressWarnings("unused")
    private static final class Customer extends Base {

        /**
         * Значение {@code CONST}.
         */
        static final String CONST = "не сериализуется";

        /**
         * Идентификатор.
         */
        @JsonProperty("customer_id")
        private final String id = "C-1";

        /**
         * Имя.
         */
        private final String name = "Пётр";

        /**
         * Возраст.
         */
        private final int age = 30;

        /**
         * Признак активности.
         */
        private final boolean active = true;

        /**
         * Статус.
         */
        private final Status status = Status.ACTIVE;

        /**
         * Метки.
         */
        private final List<String> tags = List.of("vip", "new");

        /**
         * Значение {@code scores}.
         */
        private final int[] scores = {5, 7};

        /**
         * Значение {@code extra}.
         */
        private final Map<String, String> extra = Map.of("city", "Москва");

        /**
         * Значение {@code comment}.
         */
        private final String comment = null;

        /**
         * Секрет.
         */
        @JsonIgnore
        private final String secret = "пароль";

        /**
         * Значение {@code cache}.
         */
        private transient String cache = "временное";
    }

    @Test
    @DisplayName("static, transient и @JsonIgnore из выборки отбрасываются")
    void skipsExcludedFields() {
        MatcherAssert.assertThat(
            "excluded fields cannot stay out of the selection",
            new SerializableFields(Customer.class).list().stream().map(Field::getName).toList(),
            Matchers.not(Matchers.hasItems("CONST", "secret", "cache"))
        );
    }

    @Test
    @DisplayName("поля родителя попадают в выборку")
    void includesInheritedFields() {
        MatcherAssert.assertThat(
            "inherited field cannot reach the selection",
            new SerializableFields(Customer.class).list().stream().map(Field::getName).toList(),
            Matchers.hasItems("baseField")
        );
    }

    @Test
    @DisplayName("поля самого класса идут раньше полей родителя")
    void ordersOwnFieldsFirst() {
        final List<String> names =
            new SerializableFields(Customer.class).list().stream().map(Field::getName).toList();
        MatcherAssert.assertThat(
            "own fields cannot come before the inherited ones",
            names.indexOf("id"),
            Matchers.lessThan(names.indexOf("baseField"))
        );
    }

    @Test
    @DisplayName("@JsonProperty переопределяет имя ключа")
    void renamesByAnnotation() {
        MatcherAssert.assertThat(
            "annotation cannot rename the JSON key",
            new PropertyName(
                java.util.Arrays.stream(Customer.class.getDeclaredFields())
                    .filter(field -> "id".equals(field.getName()))
                    .findFirst()
                    .orElseThrow()
            ).text(),
            Matchers.equalTo("customer_id")
        );
    }

    @Test
    @DisplayName("без аннотации ключом становится имя поля")
    void keepsFieldNameWithoutAnnotation() {
        MatcherAssert.assertThat(
            "plain field cannot keep its own name as the key",
            new PropertyName(
                java.util.Arrays.stream(Customer.class.getDeclaredFields())
                    .filter(field -> "name".equals(field.getName()))
                    .findFirst()
                    .orElseThrow()
            ).text(),
            Matchers.equalTo("name")
        );
    }

    @Test
    @DisplayName("строка пишется в кавычках, число — без них")
    void writesScalars() {
        MatcherAssert.assertThat(
            "scalar values cannot be written with the right quoting",
            new Json(new Customer()).text(),
            Matchers.containsString("\"name\":\"Пётр\",\"age\":30")
        );
    }

    @Test
    @DisplayName("enum становится строкой своего имени")
    void writesEnumAsString() {
        MatcherAssert.assertThat(
            "enum cannot be written as its own name",
            new Json(new Customer()).text(),
            Matchers.containsString("\"status\":\"ACTIVE\"")
        );
    }

    @Test
    @DisplayName("коллекция становится JSON-массивом")
    void writesCollectionAsArray() {
        MatcherAssert.assertThat(
            "collection cannot become a JSON array",
            new Json(new Customer()).text(),
            Matchers.containsString("\"tags\":[\"vip\",\"new\"]")
        );
    }

    @Test
    @DisplayName("массив примитивов тоже становится JSON-массивом")
    void writesPrimitiveArray() {
        MatcherAssert.assertThat(
            "primitive array cannot become a JSON array",
            new Json(new Customer()).text(),
            Matchers.containsString("\"scores\":[5,7]")
        );
    }

    @Test
    @DisplayName("Map становится вложенным объектом")
    void writesMapAsObject() {
        MatcherAssert.assertThat(
            "map cannot become a nested object",
            new Json(new Customer()).text(),
            Matchers.containsString("\"extra\":{\"city\":\"Москва\"}")
        );
    }

    @Test
    @DisplayName("null-поле пишется литералом null")
    void writesNullField() {
        MatcherAssert.assertThat(
            "null field cannot be written as a literal",
            new Json(new Customer()).text(),
            Matchers.containsString("\"comment\":null")
        );
    }

    @Test
    @DisplayName("null вместо объекта тоже даёт литерал null")
    void writesNullObject() {
        MatcherAssert.assertThat(
            "null object cannot be written as a literal",
            new Json(null).text(),
            Matchers.equalTo("null")
        );
    }

    @Test
    @DisplayName("кавычки и переводы строк экранируются — JSON остаётся валидным")
    void escapesSpecialCharacters() {
        record Note(String text) {
        }
        MatcherAssert.assertThat(
            "special characters cannot be escaped",
            new Json(new Note("он сказал \"да\"\nи ушёл")).text(),
            Matchers.equalTo("{\"text\":\"он сказал \\\"да\\\"\\nи ушёл\"}")
        );
    }

    @Test
    @DisplayName("вложенный объект сериализуется тем же кодом рекурсивно")
    void writesNestedObject() {
        record Address(String city) {
        }
        record Person(String name, Address address) {
        }
        MatcherAssert.assertThat(
            "nested object cannot be serialised recursively",
            new Json(new Person("Пётр", new Address("Москва"))).text(),
            Matchers.equalTo("{\"name\":\"Пётр\",\"address\":{\"city\":\"Москва\"}}")
        );
    }

    @Test
    @DisplayName("объект без полей даёт пустой JSON-объект")
    void writesEmptyObject() {
        MatcherAssert.assertThat(
            "field free object cannot yield an empty JSON object",
            new Json(new Object() {
            }).text(),
            Matchers.equalTo("{}")
        );
    }
}
