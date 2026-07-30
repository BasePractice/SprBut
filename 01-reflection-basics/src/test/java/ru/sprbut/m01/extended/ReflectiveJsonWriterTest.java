package ru.sprbut.m01.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: JSON-сериализатор целиком на рефлексии")
class ReflectiveJsonWriterTest {

    enum Status { ACTIVE, BLOCKED }

    @SuppressWarnings("unused")
    static class Base {
        private final String baseField = "база";
    }

    @SuppressWarnings("unused")
    static class Customer extends Base {
        static final String CONST = "не сериализуется";

        @JsonProperty("customer_id")
        private final String id;

        private final String name;

        @JsonIgnore
        private final String password;

        private transient String sessionToken = "секрет";

        private final int age;
        private final boolean vip;
        private final Status status;
        private final List<String> tags;
        private final Map<String, Integer> limits;
        private final int[] scores;
        private final String comment;

        Customer(String id, String name, String password, int age, boolean vip, Status status,
                 List<String> tags, Map<String, Integer> limits, int[] scores, String comment) {
            this.id = id;
            this.name = name;
            this.password = password;
            this.age = age;
            this.vip = vip;
            this.status = status;
            this.tags = tags;
            this.limits = limits;
            this.scores = scores;
            this.comment = comment;
        }
    }

    private Customer sample() {
        Map<String, Integer> limits = new LinkedHashMap<>();
        limits.put("daily", 1000);
        limits.put("monthly", 25000);
        return new Customer("C-1", "Иванов", "hunter2", 42, true, Status.ACTIVE,
                List.of("vip", "premium"), limits, new int[]{5, 4, 5}, null);
    }

    @Nested
    @DisplayName("Отбор полей строится на модификаторах и аннотациях")
    class FieldSelection {

        @Test
        @DisplayName("static, transient и @JsonIgnore отбрасываются, поля родителя — берутся")
        void selectsRightFields() {
            List<String> names = ReflectiveJsonWriter.serializableFields(Customer.class)
                    .stream()
                    .map(java.lang.reflect.Field::getName)
                    .toList();

            assertThat(names)
                    .contains("id", "name", "age", "vip", "status", "baseField")
                    .doesNotContain("CONST", "password", "sessionToken");
        }

        @Test
        @DisplayName("Поля самого класса идут раньше полей родителя")
        void ownFieldsComeBeforeInherited() {
            List<String> names = ReflectiveJsonWriter.serializableFields(Customer.class)
                    .stream()
                    .map(java.lang.reflect.Field::getName)
                    .toList();

            assertThat(names.indexOf("id")).isLessThan(names.indexOf("baseField"));
        }

        @Test
        @DisplayName("@JsonProperty переопределяет имя ключа, без неё берётся имя поля")
        void resolvesPropertyName() throws NoSuchFieldException {
            assertThat(ReflectiveJsonWriter.propertyName(Customer.class.getDeclaredField("id")))
                    .isEqualTo("customer_id");
            assertThat(ReflectiveJsonWriter.propertyName(Customer.class.getDeclaredField("name")))
                    .isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("Сериализация значений")
    class Serialization {

        @Test
        @DisplayName("Скаляры: строка в кавычках, число и boolean — без них")
        void writesScalars() {
            String json = ReflectiveJsonWriter.write(sample());

            assertThat(json)
                    .contains("\"customer_id\":\"C-1\"")
                    .contains("\"name\":\"Иванов\"")
                    .contains("\"age\":42")
                    .contains("\"vip\":true");
        }

        @Test
        @DisplayName("Enum пишется как строка своего имени")
        void writesEnumAsString() {
            assertThat(ReflectiveJsonWriter.write(sample())).contains("\"status\":\"ACTIVE\"");
        }

        @Test
        @DisplayName("Коллекции и массивы становятся JSON-массивами")
        void writesCollectionsAndArrays() {
            String json = ReflectiveJsonWriter.write(sample());

            assertThat(json)
                    .contains("\"tags\":[\"vip\",\"premium\"]")
                    .contains("\"scores\":[5,4,5]");
        }

        @Test
        @DisplayName("Map становится вложенным объектом")
        void writesMapAsObject() {
            assertThat(ReflectiveJsonWriter.write(sample()))
                    .contains("\"limits\":{\"daily\":1000,\"monthly\":25000}");
        }

        @Test
        @DisplayName("null-поле пишется литералом null, null-объект целиком — тоже")
        void writesNulls() {
            assertThat(ReflectiveJsonWriter.write(sample())).contains("\"comment\":null");
            assertThat(ReflectiveJsonWriter.write(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("Игнорируемые и служебные поля в вывод не попадают")
        void hidesIgnoredFields() {
            String json = ReflectiveJsonWriter.write(sample());

            assertThat(json)
                    .doesNotContain("hunter2")
                    .doesNotContain("password")
                    .doesNotContain("sessionToken")
                    .doesNotContain("CONST");
        }

        @Test
        @DisplayName("Кавычки и переводы строк экранируются — JSON остаётся валидным")
        void escapesSpecialCharacters() {
            record Note(String text) {
            }
            // record-компоненты — обычные private final поля, рефлексия их видит
            assertThat(ReflectiveJsonWriter.write(new Note("он сказал \"да\"\nи ушёл")))
                    .isEqualTo("{\"text\":\"он сказал \\\"да\\\"\\nи ушёл\"}");
        }

        @Test
        @DisplayName("Вложенный объект сериализуется рекурсивно")
        void writesNestedObjects() {
            record Address(String city) {
            }
            record Person(String name, Address address) {
            }

            assertThat(ReflectiveJsonWriter.write(new Person("Пётр", new Address("Москва"))))
                    .isEqualTo("{\"name\":\"Пётр\",\"address\":{\"city\":\"Москва\"}}");
        }

        @Test
        @DisplayName("Объект без полей даёт пустой JSON-объект")
        void writesEmptyObject() {
            assertThat(ReflectiveJsonWriter.write(new Object() {
            })).isEqualTo("{}");
        }
    }
}
