package ru.sprbut.m10.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m10.lombok.CustomerDto;
import ru.sprbut.m10.lombok.CustomerEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

@DisplayName("Расширенный пример: MapStruct поверх Lombok")
final class CustomerMapperTest {

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

            assertThat(
                "generated mapper cannot copy the identifier",
                dto.getId(),
                equalTo("C-1")
            );
        }

        @Test
        @DisplayName("expression склеивает два поля в одно")
        void expressionCombinesFields() {
            assertThat(
                "spring managed mapper cannot work the same way",
                mapper.toDto(entity()).getFullName(),
                equalTo("Иван Иванов")
            );
        }

        @Test
        @DisplayName("qualifiedByName вызывает именованный метод преобразования")
        void namedMethodsConvertValues() {
            CustomerDto dto = mapper.toDto(entity());

            assertThat(
                "named method cannot convert the status",
                dto.getStatus(),
                equalTo("VIP")
            );
        }

        @Test
        @DisplayName("Не-VIP получает другой статус")
        void nonVipGetsStandardStatus() {
            CustomerEntity plain = entity();
            plain.setVip(false);

            assertThat(
                "conditional mapping cannot pick the default status",
                mapper.toDto(plain).getStatus(),
                equalTo("STANDARD")
            );
        }

        @Test
        @DisplayName("@AfterMapping подставляет значение по умолчанию вместо null")
        void afterMappingNormalizesNulls() {
            CustomerEntity noBalance = entity();
            noBalance.setBalance(null);

            assertThat(
                "missing value cannot fall back to the default",
                mapper.toDto(noBalance).getBalance(),
                comparesEqualTo(new java.math.BigDecimal("0"))
            );
        }

        @Test
        @DisplayName("Маппинг списка генерируется автоматически из одиночного метода")
        void mapsCollections() {
            CustomerEntity second = entity();
            second.setId("C-2");
            second.setVip(false);

            List<CustomerDto> dtos = mapper.toDtos(List.of(entity(), second));

            assertThat(
                "collection mapping cannot keep the order",
                dtos.stream().map(CustomerDto::getId).toList(),
                contains("C-1", "C-2")
            );
        }

        @Test
        @DisplayName("null на входе — null на выходе")
        void handlesNull() {
            assertThat(
                "null input cannot yield null output",
                mapper.toDto(null),
                nullValue()
            );
        }

        @Test
        @DisplayName("Служебное поле в DTO не попадает — его там просто нет")
        void internalFieldDoesNotLeak() {
            assertThat(
                "ignored field cannot stay out of the dto",
                mapper.toDto(entity()).toString(),
                not(containsString("служебное"))
            );
        }
    }

    @Nested
    @DisplayName("Что именно сгенерировано")
    class Generated {

        @Test
        @DisplayName("Реализация интерфейса появилась при компиляции")
        void implementationIsGenerated() {
            assertThat(
                "generated class cannot follow the naming rule",
                mapper.getClass().getName(),
                containsString("ru.sprbut.m10.extended.CustomerMapperImpl")
            );
            assertThat(
                "generated implementation cannot implement the mapper interface",
                mapper,
                instanceOf(CustomerMapper.class)
            );
        }

        @Test
        @DisplayName("Это обычный класс, а не прокси — рефлексии в нём нет")
        void generatedClassIsNotAProxy() {
            assertThat(
                "generated mapper cannot avoid being a proxy",
                java.lang.reflect.Proxy.isProxyClass(mapper.getClass()),
                equalTo(false)
            );
            assertThat(
                "generated mapper cannot stay stateless",
                mapper.getClass().getDeclaredFields(),
                emptyArray()
            );
        }

        @Test
        @DisplayName("Mappers.getMapper создаёт новый экземпляр на каждый вызов — кэшировать надо самому")
        void mappersFactoryCreatesNewInstances() {
            CustomerMapper first = org.mapstruct.factory.Mappers.getMapper(CustomerMapper.class);
            CustomerMapper second = org.mapstruct.factory.Mappers.getMapper(CustomerMapper.class);

            assertThat(
                "spring managed mapper cannot differ from the standalone one",
                first,
                not(sameInstance(CustomerMapper.INSTANCE))
            );
            assertThat(
                "both instances cannot share the generated class",
                first.getClass(),
                equalTo(CustomerMapper.INSTANCE.getClass())
            );

            // Именно поэтому в Spring-проектах используют componentModel = "spring":
            // тогда маппер становится бином, и его жизненным циклом управляет контейнер
        }

        @Test
        @DisplayName("Lombok отработал раньше MapStruct — иначе геттеров бы не было")
        void lombokRanBeforeMapStruct() {
            // MapStruct вызывает entity.getFirstName(), которого нет в исходнике.
            // Тот факт, что модуль скомпилировался, — и есть доказательство порядка.
            assertThat(
                "spring managed mapper cannot work the same way",
                mapper.toDto(entity()).getFullName(),
                equalTo("Иван Иванов")
            );
        }
    }
}
