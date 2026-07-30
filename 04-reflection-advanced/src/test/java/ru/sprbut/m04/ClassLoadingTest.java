package ru.sprbut.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 30: X.class, obj.getClass(), Class.forName()")
class ClassLoadingTest {

    @Test
    @DisplayName("Три способа дают один и тот же объект Class")
    void allThreeWaysAgree() throws ClassNotFoundException {
        Object text = "строка";

        assertThat(ClassLoading.byLiteral())
                .isSameAs(ClassLoading.byInstance(text))
                .isSameAs(ClassLoading.byName("java.lang.String"));
    }

    @Test
    @DisplayName("getClass() отдаёт фактический тип, а не тип переменной")
    void instanceGivesActualType() {
        Object asObject = java.util.List.of(1, 2, 3);

        assertThat(ClassLoading.byInstance(asObject)).isNotEqualTo(java.util.List.class);
        assertThat(java.util.List.class.isAssignableFrom(ClassLoading.byInstance(asObject))).isTrue();
    }

    @Test
    @DisplayName("forName() падает в runtime — компилятор строку не проверяет")
    void forNameFailsAtRuntime() {
        assertThatThrownBy(() -> ClassLoading.byName("ru.sprbut.НетТакогоКласса"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("forName(name, false, loader) загружает класс, не выполняя статический блок")
    void skipsInitialization() throws ClassNotFoundException {
        ClassLoading.initialized = false;
        String name = ClassLoading.WithStaticInit.class.getName();

        ClassLoading.byNameWithoutInit(name, getClass().getClassLoader());
        assertThat(ClassLoading.initialized).as("статический блок не должен был выполниться").isFalse();

        ClassLoading.byName(name);
        assertThat(ClassLoading.initialized).as("forName по умолчанию инициализирует класс").isTrue();
    }

    @Test
    @DisplayName("int.class == Integer.TYPE, но не Integer.class")
    void primitiveClassIsDistinct() {
        assertThat(ClassLoading.primitiveClassDiffersFromWrapper()).isTrue();
        assertThat(int.class.getName()).isEqualTo("int");
        assertThat(Integer.class.getName()).isEqualTo("java.lang.Integer");
    }

    @Test
    @DisplayName("Имя класса массива записано в JVM-нотации")
    void arrayNameUsesJvmNotation() {
        assertThat(ClassLoading.jvmArrayName()).isEqualTo("[Ljava.lang.String;");
        assertThat(String[].class.getSimpleName()).isEqualTo("String[]");
        assertThat(int[].class.getName()).isEqualTo("[I");
    }

    @Test
    @DisplayName("У классов java.base загрузчик равен null — это bootstrap loader")
    void bootstrapLoaderIsNull() {
        assertThat(ClassLoading.loaderOf(String.class)).isNull();
        assertThat(ClassLoading.loaderOf(ClassLoading.class)).isNotNull();
    }
}
