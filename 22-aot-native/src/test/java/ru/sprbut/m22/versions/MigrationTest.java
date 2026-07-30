package ru.sprbut.m22.versions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

@DisplayName("Слайд «Версии»: переезд javax в jakarta")
final class MigrationTest {

    @Test
    @DisplayName("аннотации Java EE переезжают в jakarta")
    void movesAnnotationsToJakarta() {
        assertThat(
            "javax annotation package cannot move to jakarta",
            new Migration("javax.annotation.PostConstruct").target(),
            equalTo("jakarta.annotation.PostConstruct")
        );
    }

    @Test
    @DisplayName("сервлеты и персистентность переезжают вместе со всем Java EE")
    void movesPersistenceToJakarta() {
        assertThat(
            "javax persistence package cannot move to jakarta",
            new Migration("javax.persistence.Entity").target(),
            equalTo("jakarta.persistence.Entity")
        );
    }

    @Test
    @DisplayName("javax.annotation.processing остаётся на месте — это JDK, а не Java EE")
    void dontMoveAnnotationProcessing() {
        assertThat(
            "annotation processing API cannot stay in javax",
            new Migration("javax.annotation.processing.Processor").target(),
            startsWith("javax.")
        );
    }

    @Test
    @DisplayName("чужие пакеты переезд не трогает")
    void dontTouchForeignPackages() {
        assertThat(
            "unrelated package cannot survive the migration untouched",
            new Migration("org.springframework.stereotype.Service").target(),
            equalTo("org.springframework.stereotype.Service")
        );
    }
}
