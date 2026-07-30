package ru.sprbut.m06.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.sprbut.m06.Composition;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Расширенный пример: слияние аннотаций как в Spring")
class MergedAnnotationScannerTest {

    private Method endpoint(String name) throws NoSuchMethodException {
        return WebAnnotations.UserController.class.getMethod(name);
    }

    @Nested
    @DisplayName("Поиск сквозь мета-аннотации")
    class Lookup {

        @Test
        @DisplayName("Аннотация найдена прямо на элементе")
        void findsDirectAnnotation() throws NoSuchMethodException {
            var merged = MergedAnnotationScanner
                    .find(endpoint("raw"), WebAnnotations.RequestMapping.class);

            assertThat(merged).isPresent();
            assertThat(merged.get().metaPath()).containsExactly("@RequestMapping");
            assertThat(merged.get().getString("path")).isEqualTo("/raw");
        }

        @Test
        @DisplayName("Аннотация найдена через один уровень композиции")
        void findsThroughOneLevel() throws NoSuchMethodException {
            var merged = MergedAnnotationScanner
                    .find(endpoint("list"), WebAnnotations.RequestMapping.class);

            assertThat(merged).isPresent();
            assertThat(merged.get().metaPath()).containsExactly("@GetMapping", "@RequestMapping");
        }

        @Test
        @DisplayName("Аннотация найдена через два уровня композиции")
        void findsThroughTwoLevels() throws NoSuchMethodException {
            var merged = MergedAnnotationScanner
                    .find(endpoint("json"), WebAnnotations.RequestMapping.class);

            assertThat(merged.orElseThrow().metaPath())
                    .containsExactly("@GetJson", "@GetMapping", "@RequestMapping");
        }

        @Test
        @DisplayName("Метод без аннотаций ничего не даёт")
        void plainMethodFindsNothing() throws NoSuchMethodException {
            assertThat(MergedAnnotationScanner
                    .find(endpoint("plain"), WebAnnotations.RequestMapping.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Слияние значений элементов")
    class Merging {

        @Test
        @DisplayName("@AliasFor переносит value композита в path мета-аннотации")
        void aliasForOverridesByName() throws NoSuchMethodException {
            var merged = MergedAnnotationScanner
                    .find(endpoint("list"), WebAnnotations.RequestMapping.class)
                    .orElseThrow();

            assertThat(merged.getString("path")).isEqualTo("/users");
        }

        @Test
        @DisplayName("Значение из мета-аннотации сохраняется, если композит его не трогает")
        void metaAnnotationValueSurvives() throws NoSuchMethodException {
            var merged = MergedAnnotationScanner
                    .find(endpoint("list"), WebAnnotations.RequestMapping.class)
                    .orElseThrow();

            // method = GET задан на самой @GetMapping и никем не переопределён
            assertThat((WebAnnotations.HttpMethod) merged.get("method")).isEqualTo(WebAnnotations.HttpMethod.GET);
        }

        @Test
        @DisplayName("Одноимённый элемент переопределяет мета-аннотацию и без @AliasFor")
        void sameNameOverridesWithoutAlias() throws NoSuchMethodException {
            var merged = MergedAnnotationScanner
                    .find(endpoint("listActive"), WebAnnotations.RequestMapping.class)
                    .orElseThrow();

            assertThat((String[]) merged.get("produces"))
                    .containsExactly("application/json", "application/xml");
        }

        @Test
        @DisplayName("Незаданный элемент композита не затирает значение мета-аннотации")
        void defaultValueDoesNotOverride() throws NoSuchMethodException {
            // @GetJson задаёт produces = "application/json" на уровне @GetMapping,
            // а сам @GetJson("/users/json") элемент produces не имеет вовсе
            var merged = MergedAnnotationScanner
                    .find(endpoint("json"), WebAnnotations.RequestMapping.class)
                    .orElseThrow();

            assertThat((String[]) merged.get("produces")).containsExactly("application/json");
            assertThat(merged.getString("path")).isEqualTo("/users/json");
        }

        @Test
        @DisplayName("Ближайшая к элементу аннотация выигрывает у дальней")
        void nearestWins() throws NoSuchMethodException {
            var merged = MergedAnnotationScanner
                    .find(endpoint("json"), WebAnnotations.RequestMapping.class)
                    .orElseThrow();

            // path задан на @GetJson (ближе), а не на @GetMapping (дальше)
            assertThat(merged.getString("path")).isEqualTo("/users/json");
            assertThat((WebAnnotations.HttpMethod) merged.get("method")).isEqualTo(WebAnnotations.HttpMethod.GET);
        }

        @Test
        @DisplayName("@AliasFor на несуществующий элемент — ошибка конфигурации, а не тихий сбой")
        void brokenAliasIsReported() throws NoSuchMethodException {
            Method broken = endpoint("broken");

            assertThatThrownBy(() -> MergedAnnotationScanner
                    .find(broken, WebAnnotations.RequestMapping.class))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("нетТакогоЭлемента");
        }
    }

    @Nested
    @DisplayName("Обход иерархии — то, чего @Inherited не умеет")
    class Hierarchy {

        @Composition.RestController("базовый")
        static class Base {
        }

        static class Derived extends Base {
        }

        interface Marked {
        }

        @Composition.Controller("через-интерфейс")
        interface MarkedContract {
        }

        static class ViaInterface implements MarkedContract {
        }

        @Test
        @DisplayName("Аннотация родителя находится вместе с раскрытием мета-аннотаций")
        void findsInheritedThroughMetaAnnotations() {
            var merged = MergedAnnotationScanner
                    .findOnHierarchy(Derived.class, Composition.Controller.class);

            assertThat(merged).isPresent();
            assertThat(merged.get().metaPath()).containsExactly("@RestController", "@Controller");
        }

        @Test
        @DisplayName("Аннотация интерфейса тоже находится — язык на неё не смотрит вовсе")
        void findsAnnotationOnInterface() {
            assertThat(ViaInterface.class.isAnnotationPresent(Composition.Controller.class))
                    .as("язык аннотации интерфейса не наследует")
                    .isFalse();

            assertThat(MergedAnnotationScanner
                    .findOnHierarchy(ViaInterface.class, Composition.Controller.class))
                    .isPresent();
        }

        @Test
        @DisplayName("Класс без аннотаций во всей иерархии ничего не даёт")
        void emptyHierarchyFindsNothing() {
            class Plain implements Marked {
            }

            assertThat(MergedAnnotationScanner
                    .findOnHierarchy(Plain.class, Composition.Controller.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Отладочный вывод")
    class Debugging {

        @Test
        @DisplayName("flatten() показывает всё, что навешано на элемент, включая мета-аннотации")
        void flattensEverything() throws NoSuchMethodException {
            assertThat(MergedAnnotationScanner.flatten(endpoint("json")))
                    .containsExactly("@GetJson", "@GetMapping", "@RequestMapping");
        }

        @Test
        @DisplayName("rawAttributes() отдаёт значения элементов как есть, без слияния")
        void rawAttributesAreUnmerged() throws NoSuchMethodException {
            var getMapping = endpoint("list").getAnnotation(WebAnnotations.GetMapping.class);

            assertThat(MergedAnnotationScanner.rawAttributes(getMapping))
                    .containsEntry("value", "/users")
                    .containsKey("produces");
        }
    }
}
