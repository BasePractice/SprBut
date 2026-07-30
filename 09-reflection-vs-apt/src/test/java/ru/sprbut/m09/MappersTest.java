package ru.sprbut.m09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайды 73–75: три механизма, один результат")
class MappersTest {

    private UserEntity entity() {
        UserEntity entity = new UserEntity("U-1", "Иван", "Иванов", 42, true);
        entity.setInternalNote("служебное");
        return entity;
    }

    @Nested
    @DisplayName("Reflection: runtime, гибко, медленно")
    class Reflective {

        private final ReflectiveMapper mapper = new ReflectiveMapper();

        @Test
        @DisplayName("Правила маппинга выведены из метаданных, а не написаны руками")
        void discoversRulesItself() {
            assertThat(mapper.propertyNames())
                    .containsExactly("active", "age", "firstName", "id", "lastName");
            assertThat(mapper.discoveredProperties()).isEqualTo(5);
        }

        @Test
        @DisplayName("Свойство, которого нет у цели, просто пропускается")
        void skipsUnmatchedProperties() {
            assertThat(mapper.propertyNames()).doesNotContain("internalNote");
        }

        @Test
        @DisplayName("Маппинг работает")
        void mapsCorrectly() {
            UserDto dto = mapper.toDto(entity());

            assertThat(dto.getId()).isEqualTo("U-1");
            assertThat(dto.getFirstName()).isEqualTo("Иван");
            assertThat(dto.getAge()).isEqualTo(42);
            assertThat(dto.isActive()).isTrue();
        }

        @Test
        @DisplayName("null на входе — null на выходе")
        void handlesNull() {
            assertThat(mapper.toDto(null)).isNull();
        }
    }

    @Nested
    @DisplayName("APT: compile-time, только генерация, быстро")
    class GeneratedStyle {

        private final GeneratedStyleMapper mapper = new GeneratedStyleMapper();

        @Test
        @DisplayName("Прямые вызовы дают тот же результат, что и рефлексия")
        void resultMatchesReflection() {
            assertThat(mapper.toDto(entity())).isEqualTo(new ReflectiveMapper().toDto(entity()));
        }

        @Test
        @DisplayName("Никаких обращений к метаданным в реализации нет")
        void usesNoReflection() {
            // Класс не хранит ни Method, ни Field — только код
            assertThat(GeneratedStyleMapper.class.getDeclaredFields()).isEmpty();
            assertThat(mapper.strategy()).contains("этапе компиляции");
        }
    }

    @Nested
    @DisplayName("Байткод: и то, и другое")
    class Bytecode {

        @Test
        @DisplayName("Класс собирается в runtime — в исходниках его нет")
        void classIsGeneratedAtRuntime() {
            UserMapper mapper = BytecodeMapper.create();

            assertThat(mapper.getClass().getName())
                    .isEqualTo("ru.sprbut.m09.bytebuddy.GeneratedUserMapper");
            assertThat(mapper.strategy()).contains("runtime");
        }

        @Test
        @DisplayName("Сгенерированный класс — полноценная реализация интерфейса")
        void generatedClassWorks() {
            assertThat(BytecodeMapper.create().toDto(entity()))
                    .isEqualTo(new GeneratedStyleMapper().toDto(entity()));
        }

        @Test
        @DisplayName("Каждый вызов create() даёт новый загруженный класс")
        void eachCallProducesANewClass() {
            assertThat(BytecodeMapper.create().getClass())
                    .as("WRAPPER-стратегия создаёт отдельный загрузчик на каждый вызов")
                    .isNotSameAs(BytecodeMapper.create().getClass());
        }

        @Test
        @DisplayName("Байткод проксирует класс БЕЗ интерфейса — то, чего не умеет JDK-прокси")
        void proxiesClassWithoutInterface() {
            BytecodeMapper.AuditService proxy = BytecodeMapper.proxyWithoutInterface();

            assertThat(proxy).isInstanceOf(BytecodeMapper.AuditService.class);
            assertThat(java.lang.reflect.Proxy.isProxyClass(proxy.getClass()))
                    .as("это не JDK-прокси, а подкласс")
                    .isFalse();
            assertThat(proxy.getClass().getSuperclass())
                    .isEqualTo(BytecodeMapper.AuditService.class);
        }

        @Test
        @DisplayName("Перехват работает, оригинальный метод при этом вызывается")
        void interceptsAndDelegates() {
            BytecodeMapper.AuditService proxy = BytecodeMapper.proxyWithoutInterface();

            assertThat(proxy.record("вход")).isEqualTo("записано: вход");
            assertThat(BytecodeMapper.INTERCEPTED).hasSize(1);
            assertThat(BytecodeMapper.INTERCEPTED.get(0)).contains("Enhanced");
        }

        @Test
        @DisplayName("Класс интерфейса не нужен вовсе — AuditService его не реализует")
        void targetHasNoInterfaces() {
            assertThat(BytecodeMapper.AuditService.class.getInterfaces()).isEmpty();
        }
    }
}
