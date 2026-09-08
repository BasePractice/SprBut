/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m11.step4;

import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Шаг 4: тот же граф, собранный на этапе компиляции.
 * @since 1.0
 */
@DisplayName("Шаг 4: тот же граф, собранный на этапе компиляции")
final class DaggerWiringTest {

    @Test
    @DisplayName("Сгенерированный компонент собирает тот же граф, что фабрика и Spring")
    void componentAssemblesTheSameGraph() {
        MatcherAssert.assertThat(
            "generated component cannot assemble the same graph",
            DaggerOrderComponent.builder()
                .wiringModule(new WiringModule("email"))
                .build()
                .orderService()
                .placeOrder("ivanov@mail.ru", new BigDecimal("100")),
            Matchers.comparesEqualTo(new BigDecimal("120.00"))
        );
    }

    @Test
    @DisplayName("Привязка со @Singleton живёт в одном экземпляре на компонент")
    void scopedBindingIsASingleton() {
        final OrderComponent component = DaggerOrderComponent.builder()
            .wiringModule(new WiringModule("email"))
            .build();
        MatcherAssert.assertThat(
            "scoped binding cannot stay a singleton within the component",
            component.orderService(),
            Matchers.sameInstance(component.orderService())
        );
    }

    @Test
    @DisplayName("Разные компоненты — разные графы со своими экземплярами")
    void separateComponentsOwnSeparateGraphs() {
        MatcherAssert.assertThat(
            "separate components cannot own separate graphs",
            DaggerOrderComponent.builder()
                .wiringModule(new WiringModule("email"))
                .build()
                .orderService(),
            Matchers.not(
                Matchers.sameInstance(
                    DaggerOrderComponent.builder()
                        .wiringModule(new WiringModule("email"))
                        .build()
                        .orderService()
                )
            )
        );
    }

    @Test
    @DisplayName("Выбор реализации остаётся в модуле — как в ручной фабрике")
    void moduleDecidesTheImplementation() {
        MatcherAssert.assertThat(
            "module cannot concentrate the implementation choice",
            DaggerOrderComponent.builder()
                .wiringModule(new WiringModule("sms"))
                .build()
                .orderService()
                .usedChannel(),
            Matchers.equalTo("sms")
        );
    }

    @Test
    @DisplayName("В рантайме работает сгенерированный класс, а не прокси и не рефлексия")
    void graphIsPlainGeneratedCode() {
        MatcherAssert.assertThat(
            "runtime object cannot be an instance of the generated class",
            DaggerOrderComponent.builder()
                .wiringModule(new WiringModule("email"))
                .build()
                .getClass()
                .getName(),
            Matchers.startsWith("ru.sprbut.m11.step4.DaggerOrderComponent")
        );
    }
}
