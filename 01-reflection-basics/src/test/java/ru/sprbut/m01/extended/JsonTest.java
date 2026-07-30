package ru.sprbut.m01.extended;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

@DisplayName("Расширенный пример: JSON-сериализатор целиком на рефлексии")
final class JsonTest {

    private enum Status { ACTIVE, BLOCKED }

    @SuppressWarnings("unused")
    private static class Base {

        private final String baseField = "база";
    }

    @SuppressWarnings("unused")
    private static final class Customer extends Base {

        static final String CONST = "не сериализуется";

        @JsonProperty("customer_id")
        private final String id = "C-1";

        private final String name = "Пётр";

        private final int age = 30;

        private final boolean active = true;

        private final Status status = Status.ACTIVE;

        private final List<String> tags = List.of("vip", "new");

        private final int[] scores = {5, 7};

        private final Map<String, String> extra = Map.of("city", "Москва");

        private final String comment = null;

        @JsonIgnore
        private final String secret = "пароль";

        private transient String cache = "временное";
    }

    @Test
    @DisplayName("static, transient и @JsonIgnore из выборки отбрасываются")
    void skipsExcludedFields() {
        assertThat(
            "excluded fields cannot stay out of the selection",
            new SerializableFields(Customer.class).list().stream().map(Field::getName).toList(),
            not(hasItems("CONST", "secret", "cache"))
        );
    }

    @Test
    @DisplayName("поля родителя попадают в выборку")
    void includesInheritedFields() {
        assertThat(
            "inherited field cannot reach the selection",
            new SerializableFields(Customer.class).list().stream().map(Field::getName).toList(),
            hasItems("baseField")
        );
    }

    @Test
    @DisplayName("поля самого класса идут раньше полей родителя")
    void ordersOwnFieldsFirst() {
        List<String> names =
            new SerializableFields(Customer.class).list().stream().map(Field::getName).toList();
        assertThat(
            "own fields cannot come before the inherited ones",
            names.indexOf("id"),
            lessThan(names.indexOf("baseField"))
        );
    }

    @Test
    @DisplayName("@JsonProperty переопределяет имя ключа")
    void renamesByAnnotation() {
        assertThat(
            "annotation cannot rename the JSON key",
            new PropertyName(
                java.util.Arrays.stream(Customer.class.getDeclaredFields())
                    .filter(field -> "id".equals(field.getName()))
                    .findFirst()
                    .orElseThrow()
            ).text(),
            equalTo("customer_id")
        );
    }

    @Test
    @DisplayName("без аннотации ключом становится имя поля")
    void keepsFieldNameWithoutAnnotation() {
        assertThat(
            "plain field cannot keep its own name as the key",
            new PropertyName(
                java.util.Arrays.stream(Customer.class.getDeclaredFields())
                    .filter(field -> "name".equals(field.getName()))
                    .findFirst()
                    .orElseThrow()
            ).text(),
            equalTo("name")
        );
    }

    @Test
    @DisplayName("строка пишется в кавычках, число — без них")
    void writesScalars() {
        assertThat(
            "scalar values cannot be written with the right quoting",
            new Json(new Customer()).text(),
            containsString("\"name\":\"Пётр\",\"age\":30")
        );
    }

    @Test
    @DisplayName("enum становится строкой своего имени")
    void writesEnumAsString() {
        assertThat(
            "enum cannot be written as its own name",
            new Json(new Customer()).text(),
            containsString("\"status\":\"ACTIVE\"")
        );
    }

    @Test
    @DisplayName("коллекция становится JSON-массивом")
    void writesCollectionAsArray() {
        assertThat(
            "collection cannot become a JSON array",
            new Json(new Customer()).text(),
            containsString("\"tags\":[\"vip\",\"new\"]")
        );
    }

    @Test
    @DisplayName("массив примитивов тоже становится JSON-массивом")
    void writesPrimitiveArray() {
        assertThat(
            "primitive array cannot become a JSON array",
            new Json(new Customer()).text(),
            containsString("\"scores\":[5,7]")
        );
    }

    @Test
    @DisplayName("Map становится вложенным объектом")
    void writesMapAsObject() {
        assertThat(
            "map cannot become a nested object",
            new Json(new Customer()).text(),
            containsString("\"extra\":{\"city\":\"Москва\"}")
        );
    }

    @Test
    @DisplayName("null-поле пишется литералом null")
    void writesNullField() {
        assertThat(
            "null field cannot be written as a literal",
            new Json(new Customer()).text(),
            containsString("\"comment\":null")
        );
    }

    @Test
    @DisplayName("null вместо объекта тоже даёт литерал null")
    void writesNullObject() {
        assertThat(
            "null object cannot be written as a literal",
            new Json(null).text(),
            equalTo("null")
        );
    }

    @Test
    @DisplayName("кавычки и переводы строк экранируются — JSON остаётся валидным")
    void escapesSpecialCharacters() {
        record Note(String text) {
        }
        assertThat(
            "special characters cannot be escaped",
            new Json(new Note("он сказал \"да\"\nи ушёл")).text(),
            equalTo("{\"text\":\"он сказал \\\"да\\\"\\nи ушёл\"}")
        );
    }

    @Test
    @DisplayName("вложенный объект сериализуется тем же кодом рекурсивно")
    void writesNestedObject() {
        record Address(String city) {
        }
        record Person(String name, Address address) {
        }
        assertThat(
            "nested object cannot be serialised recursively",
            new Json(new Person("Пётр", new Address("Москва"))).text(),
            equalTo("{\"name\":\"Пётр\",\"address\":{\"city\":\"Москва\"}}")
        );
    }

    @Test
    @DisplayName("объект без полей даёт пустой JSON-объект")
    void writesEmptyObject() {
        assertThat(
            "field free object cannot yield an empty JSON object",
            new Json(new Object() {
            }).text(),
            equalTo("{}")
        );
    }
}
