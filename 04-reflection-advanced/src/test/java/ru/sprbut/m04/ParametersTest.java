package ru.sprbut.m04;

import java.lang.annotation.Retention;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@DisplayName("Слайд 36: Executable, Parameter, AnnotatedElement, Array")
final class ParametersTest {

    @Test
    @DisplayName("конструктор и метод разбираются одним кодом — оба Executable")
    void treatsConstructorAndMethodAlike() throws NoSuchMethodException {
        assertThat(
            "constructor and method cannot share the same parameter code",
            new Parameters(Service.class.getMethod("configure", long.class, String.class))
                .descriptions(),
            contains("long millis", "String label")
        );
    }

    @Test
    @DisplayName("имена параметров сохранены благодаря флагу -parameters")
    void keepsParameterNames() throws NoSuchMethodException {
        assertThat(
            "parameter names cannot survive compilation",
            new Parameters(Service.class.getMethod("configure", long.class, String.class)).named(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("точки внедрения находятся по аннотации на параметре")
    void findsInjectionPoints() throws NoSuchMethodException {
        assertThat(
            "injection points cannot be found by the parameter annotation",
            new Parameters(
                Service.class.getConstructor(String.class, int.class, boolean.class)
            ).injectionPoints(),
            contains("appName", "retries")
        );
    }

    @Test
    @DisplayName("имя из аннотации служит квалификатором")
    void usesAnnotationValueAsQualifier() throws NoSuchMethodException {
        assertThat(
            "annotation value cannot serve as the qualifier",
            new Parameters(Service.class.getMethod("configure", long.class, String.class))
                .injectionPoints(),
            contains("timeout")
        );
    }

    @Test
    @DisplayName("аннотации читаются с любого элемента одинаково")
    void readsAnnotationsFromAnyElement() {
        assertThat(
            "annotations cannot be read from any element the same way",
            new ElementAnnotations(Injected.class).names(),
            hasItem("Retention")
        );
    }

    @Test
    @DisplayName("аннотация на аннотации — тоже обычный AnnotatedElement")
    void readsMetaAnnotation() {
        assertThat(
            "meta annotation cannot be read like any other",
            new ElementAnnotations(Retention.class).names(),
            hasItem("Documented")
        );
    }

    @Test
    @DisplayName("массив с типом элемента из runtime создаётся фабрикой")
    void createsArrayReflectively() {
        assertThat(
            "runtime typed array cannot be created",
            new ArrayValue(new ReflectiveArray(String.class).single(4)).length(),
            equalTo(4)
        );
    }

    @Test
    @DisplayName("многомерный массив создаётся тем же API")
    void createsMatrix() {
        assertThat(
            "matrix cannot be created by the same API",
            ((int[][]) new ReflectiveArray(int.class).matrix(2, 3))[1].length,
            equalTo(3)
        );
    }

    @Test
    @DisplayName("элемент примитивного массива читается без приведения к Object[]")
    void readsPrimitiveElement() {
        Object array = new ReflectiveArray(int.class).single(2);
        ArrayValue value = new ArrayValue(array);
        value.assign(0, 7);
        assertThat(
            "primitive array element cannot be read without a cast",
            value.element(0),
            equalTo(7)
        );
    }

    @Test
    @DisplayName("синтетических параметров у обычного метода нет")
    void reportsNoSyntheticParameters() throws NoSuchMethodException {
        assertThat(
            "plain method cannot report an empty synthetic list",
            new Parameters(Service.class.getMethod("configure", long.class, String.class))
                .synthetic(),
            equalTo(List.of())
        );
    }
}
