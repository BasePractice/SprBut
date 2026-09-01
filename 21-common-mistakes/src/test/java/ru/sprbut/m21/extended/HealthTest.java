/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m21.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m21.ambiguous.AmbiguousConfig;
import ru.sprbut.m21.circular.CircularConfig;
import ru.sprbut.m21.missing.MissingBeanConfig;
import ru.sprbut.m21.missing.RepairedBeanConfig;
import ru.sprbut.m21.scopes.PlainScopeConfig;

/**
 * Расширенный пример: диагност падений контекста.
 * @since 1.0
 */
@DisplayName("Расширенный пример: диагност падений контекста")
final class HealthTest {

    @Test
    @DisplayName("здоровая конфигурация получает диагноз Healthy")
    void diagnosesHealthyContext() {
        MatcherAssert.assertThat(
            "working configuration cannot be reported as healthy",
            new Health(RepairedBeanConfig.class).diagnosis(),
            Matchers.instanceOf(Healthy.class)
        );
    }

    @Test
    @DisplayName("отсутствующий бин распознаётся сквозь обёртки исключений")
    void diagnosesMissingBean() {
        MatcherAssert.assertThat(
            "missing bean cannot be recognised through wrapper exceptions",
            new Health(MissingBeanConfig.class).diagnosis(),
            Matchers.instanceOf(MissingBean.class)
        );
    }

    @Test
    @DisplayName("диагноз отсутствующего бина называет искомый тип")
    void namesMissingType() {
        MatcherAssert.assertThat(
            "missing bean summary cannot name the requested type",
            new Health(MissingBeanConfig.class).diagnosis().summary(),
            Matchers.containsString("PaymentGateway")
        );
    }

    @Test
    @DisplayName("неоднозначность не путается с отсутствием бина")
    void dontConfuseAmbiguityWithAbsence() {
        MatcherAssert.assertThat(
            "ambiguous candidates cannot outrank the missing-bean diagnosis",
            new Health(AmbiguousConfig.class).diagnosis(),
            Matchers.instanceOf(AmbiguousBean.class)
        );
    }

    @Test
    @DisplayName("диагноз неоднозначности перечисляет кандидатов")
    void listsAmbiguousCandidates() {
        MatcherAssert.assertThat(
            "ambiguity diagnosis cannot list the competing beans",
            new Health(AmbiguousConfig.class).diagnosis().summary(),
            Matchers.containsString("economy")
        );
    }

    @Test
    @DisplayName("циклическая зависимость распознаётся по своему исключению")
    void diagnosesCircularReference() {
        MatcherAssert.assertThat(
            "constructor cycle cannot be recognised by the doctor",
            new Health(CircularConfig.class).diagnosis(),
            Matchers.instanceOf(CircularReference.class)
        );
    }

    @Test
    @DisplayName("совет по циклу предлагает разделение бинов")
    void advisesSplittingOnCycle() {
        MatcherAssert.assertThat(
            "circular diagnosis cannot advise splitting the beans",
            new Health(CircularConfig.class).diagnosis().remedy(),
            Matchers.containsString("разделить")
        );
    }

    @Test
    @DisplayName("ошибка области видимости не ломает контекст и потому не диагностируется")
    void dontDiagnoseSilentScopeMistake() {
        MatcherAssert.assertThat(
            "silent scope mistake cannot leave the context healthy",
            new Health(PlainScopeConfig.class).diagnosis(),
            Matchers.instanceOf(Healthy.class)
        );
    }
}
