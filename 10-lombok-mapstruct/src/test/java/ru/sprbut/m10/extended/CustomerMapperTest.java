/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m10.extended;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sprbut.m10.lombok.CustomerDto;
import ru.sprbut.m10.lombok.CustomerEntity;

/**
 * Расширенный пример: MapStruct поверх Lombok.
 * @since 1.0
 */
@DisplayName("Расширенный пример: MapStruct поверх Lombok")
final class CustomerMapperTest {

    /**
     * Маппер.
     * @since 1.0
     */
    private final CustomerMapper mapper = CustomerMapper.INSTANCE;

    private CustomerEntity entity() {
        return new CustomerEntity("C-1", "Иван", "Иванов",
                LocalDate.of(
                    1984, 3, 15
                ), new BigDecimal(
                    "100.00"
                ), true, "служебное");
    }

    /**
     * Маппинг работает.
     * @since 1.0
     */
    @Nested
    @DisplayName("Маппинг работает")
    final class Mapping {

        @Test
        @DisplayName("Одноимённые свойства копируются напрямую")
        void copiesMatchingProperties() {
            final CustomerDto dto = CustomerMapperTest.this.mapper.toDto(CustomerMapperTest.this.entity());
            MatcherAssert.assertThat(
                "generated mapper cannot copy the identifier",
                dto.getId(),
                Matchers.equalTo("C-1")
            );
        }

        @Test
        @DisplayName("expression склеивает два поля в одно")
        void expressionCombinesFields() {
            MatcherAssert.assertThat(
                "spring managed mapper cannot work the same way",
                CustomerMapperTest.this.mapper.toDto(CustomerMapperTest.this.entity()).getFullName(),
                Matchers.equalTo("Иван Иванов")
            );
        }

        @Test
        @DisplayName("qualifiedByName вызывает именованный метод преобразования")
        void namedMethodsConvertValues() {
            final CustomerDto dto = CustomerMapperTest.this.mapper.toDto(CustomerMapperTest.this.entity());
            MatcherAssert.assertThat(
                "named method cannot convert the status",
                dto.getStatus(),
                Matchers.equalTo("VIP")
            );
        }

        @Test
        @DisplayName("Не-VIP получает другой статус")
        void nonVipGetsStandardStatus() {
            final CustomerEntity plain = CustomerMapperTest.this.entity();
            plain.setVip(false);
            MatcherAssert.assertThat(
                "conditional mapping cannot pick the default status",
                CustomerMapperTest.this.mapper.toDto(plain).getStatus(),
                Matchers.equalTo("STANDARD")
            );
        }

        @Test
        @DisplayName("@AfterMapping подставляет значение по умолчанию вместо null")
        void afterMappingNormalizesNulls() {
            final CustomerEntity noBalance = CustomerMapperTest.this.entity();
            noBalance.setBalance(null);
            MatcherAssert.assertThat(
                "missing value cannot fall back to the default",
                CustomerMapperTest.this.mapper.toDto(noBalance).getBalance(),
                Matchers.comparesEqualTo(new BigDecimal("0"))
            );
        }

        @Test
        @DisplayName("Маппинг списка генерируется автоматически из одиночного метода")
        void mapsCollections() {
            final CustomerEntity second = CustomerMapperTest.this.entity();
            second.setId("C-2");
            second.setVip(false);
            final List<CustomerDto> dtos = CustomerMapperTest.this.mapper.toDtos(List.of(CustomerMapperTest.this.entity(), second));
            MatcherAssert.assertThat(
                "collection mapping cannot keep the order",
                dtos.stream().map(CustomerDto::getId).toList(),
                Matchers.contains("C-1", "C-2")
            );
        }

        @Test
        @DisplayName("null на входе — null на выходе")
        void handlesNull() {
            MatcherAssert.assertThat(
                "null input cannot yield null output",
                CustomerMapperTest.this.mapper.toDto(null),
                Matchers.nullValue()
            );
        }

        @Test
        @DisplayName("Служебное поле в DTO не попадает — его там просто нет")
        void internalFieldDoesNotLeak() {
            MatcherAssert.assertThat(
                "ignored field cannot stay out of the dto",
                CustomerMapperTest.this.mapper.toDto(CustomerMapperTest.this.entity()).toString(),
                Matchers.not(Matchers.containsString("служебное"))
            );
        }
    }

    /**
     * Что именно сгенерировано.
     * @since 1.0
     */
    @Nested
    @DisplayName("Что именно сгенерировано")
    final class Generated {

        @Test
        @DisplayName("Реализация интерфейса появилась при компиляции")
        void implementationIsGenerated() {
            MatcherAssert.assertThat(
                "generated class cannot follow the naming rule",
                CustomerMapperTest.this.mapper.getClass().getName(),
                Matchers.containsString("ru.sprbut.m10.extended.CustomerMapperImpl")
            );
            MatcherAssert.assertThat(
                "generated implementation cannot implement the mapper interface",
                CustomerMapperTest.this.mapper,
                Matchers.instanceOf(CustomerMapper.class)
            );
        }

        @Test
        @DisplayName("Это обычный класс, а не прокси — рефлексии в нём нет")
        void generatedClassIsNotAProxy() {
            MatcherAssert.assertThat(
                "generated mapper cannot avoid being a proxy",
                Proxy.isProxyClass(CustomerMapperTest.this.mapper.getClass()),
                Matchers.equalTo(false)
            );
            MatcherAssert.assertThat(
                "generated mapper cannot stay stateless",
                CustomerMapperTest.this.mapper.getClass().getDeclaredFields(),
                Matchers.emptyArray()
            );
        }

        @Test
        @DisplayName("Mappers.getMapper создаёт новый экземпляр на каждый вызов — кэшировать надо самому")
        void mappersFactoryCreatesNewInstances() {
            final CustomerMapper first = Mappers.getMapper(CustomerMapper.class);
            final CustomerMapper second = Mappers.getMapper(CustomerMapper.class);
            MatcherAssert.assertThat(
                "spring managed mapper cannot differ from the standalone one",
                first,
                Matchers.not(Matchers.sameInstance(CustomerMapper.INSTANCE))
            );
            MatcherAssert.assertThat(
                "both instances cannot share the generated class",
                first.getClass(),
                Matchers.equalTo(CustomerMapper.INSTANCE.getClass())
            );
            // Именно поэтому в Spring-проектах используют componentModel = "spring":
            // тогда маппер становится бином, и его жизненным циклом управляет контейнер
        }

        @Test
        @DisplayName("Lombok отработал раньше MapStruct — иначе геттеров бы не было")
        void lombokRanBeforeMapStruct() {
            // MapStruct вызывает entity.getFirstName(), которого нет в исходнике.
            // Тот факт, что модуль скомпилировался, — и есть доказательство порядка.
            MatcherAssert.assertThat(
                "spring managed mapper cannot work the same way",
                CustomerMapperTest.this.mapper.toDto(CustomerMapperTest.this.entity()).getFullName(),
                Matchers.equalTo("Иван Иванов")
            );
        }
    }
}
