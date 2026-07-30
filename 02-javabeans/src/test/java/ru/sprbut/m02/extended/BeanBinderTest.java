package ru.sprbut.m02.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.classic.CustomerBean;
import ru.sprbut.m02.modern.CustomerRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Расширенный пример: биндинг конфигурации в JavaBean")
class BeanBinderTest {

    @Nested
    @DisplayName("Заполнение бина из карты значений")
    class Binding {

        @Test
        @DisplayName("Строки конвертируются в типы свойств: int, long, boolean, enum, BigDecimal, LocalDate")
        void convertsAllSupportedTypes() {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("host", "api.example.com");
            values.put("port", "8443");
            values.put("sslEnabled", "true");
            values.put("timeoutMillis", "30000");
            values.put("mode", "PROD");
            values.put("rateLimit", "12.50");
            values.put("validUntil", "2030-01-31");

            ServerProperties props = BeanBinder.bind(ServerProperties.class, values).bean();

            assertThat(props.getHost()).isEqualTo("api.example.com");
            assertThat(props.getPort()).isEqualTo(8443);
            assertThat(props.isSslEnabled()).isTrue();
            assertThat(props.getTimeoutMillis()).isEqualTo(30_000L);
            assertThat(props.getMode()).isEqualTo(ServerProperties.Mode.PROD);
            assertThat(props.getRateLimit()).isEqualByComparingTo("12.50");
            assertThat(props.getValidUntil()).isEqualTo(LocalDate.of(2030, 1, 31));
        }

        @Test
        @DisplayName("Незаданные свойства сохраняют значения по умолчанию из кода")
        void keepsCodeDefaults() {
            ServerProperties props = BeanBinder.bind(ServerProperties.class, Map.of("port", "9090")).bean();

            assertThat(props.getHost()).isEqualTo("localhost");
            assertThat(props.getMode()).isEqualTo(ServerProperties.Mode.DEV);
            assertThat(props.getPort()).isEqualTo(9090);
        }

        @Test
        @DisplayName("kebab-case и snake_case ключи приводятся к camelCase")
        void normalizesKeyStyles() {
            assertThat(BeanBinder.normalize("ssl-enabled")).isEqualTo("sslEnabled");
            assertThat(BeanBinder.normalize("timeout_millis")).isEqualTo("timeoutMillis");
            assertThat(BeanBinder.normalize("host")).isEqualTo("host");

            ServerProperties props = BeanBinder
                    .bind(ServerProperties.class, Map.of("ssl-enabled", "true", "timeout_millis", "100"))
                    .bean();

            assertThat(props.isSslEnabled()).isTrue();
            assertThat(props.getTimeoutMillis()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Enum читается без учёта регистра")
        void enumIsCaseInsensitive() {
            assertThat(BeanBinder.bind(ServerProperties.class, Map.of("mode", "stage")).bean().getMode())
                    .isEqualTo(ServerProperties.Mode.STAGE);
        }

        @Test
        @DisplayName("Неизвестные ключи не роняют биндинг, а попадают в отчёт")
        void reportsIgnoredKeys() {
            BeanBinder.BindResult<ServerProperties> result = BeanBinder.bind(
                    ServerProperties.class, Map.of("host", "h", "unknown.key", "42"));

            assertThat(result.boundProperties()).containsExactly("host");
            assertThat(result.ignoredKeys()).containsExactly("unknown.key");
        }

        @Test
        @DisplayName("Read-only свойство биндингу недоступно — сеттера нет")
        void readOnlyPropertyIsIgnored() {
            BeanBinder.BindResult<ServerProperties> result =
                    BeanBinder.bind(ServerProperties.class, Map.of("baseUrl", "http://hack"));

            assertThat(result.ignoredKeys()).containsExactly("baseUrl");
            assertThat(result.bean().getBaseUrl()).isEqualTo("http://localhost:8080");
        }
    }

    @Nested
    @DisplayName("Границы применимости соглашения")
    class Limits {

        @Test
        @DisplayName("record забиндить нельзя: нет конструктора без параметров и сеттеров")
        void cannotBindRecord() {
            assertThatThrownBy(() -> BeanBinder.bind(CustomerRecord.class, Map.of("id", "C-1")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("не является JavaBean")
                    .hasMessageContaining("нет публичного конструктора без параметров");
        }

        @Test
        @DisplayName("Ошибка конвертации называет свойство и значение")
        void conversionErrorIsDescriptive() {
            assertThatThrownBy(() -> BeanBinder.bind(ServerProperties.class, Map.of("port", "восемь")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("port")
                    .hasMessageContaining("восемь");
        }

        @Test
        @DisplayName("Неизвестное значение enum перечисляет допустимые варианты")
        void unknownEnumValueListsOptions() {
            assertThatThrownBy(() -> BeanBinder.bind(ServerProperties.class, Map.of("mode", "QA")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DEV")
                    .hasMessageContaining("PROD");
        }

        @Test
        @DisplayName("Валидация в сеттере остаётся рабочей — биндер её не обходит")
        void setterValidationStillApplies() {
            assertThatThrownBy(() -> BeanBinder.bind(ServerProperties.class, Map.of("port", "70000")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("вне диапазона");
        }
    }

    @Nested
    @DisplayName("Копирование свойств между бинами")
    class Copying {

        @SuppressWarnings("unused")
        public static class CustomerDto {
            private String id;
            private String firstName;
            private String lastName;
            private String unrelated;

            public CustomerDto() {
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public String getUnrelated() {
                return unrelated;
            }

            public void setUnrelated(String unrelated) {
                this.unrelated = unrelated;
            }
        }

        @Test
        @DisplayName("Копируются только одноимённые и совместимые по типу свойства")
        void copiesMatchingProperties() {
            CustomerBean source = new CustomerBean();
            source.setId("C-1");
            source.setFirstName("Иван");
            source.setLastName("Иванов");
            source.setAge(42);

            CustomerDto target = new CustomerDto();
            var copied = BeanBinder.copyProperties(source, target);

            assertThat(copied).containsExactly("firstName", "id", "lastName");
            assertThat(target.getId()).isEqualTo("C-1");
            assertThat(target.getFirstName()).isEqualTo("Иван");
            assertThat(target.getUnrelated()).isNull();
        }

        @Test
        @DisplayName("Бин разворачивается обратно в плоскую карту — печать эффективного конфига")
        void describesBeanAsFlatMap() {
            ServerProperties props = BeanBinder
                    .bind(ServerProperties.class, Map.of("host", "api", "port", "443", "sslEnabled", "true"))
                    .bean();

            assertThat(BeanBinder.describe(props))
                    .containsEntry("host", "api")
                    .containsEntry("port", "443")
                    .containsEntry("sslEnabled", "true")
                    .containsEntry("baseUrl", "https://api:443")
                    .containsEntry("rateLimit", BigDecimal.ZERO.toString());
        }
    }
}
