package ru.sprbut.m09.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m09.UserMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: СХЕМА 4 — compile-time против runtime")
class MapperShowcaseTest {

    @Nested
    @DisplayName("Механизм не меняет поведение, только свойства")
    class Behaviour {

        @Test
        @DisplayName("Все три реализации дают идентичный результат")
        void identicalResults() {
            assertThat(MapperShowcase.allProduceSameResult(MapperShowcase.sample())).isTrue();
        }

        @Test
        @DisplayName("Служебное поле не попадает в DTO ни в одной реализации")
        void noneLeaksInternalField() {
            assertThat(MapperShowcase.allMappers())
                    .allSatisfy(mapper -> assertThat(mapper.toDto(MapperShowcase.sample()).toString())
                            .doesNotContain("не должно попасть"));
        }

        @Test
        @DisplayName("Каждая реализация честно называет свою стратегию")
        void everyMapperDeclaresItsStrategy() {
            assertThat(MapperShowcase.allMappers())
                    .extracting(UserMapper::strategy)
                    .anySatisfy(s -> assertThat(s).contains("runtime из метаданных"))
                    .anySatisfy(s -> assertThat(s).contains("этапе компиляции"))
                    .anySatisfy(s -> assertThat(s).contains("собран и загружен в runtime"));
        }
    }

    @Nested
    @DisplayName("Цена вызова")
    class Cost {

        @Test
        @DisplayName("Замер отдаёт время всех трёх реализаций")
        void benchmarkCoversEveryMapper() {
            Map<String, Long> timings = MapperShowcase.benchmark(20_000);

            assertThat(timings).hasSize(3);
            assertThat(timings.values()).allSatisfy(nanos -> assertThat(nanos).isPositive());
        }

        @Test
        @DisplayName("Рефлексия — самая дорогая из трёх")
        void reflectionIsTheSlowest() {
            MapperShowcase.benchmark(20_000); // разогрев
            Map<String, Long> timings = MapperShowcase.benchmark(50_000);

            long reflective = timings.get("ReflectiveMapper");
            long generated = timings.get("GeneratedStyleMapper");

            assertThat(reflective)
                    .as("Method.invoke на каждое свойство дороже прямого вызова")
                    .isGreaterThan(generated);
        }
    }

    @Nested
    @DisplayName("Слайд 77: пригодность для native image")
    class NativeImage {

        @Test
        @DisplayName("Рефлексивному мапперу нужны hints на каждый геттер и сеттер")
        void reflectionNeedsHints() {
            assertThat(MapperShowcase.requiredRuntimeHints().get("ReflectiveMapper"))
                    .contains("UserEntity#getFirstName", "UserDto#setFirstName")
                    .hasSize(10);
        }

        @Test
        @DisplayName("Сгенерированному коду hints не нужны вовсе")
        void generatedCodeNeedsNothing() {
            assertThat(MapperShowcase.requiredRuntimeHints().get("GeneratedStyleMapper")).isEmpty();
        }

        @Test
        @DisplayName("Генерация байткода в runtime в native image невозможна в принципе")
        void bytecodeIsIncompatible() {
            assertThat(MapperShowcase.requiredRuntimeHints().get("BytecodeMapper"))
                    .singleElement()
                    .asString()
                    .contains("native image неприменим");
        }

        @Test
        @DisplayName("В native image выживает только compile-time механизм")
        void onlyCompileTimeSurvives() {
            assertThat(MechanismProfile.survivingInNativeImage()).containsExactly("apt");
        }
    }

    @Nested
    @DisplayName("Слайд 76: Spring использует все три механизма")
    class SpringUsesAll {

        @Test
        @DisplayName("У каждого механизма есть своя роль в Spring")
        void everyMechanismHasARole() {
            assertThat(MechanismProfile.springUsesAllThree()).isTrue();
            assertThat(MechanismProfile.REFLECTION.springUsesItFor())
                    .anyMatch(s -> s.contains("@Autowired"));
            assertThat(MechanismProfile.BYTECODE.springUsesItFor())
                    .anyMatch(s -> s.contains("CGLIB"));
            assertThat(MechanismProfile.APT.springUsesItFor())
                    .anyMatch(s -> s.contains("AOT"));
        }

        @Test
        @DisplayName("Профили расставлены по оси времени: compile-time, runtime и оба сразу")
        void phasesCoverTheWholeAxis() {
            assertThat(MechanismProfile.all())
                    .extracting(MechanismProfile::phase)
                    .containsExactly(MechanismProfile.Phase.RUNTIME,
                            MechanismProfile.Phase.COMPILE_TIME,
                            MechanismProfile.Phase.BOTH);
        }

        @Test
        @DisplayName("Гибкость и типобезопасность — взаимоисключающие свойства")
        void flexibilityAndSafetyAreOpposites() {
            assertThat(MechanismProfile.REFLECTION.flexibleAtRuntime()).isTrue();
            assertThat(MechanismProfile.REFLECTION.typeSafeAtCompileTime()).isFalse();

            assertThat(MechanismProfile.APT.flexibleAtRuntime()).isFalse();
            assertThat(MechanismProfile.APT.typeSafeAtCompileTime()).isTrue();

            // байткод берёт гибкость и скорость, платит типобезопасностью и native image
            assertThat(MechanismProfile.BYTECODE.flexibleAtRuntime()).isTrue();
            assertThat(MechanismProfile.BYTECODE.fastCalls()).isTrue();
            assertThat(MechanismProfile.BYTECODE.nativeImageFriendly()).isFalse();
        }
    }

    @Nested
    @DisplayName("Ради чего терпят рефлексию")
    class Flexibility {

        @Test
        @DisplayName("Рефлексивный маппер находит свойства сам, без единого правила")
        void findsPropertiesWithoutRules() {
            assertThat(MapperShowcase.adaptsWithoutRebuild()).isTrue();
            assertThat(MapperShowcase.discoveredProperties())
                    .containsExactly("active", "age", "firstName", "id", "lastName");
        }
    }
}
