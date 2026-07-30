package ru.sprbut.m10.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m10.lombok.CustomerDto;
import ru.sprbut.m10.lombok.CustomerEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: MapStruct поверх Lombok")
class CustomerMapperTest {

    private final CustomerMapper mapper = CustomerMapper.INSTANCE;

    private CustomerEntity entity() {
        return new CustomerEntity("C-1", "Иван", "Иванов",
                LocalDate.of(1984, 3, 15), new BigDecimal("100.00"), true, "служебное");
    }

    @Nested
    @DisplayName("Маппинг работает")
    class Mapping {

        @Test
        @DisplayName("Одноимённые свойства копируются напрямую")
        void copiesMatchingProperties() {
            CustomerDto dto = mapper.toDto(entity());

            assertThat(dto.getId()).isEqualTo("C-1");
            assertThat(dto.getBalance()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("expression склеивает два поля в одно")
        void expressionCombinesFields() {
            assertThat(mapper.toDto(entity()).getFullName()).isEqualTo("Иван Иванов");
        }

        @Test
        @DisplayName("qualifiedByName вызывает именованный метод преобразования")
        void namedMethodsConvertValues() {
            CustomerDto dto = mapper.toDto(entity());

            assertThat(dto.getAge()).isEqualTo(42);
            assertThat(dto.getStatus()).isEqualTo("VIP");
        }

        @Test
        @DisplayName("Не-VIP получает другой статус")
        void nonVipGetsStandardStatus() {
            CustomerEntity plain = entity();
            plain.setVip(false);

            assertThat(mapper.toDto(plain).getStatus()).isEqualTo("STANDARD");
        }

        @Test
        @DisplayName("@AfterMapping подставляет значение по умолчанию вместо null")
        void afterMappingNormalizesNulls() {
            CustomerEntity noBalance = entity();
            noBalance.setBalance(null);

            assertThat(mapper.toDto(noBalance).getBalance()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Маппинг списка генерируется автоматически из одиночного метода")
        void mapsCollections() {
            CustomerEntity second = entity();
            second.setId("C-2");
            second.setVip(false);

            List<CustomerDto> dtos = mapper.toDtos(List.of(entity(), second));

            assertThat(dtos).extracting(CustomerDto::getId).containsExactly("C-1", "C-2");
            assertThat(dtos).extracting(CustomerDto::getStatus).containsExactly("VIP", "STANDARD");
        }

        @Test
        @DisplayName("null на входе — null на выходе")
        void handlesNull() {
            assertThat(mapper.toDto(null)).isNull();
            assertThat(mapper.toDtos(null)).isNull();
        }

        @Test
        @DisplayName("Служебное поле в DTO не попадает — его там просто нет")
        void internalFieldDoesNotLeak() {
            assertThat(mapper.toDto(entity()).toString()).doesNotContain("служебное");
        }
    }

    @Nested
    @DisplayName("Что именно сгенерировано")
    class Generated {

        @Test
        @DisplayName("Реализация интерфейса появилась при компиляции")
        void implementationIsGenerated() {
            assertThat(mapper.getClass().getName())
                    .isEqualTo("ru.sprbut.m10.extended.CustomerMapperImpl");
            assertThat(mapper).isInstanceOf(CustomerMapper.class);
        }

        @Test
        @DisplayName("Это обычный класс, а не прокси — рефлексии в нём нет")
        void generatedClassIsNotAProxy() {
            assertThat(java.lang.reflect.Proxy.isProxyClass(mapper.getClass())).isFalse();
            assertThat(mapper.getClass().getDeclaredFields())
                    .as("маппер не хранит ни Method, ни Field")
                    .isEmpty();
        }

        @Test
        @DisplayName("Mappers.getMapper создаёт новый экземпляр на каждый вызов — кэшировать надо самому")
        void mappersFactoryCreatesNewInstances() {
            CustomerMapper first = org.mapstruct.factory.Mappers.getMapper(CustomerMapper.class);
            CustomerMapper second = org.mapstruct.factory.Mappers.getMapper(CustomerMapper.class);

            assertThat(first).isNotSameAs(second).isNotSameAs(CustomerMapper.INSTANCE);
            assertThat(first).hasSameClassAs(CustomerMapper.INSTANCE);

            // Именно поэтому в Spring-проектах используют componentModel = "spring":
            // тогда маппер становится бином, и его жизненным циклом управляет контейнер
        }

        @Test
        @DisplayName("Lombok отработал раньше MapStruct — иначе геттеров бы не было")
        void lombokRanBeforeMapStruct() {
            // MapStruct вызывает entity.getFirstName(), которого нет в исходнике.
            // Тот факт, что модуль скомпилировался, — и есть доказательство порядка.
            assertThat(mapper.toDto(entity()).getFullName()).isEqualTo("Иван Иванов");
        }
    }
}
