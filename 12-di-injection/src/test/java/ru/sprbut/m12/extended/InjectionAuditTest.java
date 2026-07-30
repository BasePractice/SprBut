package ru.sprbut.m12.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m12.injection.ConstructorInjected;
import ru.sprbut.m12.injection.FieldInjected;
import ru.sprbut.m12.injection.SetterInjected;
import ru.sprbut.m12.jakarta.JakartaInjected;
import ru.sprbut.m12.locator.ServiceLocatorDemo;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: аудит точек внедрения")
class InjectionAuditTest {

    @Nested
    @DisplayName("Распознавание способа внедрения")
    class StyleDetection {

        @Test
        @DisplayName("Внедрение через конструктор распознаётся и не даёт замечаний")
        void constructorInjectionIsClean() {
            InjectionAudit.Report report = InjectionAudit.audit(ConstructorInjected.class);

            assertThat(report.primaryStyle()).isEqualTo(InjectionAudit.Style.CONSTRUCTOR);
            assertThat(report.dependencies()).containsExactly("TaxService", "DiscountService");
            assertThat(report.clean()).isTrue();
        }

        @Test
        @DisplayName("Внедрение в поле распознаётся и получает замечание")
        void fieldInjectionIsFlagged() {
            InjectionAudit.Report report = InjectionAudit.audit(FieldInjected.class);

            assertThat(report.styles()).contains(InjectionAudit.Style.FIELD);
            assertThat(report.warnings())
                    .anyMatch(w -> w.contains("нельзя собрать обычным new"))
                    .anyMatch(w -> w.contains("taxService"));
        }

        @Test
        @DisplayName("Внедрение через сеттер распознаётся отдельно")
        void setterInjectionIsDetected() {
            InjectionAudit.Report report = InjectionAudit.audit(SetterInjected.class);

            assertThat(report.styles()).contains(InjectionAudit.Style.SETTER);
            assertThat(InjectionAudit.injectedSetters(SetterInjected.class))
                    .extracting(java.lang.reflect.Method::getName)
                    .containsExactly("setDiscountService", "setTaxService");
        }

        @Test
        @DisplayName("Service Locator ловится по ApplicationContextAware")
        void serviceLocatorIsFlagged() {
            InjectionAudit.Report report = InjectionAudit.audit(ServiceLocatorDemo.class);

            assertThat(report.styles()).contains(InjectionAudit.Style.SERVICE_LOCATOR);
            assertThat(report.warnings()).anyMatch(w -> w.contains("Service Locator"));
        }

        @Test
        @DisplayName("jakarta-аннотации учитываются наравне со спринговыми")
        void jakartaAnnotationsAreRecognised() {
            InjectionAudit.Report report = InjectionAudit.audit(JakartaInjected.class);

            assertThat(report.styles())
                    .contains(InjectionAudit.Style.CONSTRUCTOR, InjectionAudit.Style.FIELD);
            assertThat(report.dependencies()).contains("TaxService", "DiscountService");
        }
    }

    @Nested
    @DisplayName("Тестируемость без контейнера")
    class Testability {

        @Test
        @DisplayName("Конструкторное внедрение — тестируемо")
        void constructorInjectionIsTestable() {
            assertThat(InjectionAudit.audit(ConstructorInjected.class).testableWithoutContainer())
                    .isTrue();
        }

        @Test
        @DisplayName("Полевое внедрение и Service Locator — нет")
        void othersAreNot() {
            assertThat(InjectionAudit.audit(FieldInjected.class).testableWithoutContainer()).isFalse();
            assertThat(InjectionAudit.audit(ServiceLocatorDemo.class).testableWithoutContainer())
                    .isFalse();
        }

        @Test
        @DisplayName("final-поля есть только при внедрении через конструктор")
        void finalFieldsRequireConstructorInjection() {
            assertThat(InjectionAudit.audit(ConstructorInjected.class).allFieldsFinal()).isTrue();
            assertThat(InjectionAudit.audit(FieldInjected.class).allFieldsFinal()).isFalse();
            assertThat(InjectionAudit.audit(SetterInjected.class).allFieldsFinal()).isFalse();
        }
    }

    @Nested
    @DisplayName("Дополнительные правила")
    class ExtraRules {

        @SuppressWarnings("unused")
        static class TooManyDependencies {
            TooManyDependencies(String a, Integer b, Long c, Double d, Boolean e, Byte f) {
            }
        }

        @SuppressWarnings("unused")
        static class MixedStyles {
            private final String constructorDependency;

            @org.springframework.beans.factory.annotation.Autowired
            private Integer fieldDependency;

            MixedStyles(String constructorDependency) {
                this.constructorDependency = constructorDependency;
            }
        }

        @Test
        @DisplayName("Слишком много зависимостей — сигнал, что класс делает лишнее")
        void tooManyDependenciesAreFlagged() {
            InjectionAudit.Report report = InjectionAudit.audit(TooManyDependencies.class);

            assertThat(report.dependencies()).hasSize(6);
            assertThat(report.warnings()).anyMatch(w -> w.contains("слишком много"));
        }

        @Test
        @DisplayName("Смешение способов внедрения в одном классе — тоже замечание")
        void mixedStylesAreFlagged() {
            InjectionAudit.Report report = InjectionAudit.audit(MixedStyles.class);

            assertThat(report.styles())
                    .contains(InjectionAudit.Style.CONSTRUCTOR, InjectionAudit.Style.FIELD);
            assertThat(report.warnings()).anyMatch(w -> w.contains("смешаны способы"));
        }

        @Test
        @DisplayName("Класс без зависимостей проходит аудит чисто")
        void noDependenciesIsFine() {
            InjectionAudit.Report report = InjectionAudit.audit(ru.sprbut.m12.domain.TaxService.class);

            assertThat(report.dependencies()).isEmpty();
            assertThat(report.primaryStyle()).isEqualTo(InjectionAudit.Style.NONE);
            assertThat(report.clean()).isTrue();
        }
    }
}
