package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайды 3–5: метаданные класса в runtime")
class ClassMetadataTest {

    private final Account account = new Account("ACC-1", "Иванов", new BigDecimal("100.00"));

    @Test
    @DisplayName("getName() отдаёт полное имя с пакетом, getSimpleName() — короткое")
    void readsNames() {
        assertThat(ClassMetadata.fullName(account)).isEqualTo("ru.sprbut.m01.model.Account");
        assertThat(ClassMetadata.simpleName(account)).isEqualTo("Account");
        assertThat(ClassMetadata.packageName(account)).isEqualTo("ru.sprbut.m01.model");
    }

    @Test
    @DisplayName("Иерархия наследования доходит до Object — по ней фреймворки ищут поля родителей")
    void walksHierarchy() {
        assertThat(ClassMetadata.hierarchy(account)).containsExactly("Account", "Object");
        assertThat(ClassMetadata.hierarchyOf(java.util.ArrayList.class))
                .containsExactly("ArrayList", "AbstractList", "AbstractCollection", "Object");
    }

    @Test
    @DisplayName("getInterfaces() возвращает только напрямую реализованные интерфейсы")
    void readsDirectInterfaces() {
        assertThat(ClassMetadata.directInterfaces(Account.class)).isEmpty();
        assertThat(ClassMetadata.directInterfaces(java.util.ArrayList.class))
                .contains("List", "RandomAccess", "Cloneable");
    }

    @Test
    @DisplayName("Class.forName() загружает класс по строке — так читаются конфиги с именами классов")
    void loadsClassByName() throws ClassNotFoundException {
        Class<?> loaded = ClassMetadata.byName("ru.sprbut.m01.model.Account");

        assertThat(loaded).isSameAs(Account.class);
        assertThatThrownBy(() -> ClassMetadata.byName("ru.sprbut.NoSuchClass"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("Три способа получить Class дают один и тот же объект")
    void classObjectIsASingleton() throws ClassNotFoundException {
        assertThat(Account.class)
                .isSameAs(account.getClass())
                .isSameAs(Class.forName(Account.class.getName(), false, getClass().getClassLoader()));
    }

    @Test
    @DisplayName("Интерфейсы, абстрактные классы и примитивы инстанцировать нельзя")
    void detectsInstantiableTypes() {
        assertThat(ClassMetadata.isInstantiable(Account.class)).isTrue();
        assertThat(ClassMetadata.isInstantiable(List.class)).isFalse();
        assertThat(ClassMetadata.isInstantiable(java.util.AbstractList.class)).isFalse();
        assertThat(ClassMetadata.isInstantiable(int.class)).isFalse();
        assertThat(ClassMetadata.isInstantiable(String[].class)).isFalse();
    }
}
