package ru.sprbut.m06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайды 50–52: типы элементов аннотации и значения по умолчанию")
class MemberTypesTest {

    private MemberTypes.Operation operation(String method) throws NoSuchMethodException {
        Method m = MemberTypes.Service.class.getMethod(method);
        return m.getAnnotation(MemberTypes.Operation.class);
    }

    @Test
    @DisplayName("Допустимы примитив, String, Class, enum, вложенная аннотация и массивы из них")
    void supportsAllAllowedMemberTypes() throws NoSuchMethodException {
        var values = MemberTypes.valuesOf(operation("withEverything"));

        assertThat(values)
                .containsEntry("timeout", 5)                                 // примитив
                .containsEntry("readOnly", true)                             // примитив
                .containsEntry("name", "полный")                             // String
                .containsEntry("rollbackFor", "IllegalStateException")       // Class
                .containsEntry("isolation", MemberTypes.Isolation.SERIALIZABLE) // enum
                .containsEntry("retry", "@Retry")                            // вложенная аннотация
                .containsEntry("tags", "[critical, billing]")                // массив String
                .containsEntry("handles", "[String, Integer]")               // массив Class
                .containsEntry("allowed", "[READ_COMMITTED, SERIALIZABLE]"); // массив enum
    }

    @Test
    @DisplayName("Незаданные элементы получают значения из default")
    void defaultsFillTheGaps() throws NoSuchMethodException {
        var values = MemberTypes.valuesOf(operation("withDefaults"));

        assertThat(values)
                .containsEntry("name", "минимум")
                .containsEntry("timeout", 30)
                .containsEntry("readOnly", false)
                .containsEntry("isolation", MemberTypes.Isolation.DEFAULT)
                .containsEntry("rollbackFor", "RuntimeException")
                .containsEntry("tags", "[]");
    }

    @Test
    @DisplayName("Значения по умолчанию читаются с самой аннотации, отдельно от использования")
    void defaultsAreReadableFromTheAnnotationItself() {
        assertThat(MemberTypes.defaultsOf(MemberTypes.Operation.class))
                .containsEntry("timeout", 30)
                .containsEntry("isolation", MemberTypes.Isolation.DEFAULT)
                .containsEntry("name", null);
    }

    @Test
    @DisplayName("Элемент без default обязателен — компилятор потребует его задать")
    void elementsWithoutDefaultAreRequired() {
        assertThat(MemberTypes.requiredElements(MemberTypes.Operation.class))
                .containsExactly("name");
        assertThat(MemberTypes.requiredElements(MemberTypes.Retry.class)).isEmpty();
    }

    @Test
    @DisplayName("Вложенная аннотация читается как полноценный объект")
    void nestedAnnotationIsAnObject() throws NoSuchMethodException {
        MemberTypes.Retry retry = operation("withEverything").retry();

        assertThat(retry.attempts()).isEqualTo(3);
        assertThat(retry.backoffMillis()).isEqualTo(250L);

        // а у варианта с defaults — значения по умолчанию вложенной аннотации
        assertThat(operation("withDefaults").retry().attempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("Элементы аннотации — это методы: их и перечисляет getDeclaredMethods")
    void annotationElementsAreMethods() {
        assertThat(MemberTypes.Operation.class.getDeclaredMethods())
                .extracting(Method::getName)
                .contains("timeout", "name", "rollbackFor", "isolation", "retry", "tags");
        assertThat(MemberTypes.Operation.class.isAnnotation()).isTrue();
        assertThat(MemberTypes.Operation.class.isInterface()).isTrue();
    }
}
