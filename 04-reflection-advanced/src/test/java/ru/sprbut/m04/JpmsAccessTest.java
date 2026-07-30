package ru.sprbut.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты полагаются на {@code surefire.argLine} из корневого pom.xml, где открыты
 * {@code java.lang}, {@code java.util} и {@code java.time}, но <b>не</b> {@code java.io}.
 * Именно на этой разнице и строится демонстрация.
 */
@DisplayName("Слайд 31: setAccessible и JPMS — нужен --add-opens")
class JpmsAccessTest {

    @Test
    @DisplayName("Пакет java.io экспортирован, но не открыт — глубокая рефлексия запрещена")
    void closedPackageRejectsDeepReflection() {
        assertThat(JpmsAccess.isExportedToUs(File.class)).isTrue();
        assertThat(JpmsAccess.isOpenToUs(File.class)).isFalse();

        JpmsAccess.AccessAttempt attempt = JpmsAccess.tryOpen(File.class, "path");

        assertThat(attempt.succeeded()).isFalse();
        assertThat(attempt.failureType()).isEqualTo("InaccessibleObjectException");
        assertThat(attempt.message()).contains("module java.base does not \"opens java.io\"");
    }

    @Test
    @DisplayName("java.lang открыт флагом --add-opens — та же операция проходит")
    void openedPackageAllowsDeepReflection() {
        assertThat(JpmsAccess.isOpenToUs(String.class)).isTrue();
        assertThat(JpmsAccess.tryOpen(String.class, "value").succeeded()).isTrue();
    }

    @Test
    @DisplayName("Успех зависит от пакета, а не от модификатора поля")
    void accessDependsOnPackageNotModifier() {
        // оба поля private, но результат разный
        assertThat(JpmsAccess.tryOpen(String.class, "value").succeeded()).isTrue();
        assertThat(JpmsAccess.tryOpen(File.class, "path").succeeded()).isFalse();
    }

    @Test
    @DisplayName("Свой класс из безымянного модуля открыт всегда")
    void ownClassesAreAlwaysOpen() {
        assertThat(JpmsAccess.isOpenToUs(JpmsAccess.OurOwnClass.class)).isTrue();
        assertThat(JpmsAccess.tryOpen(JpmsAccess.OurOwnClass.class, "secret").succeeded()).isTrue();
    }

    @Test
    @DisplayName("Код с classpath живёт в безымянном модуле — у него нет имени")
    void classpathCodeIsInUnnamedModule() {
        assertThat(JpmsAccess.moduleNameOf(String.class)).isEqualTo("java.base");
        assertThat(JpmsAccess.moduleNameOf(JpmsAccess.class)).isNull();
    }

    @Test
    @DisplayName("Несуществующее поле — другая ошибка, не связанная с JPMS")
    void missingFieldIsADifferentProblem() {
        JpmsAccess.AccessAttempt attempt = JpmsAccess.tryOpen(String.class, "нетТакогоПоля");

        assertThat(attempt.failureType()).isEqualTo("NoSuchFieldException");
    }
}
