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

    private static CustomerEntity entity() {
        return new CustomerEntity(
            "C-1", "Иван", "Иванов", LocalDate.of(1984, 3, 15),
            new BigDecimal("100.00"), true, "служебное"
        );
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
            MatcherAssert.assertThat(
                "generated mapper cannot copy the identifier",
                CustomerMapper.INSTANCE.toDto(CustomerMapperTest.entity()).getId(),
                Matchers.equalTo("C-1")
            );
        }

        @Test
        @DisplayName("expression склеивает два поля в одно")
        void expressionCombinesFields() {
            MatcherAssert.assertThat(
                "spring managed mapper cannot work the same way",
                CustomerMapper.INSTANCE.toDto(CustomerMapperTest.entity()).getFullName(),
                Matchers.equalTo("Иван Иванов")
            );
        }

        @Test
        @DisplayName("qualifiedByName вызывает именованный метод преобразования")
        void namedMethodsConvertValues() {
            MatcherAssert.assertThat(
                "named method cannot convert the status",
                CustomerMapper.INSTANCE.toDto(CustomerMapperTest.entity()).getStatus(),
                Matchers.equalTo("VIP")
            );
        }

        @Test
        @DisplayName("Не-VIP получает другой статус")
        void nonVipGetsStandardStatus() {
            final CustomerEntity plain = CustomerMapperTest.entity();
            plain.setVip(false);
            MatcherAssert.assertThat(
                "conditional mapping cannot pick the default status",
                CustomerMapper.INSTANCE.toDto(plain).getStatus(),
                Matchers.equalTo("STANDARD")
            );
        }

        @Test
        @DisplayName("@AfterMapping подставляет значение по умолчанию вместо null")
        void afterMappingNormalizesNulls() {
            final CustomerEntity empty = CustomerMapperTest.entity();
            empty.setBalance(null);
            MatcherAssert.assertThat(
                "missing value cannot fall back to the default",
                CustomerMapper.INSTANCE.toDto(empty).getBalance(),
                Matchers.comparesEqualTo(BigDecimal.ZERO)
            );
        }

        @Test
        @DisplayName("Маппинг списка генерируется автоматически из одиночного метода")
        void mapsCollections() {
            final CustomerEntity next = CustomerMapperTest.entity();
            next.setId("C-2");
            next.setVip(false);
            MatcherAssert.assertThat(
                "collection mapping cannot keep the order",
                CustomerMapper.INSTANCE
                    .toDtos(List.of(CustomerMapperTest.entity(), next))
                    .stream()
                    .map(CustomerDto::getId)
                    .toList(),
                Matchers.contains("C-1", "C-2")
            );
        }

        @Test
        @DisplayName("null на входе — null на выходе")
        void handlesNull() {
            MatcherAssert.assertThat(
                "null input cannot yield null output",
                CustomerMapper.INSTANCE.toDto(null),
                Matchers.nullValue()
            );
        }

        @Test
        @DisplayName("Служебное поле в DTO не попадает — его там просто нет")
        void internalFieldDoesNotLeak() {
            MatcherAssert.assertThat(
                "ignored field cannot stay out of the dto",
                CustomerMapper.INSTANCE.toDto(CustomerMapperTest.entity()).toString(),
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
                CustomerMapper.INSTANCE.getClass().getName(),
                Matchers.containsString("ru.sprbut.m10.extended.CustomerMapperImpl")
            );
        }

        @Test
        @DisplayName("Сгенерированный класс реализует сам интерфейс маппера")
        void implementationFollowsTheInterface() {
            MatcherAssert.assertThat(
                "generated implementation cannot implement the mapper interface",
                CustomerMapper.INSTANCE,
                Matchers.instanceOf(CustomerMapper.class)
            );
        }

        @Test
        @DisplayName("Это обычный класс, а не прокси — рефлексии в нём нет")
        void generatedClassIsNotAProxy() {
            MatcherAssert.assertThat(
                "generated mapper cannot avoid being a proxy",
                Proxy.isProxyClass(CustomerMapper.INSTANCE.getClass()),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("У сгенерированного класса нет состояния")
        void generatedClassIsStateless() {
            MatcherAssert.assertThat(
                "generated mapper cannot stay stateless",
                CustomerMapper.INSTANCE.getClass().getDeclaredFields(),
                Matchers.emptyArray()
            );
        }

        @Test
        @DisplayName("Mappers.getMapper создаёт новый экземпляр на каждый вызов")
        void mappersFactoryCreatesNewInstances() {
            MatcherAssert.assertThat(
                "spring managed mapper cannot differ from the standalone one",
                Mappers.getMapper(CustomerMapper.class),
                Matchers.not(Matchers.sameInstance(CustomerMapper.INSTANCE))
            );
        }

        @Test
        @DisplayName("Оба экземпляра — одного и того же сгенерированного класса")
        void bothInstancesShareTheGeneratedClass() {
            MatcherAssert.assertThat(
                "both instances cannot share the generated class",
                Mappers.getMapper(CustomerMapper.class).getClass(),
                Matchers.equalTo(CustomerMapper.INSTANCE.getClass())
            );
        }

        @Test
        @DisplayName("Lombok отработал раньше MapStruct — иначе геттеров бы не было")
        void lombokRanBeforeMapStruct() {
            MatcherAssert.assertThat(
                "spring managed mapper cannot work the same way",
                CustomerMapper.INSTANCE.toDto(CustomerMapperTest.entity()).getFullName(),
                Matchers.equalTo("Иван Иванов")
            );
        }
    }
}
