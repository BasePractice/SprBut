package ru.sprbut.m01;

import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.sameInstance;

@DisplayName("Слайды 3–5: метаданные класса в runtime")
final class ClassMetadataTest {

    @Test
    @DisplayName("полное имя включает пакет")
    void readsFullName() {
        assertThat(
            "class metadata cannot report the fully qualified name",
            new ClassMetadata(new Account("ACC-1", "Иванов", new BigDecimal("100.00"))).fullName(),
            equalTo("ru.sprbut.m01.model.Account")
        );
    }

    @Test
    @DisplayName("короткое имя пакет не включает")
    void readsSimpleName() {
        assertThat(
            "class metadata cannot report the simple name",
            new ClassMetadata(new Account("ACC-2", "Петров", BigDecimal.ONE)).simpleName(),
            equalTo("Account")
        );
    }

    @Test
    @DisplayName("имя пакета читается отдельно от имени класса")
    void readsPackageName() {
        assertThat(
            "class metadata cannot report the package name",
            new ClassMetadata(Account.class).packageName(),
            equalTo("ru.sprbut.m01.model")
        );
    }

    @Test
    @DisplayName("иерархия наследования доходит до Object")
    void walksHierarchyUpToObject() {
        assertThat(
            "hierarchy cannot reach Object",
            new ClassMetadata(Account.class).hierarchy(),
            contains("Account", "Object")
        );
    }

    @Test
    @DisplayName("иерархия перечисляет все промежуточные классы")
    void listsIntermediateClasses() {
        assertThat(
            "hierarchy cannot list the intermediate superclasses",
            new ClassMetadata(ArrayList.class).hierarchy(),
            contains("ArrayList", "AbstractList", "AbstractCollection", "Object")
        );
    }

    @Test
    @DisplayName("getInterfaces() отдаёт только напрямую реализованные интерфейсы")
    void readsDirectInterfaces() {
        assertThat(
            "direct interfaces cannot be listed",
            new ClassMetadata(ArrayList.class).interfaces(),
            hasItems("List", "RandomAccess", "Cloneable")
        );
    }

    @Test
    @DisplayName("класс без интерфейсов даёт пустой список, а не null")
    void reportsNoInterfaces() {
        assertThat(
            "class without interfaces cannot yield an empty list",
            new ClassMetadata(Account.class).interfaces(),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("Class — единственный объект на загруженный класс")
    void keepsSingleClassObject() {
        assertThat(
            "two ways of getting Class cannot lead to the same object",
            new Account("ACC-3", "Сидоров", BigDecimal.TEN).getClass(),
            sameInstance(Account.class)
        );
    }

    @Test
    @DisplayName("обычный класс инстанцировать можно")
    void detectsInstantiableClass() {
        assertThat(
            "plain class cannot be recognised as instantiable",
            new ClassMetadata(Account.class).instantiable(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("интерфейс инстанцировать нельзя")
    void dontInstantiateInterface() {
        assertThat(
            "interface cannot be rejected as non instantiable",
            new ClassMetadata(List.class).instantiable(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("абстрактный класс инстанцировать нельзя")
    void dontInstantiateAbstractClass() {
        assertThat(
            "abstract class cannot be rejected as non instantiable",
            new ClassMetadata(AbstractList.class).instantiable(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("примитив инстанцировать нельзя")
    void dontInstantiatePrimitive() {
        assertThat(
            "primitive cannot be rejected as non instantiable",
            new ClassMetadata(int.class).instantiable(),
            equalTo(false)
        );
    }
}
