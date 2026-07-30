package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд 4: Class.forName — загрузка класса по строке")
final class ClassByNameTest {

    @Test
    @DisplayName("класс находится по строковому имени")
    void loadsClassByName() throws ClassNotFoundException {
        assertThat(
            "class named by string cannot be loaded",
            new ClassByName("ru.sprbut.m01.model.Account").type(),
            sameInstance(Account.class)
        );
    }

    @Test
    @DisplayName("несуществующее имя даёт ClassNotFoundException — связь через строку не проверяется компилятором")
    void dontLoadUnknownClass() {
        assertThrows(
            ClassNotFoundException.class,
            () -> new ClassByName("ru.sprbut.NoSuchClass").type()
        );
    }
}
