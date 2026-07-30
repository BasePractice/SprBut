package ru.sprbut.m21.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m21.ambiguous.AmbiguousConfig;
import ru.sprbut.m21.circular.CircularConfig;
import ru.sprbut.m21.missing.MissingBeanConfig;
import ru.sprbut.m21.missing.RepairedBeanConfig;
import ru.sprbut.m21.scopes.PlainScopeConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

@DisplayName("Расширенный пример: диагност падений контекста")
final class HealthTest {

    @Test
    @DisplayName("здоровая конфигурация получает диагноз Healthy")
    void diagnosesHealthyContext() {
        assertThat(
            "working configuration cannot be reported as healthy",
            new Health(RepairedBeanConfig.class).diagnosis(),
            instanceOf(Healthy.class)
        );
    }

    @Test
    @DisplayName("отсутствующий бин распознаётся сквозь обёртки исключений")
    void diagnosesMissingBean() {
        assertThat(
            "missing bean cannot be recognised through wrapper exceptions",
            new Health(MissingBeanConfig.class).diagnosis(),
            instanceOf(MissingBean.class)
        );
    }

    @Test
    @DisplayName("диагноз отсутствующего бина называет искомый тип")
    void namesMissingType() {
        assertThat(
            "missing bean summary cannot name the requested type",
            new Health(MissingBeanConfig.class).diagnosis().summary(),
            containsString("PaymentGateway")
        );
    }

    @Test
    @DisplayName("неоднозначность не путается с отсутствием бина")
    void dontConfuseAmbiguityWithAbsence() {
        assertThat(
            "ambiguous candidates cannot outrank the missing-bean diagnosis",
            new Health(AmbiguousConfig.class).diagnosis(),
            instanceOf(AmbiguousBean.class)
        );
    }

    @Test
    @DisplayName("диагноз неоднозначности перечисляет кандидатов")
    void listsAmbiguousCandidates() {
        assertThat(
            "ambiguity diagnosis cannot list the competing beans",
            new Health(AmbiguousConfig.class).diagnosis().summary(),
            containsString("economy")
        );
    }

    @Test
    @DisplayName("циклическая зависимость распознаётся по своему исключению")
    void diagnosesCircularReference() {
        assertThat(
            "constructor cycle cannot be recognised by the doctor",
            new Health(CircularConfig.class).diagnosis(),
            instanceOf(CircularReference.class)
        );
    }

    @Test
    @DisplayName("совет по циклу предлагает разделение бинов")
    void advisesSplittingOnCycle() {
        assertThat(
            "circular diagnosis cannot advise splitting the beans",
            new Health(CircularConfig.class).diagnosis().remedy(),
            containsString("разделить")
        );
    }

    @Test
    @DisplayName("ошибка области видимости не ломает контекст и потому не диагностируется")
    void dontDiagnoseSilentScopeMistake() {
        assertThat(
            "silent scope mistake cannot leave the context healthy",
            new Health(PlainScopeConfig.class).diagnosis(),
            instanceOf(Healthy.class)
        );
    }
}
