package ru.sprbut.m03;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;

@DisplayName("СХЕМА 1: Class — центр карты Reflection API")
final class ClassApiTest {

    private enum Status { NEW, PAID }

    private record Point(int x, int y) {
    }

    @Test
    @DisplayName("из Class достаются поля")
    void listsFields() {
        assertThat(
            "class cannot yield its declared fields",
            new ClassApi(Order.class).fields(),
            hasItems("id", "customer", "total")
        );
    }

    @Test
    @DisplayName("из Class достаются методы")
    void listsMethods() {
        assertThat(
            "class cannot yield its declared methods",
            new ClassApi(Order.class).methods(),
            hasItems("getId", "addLines", "cancel")
        );
    }

    @Test
    @DisplayName("из Class достаются конструкторы")
    void countsConstructors() {
        assertThat(
            "class cannot yield its constructors",
            new ClassApi(Order.class).constructorCount(),
            greaterThan(1)
        );
    }

    @Test
    @DisplayName("обычный класс распознаётся как class")
    void classifiesClass() {
        assertThat(
            "plain class cannot be classified",
            new TypeKind(Order.class).name(),
            equalTo("class")
        );
    }

    @Test
    @DisplayName("аннотация — тоже интерфейс, поэтому проверять её надо раньше")
    void classifiesAnnotationBeforeInterface() {
        assertThat(
            "annotation cannot be classified before interface",
            new TypeKind(Retention.class).name(),
            equalTo("annotation")
        );
    }

    @Test
    @DisplayName("enum — тоже класс, и его проверка тоже идёт раньше")
    void classifiesEnumBeforeClass() {
        assertThat(
            "enum cannot be classified before class",
            new TypeKind(Status.class).name(),
            equalTo("enum")
        );
    }

    @Test
    @DisplayName("массив распознаётся отдельной категорией")
    void classifiesArray() {
        assertThat(
            "array cannot be classified",
            new TypeKind(String[].class).name(),
            equalTo("array")
        );
    }

    @Test
    @DisplayName("getComponentType() раскрывает тип элемента массива")
    void readsArrayComponentType() {
        assertThat(
            "array cannot reveal its component type",
            new ClassApi(String[].class).componentType(),
            equalTo(String.class)
        );
    }

    @Test
    @DisplayName("вложенный класс знает своего внешнего владельца")
    void knowsEnclosingClass() {
        assertThat(
            "nested class cannot name its owner",
            new ClassApi(Order.PaymentException.class).enclosing(),
            equalTo(Order.class)
        );
    }

    @Test
    @DisplayName("иерархия наследования доходит до Object")
    void walksSuperChain() {
        assertThat(
            "super chain cannot reach Object",
            new ClassApi(ArrayList.class).superChain(),
            contains("ArrayList", "AbstractList", "AbstractCollection", "Object")
        );
    }

    @Test
    @DisplayName("полный набор интерфейсов — основа подбора бина по типу")
    void collectsAllInterfaces() {
        assertThat(
            "inherited interfaces cannot be collected",
            new ClassApi(ArrayList.class).allInterfaces(),
            hasItems("List", "Collection", "Iterable")
        );
    }

    @Test
    @DisplayName("isAssignableFrom читается «слева можно хранить справа»")
    void readsAssignabilityDirection() {
        assertThat(
            "assignability cannot be read left to right",
            new ClassApi(Number.class).canHold(Integer.class),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("обратное направление ложно — это и есть источник путаницы")
    void rejectsReversedAssignability() {
        assertThat(
            "reversed assignability cannot be false",
            new ClassApi(Integer.class).canHold(Number.class),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("у record есть отдельное API компонентов")
    void readsRecordComponents() {
        assertThat(
            "record components cannot be read",
            new ClassApi(Point.class).recordComponents(),
            contains("x", "y")
        );
    }

    @Test
    @DisplayName("у enum читаются константы в порядке объявления")
    void readsEnumConstants() {
        assertThat(
            "enum constants cannot be read in declaration order",
            new ClassApi(Status.class).enumConstants(),
            contains("NEW", "PAID")
        );
    }

    @Test
    @DisplayName("массив создаётся фабрикой Array.newInstance — new здесь неприменим")
    void createsArrayReflectively() {
        assertThat(
            "reflection cannot create an array of a runtime known type",
            ((Map<?, ?>[]) new ClassApi(Map.class).array(3)).length,
            equalTo(3)
        );
    }

    @Test
    @DisplayName("не-record компонентов не имеет")
    void dontReadComponentsOfPlainClass() {
        assertThat(
            "plain class cannot report an empty component list",
            new ClassApi(Order.class).recordComponents(),
            equalTo(List.of())
        );
    }
}
