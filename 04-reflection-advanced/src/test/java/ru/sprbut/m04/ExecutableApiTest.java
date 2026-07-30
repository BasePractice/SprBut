package ru.sprbut.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 36: Parameter, Executable, AnnotatedElement, Array")
class ExecutableApiTest {

    private Constructor<?> serviceConstructor() {
        return ExecutableApi.Service.class.getConstructors()[0];
    }

    private Method configureMethod() throws NoSuchMethodException {
        return ExecutableApi.Service.class.getMethod("configure", long.class, String.class);
    }

    @Test
    @DisplayName("Executable — общий родитель Method и Constructor: один код на оба")
    void oneCodeForMethodAndConstructor() throws NoSuchMethodException {
        Executable ctor = serviceConstructor();
        Executable method = configureMethod();

        assertThat(ExecutableApi.describeParameters(ctor))
                .containsExactly("@Inject(appName) String name", "@Inject() int retries", "boolean debug");
        assertThat(ExecutableApi.describeParameters(method))
                .containsExactly("@Inject(timeout) long millis", "String label");
    }

    @Test
    @DisplayName("Имена параметров есть, потому что модуль собран с -parameters")
    void parameterNamesAreAvailable() throws NoSuchMethodException {
        assertThat(ExecutableApi.parameterNamesArePresent(configureMethod())).isTrue();
    }

    @Test
    @DisplayName("Точки внедрения находятся по аннотации параметра")
    void findsInjectionPoints() throws NoSuchMethodException {
        assertThat(ExecutableApi.injectionPoints(serviceConstructor()))
                .containsExactly("appName", "int");
        assertThat(ExecutableApi.injectionPoints(configureMethod()))
                .containsExactly("timeout");
    }

    @Test
    @DisplayName("AnnotatedElement одинаково работает для класса, метода и параметра")
    void readsAnnotationsFromAnyElement() throws NoSuchMethodException {
        assertThat(ExecutableApi.annotationsOf(
                configureMethod().getParameters()[0])).containsExactly("Inject");
        assertThat(ExecutableApi.annotationsOf(ExecutableApi.Inject.class))
                .contains("Retention", "Target");
    }

    @Test
    @DisplayName("Array.newInstance создаёт массив с типом элемента, известным только в runtime")
    void createsArrayDynamically() {
        Object array = ExecutableApi.createArray(String.class, 2);

        assertThat(array.getClass()).isEqualTo(String[].class);
        assertThat(ExecutableApi.lengthOf(array)).isEqualTo(2);

        ExecutableApi.setElement(array, 0, "первый");
        assertThat(ExecutableApi.getElement(array, 0)).isEqualTo("первый");
    }

    @Test
    @DisplayName("Array работает и с примитивами, где Object[] неприменим")
    void worksWithPrimitiveArrays() {
        Object primitives = ExecutableApi.createArray(int.class, 3);

        assertThat(primitives).isInstanceOf(int[].class).isNotInstanceOf(Object[].class);

        ExecutableApi.setElement(primitives, 1, 42);
        assertThat(ExecutableApi.getElement(primitives, 1)).isEqualTo(42);
        assertThat(((int[]) primitives)[1]).isEqualTo(42);
    }

    @Test
    @DisplayName("Многомерный массив создаётся тем же API")
    void createsMatrix() {
        Object matrix = ExecutableApi.createMatrix(double.class, 2, 3);

        assertThat(matrix).isInstanceOf(double[][].class);
        assertThat(ExecutableApi.lengthOf(matrix)).isEqualTo(2);
        assertThat(ExecutableApi.lengthOf(ExecutableApi.getElement(matrix, 0))).isEqualTo(3);
    }
}
