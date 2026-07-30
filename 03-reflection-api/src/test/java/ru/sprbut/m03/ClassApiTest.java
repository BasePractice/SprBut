package ru.sprbut.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("СХЕМА 1: Class — центр карты Reflection API")
class ClassApiTest {

    enum Colour { RED, GREEN }

    record Point(int x, int y) {
    }

    @Test
    @DisplayName("Из Class достаются все остальные узлы: поля, методы, конструкторы")
    void classIsTheEntryPoint() {
        assertThat(ClassApi.declaredFields(Order.class))
                .contains("id", "customer", "total", "items", "discounts", "paid", "STATUS_NEW");
        assertThat(ClassApi.declaredMethods(Order.class))
                .contains("getId", "addLines", "pay", "cancel", "internalTag");
        assertThat(ClassApi.declaredConstructorCount(Order.class)).isEqualTo(4);
    }

    @Test
    @DisplayName("Категория типа определяется набором предикатов, и порядок проверок важен")
    void classifiesTypes() {
        assertThat(ClassApi.kindOf(int.class)).isEqualTo("primitive");
        assertThat(ClassApi.kindOf(String[].class)).isEqualTo("array");
        assertThat(ClassApi.kindOf(Colour.class)).isEqualTo("enum");
        assertThat(ClassApi.kindOf(List.class)).isEqualTo("interface");
        assertThat(ClassApi.kindOf(Point.class)).isEqualTo("record");
        assertThat(ClassApi.kindOf(Order.class)).isEqualTo("class");
    }

    @Test
    @DisplayName("Аннотация — тоже интерфейс, поэтому проверять её надо раньше")
    void annotationIsAlsoAnInterface() {
        assertThat(Retention.class.isInterface()).isTrue();
        assertThat(ClassApi.kindOf(Retention.class)).isEqualTo("annotation");
    }

    @Test
    @DisplayName("getComponentType() раскрывает тип элемента массива")
    void readsArrayComponentType() {
        assertThat(ClassApi.componentType(String[].class)).isEqualTo(String.class);
        assertThat(ClassApi.componentType(int[][].class)).isEqualTo(int[].class);
        assertThat(ClassApi.componentType(String.class)).isNull();
    }

    @Test
    @DisplayName("Вложенный класс знает своего внешнего владельца")
    void knowsEnclosingClass() {
        assertThat(ClassApi.enclosingClass(Order.PaymentException.class)).isEqualTo(Order.class);
        assertThat(ClassApi.enclosingClass(Order.class)).isNull();
    }

    @Test
    @DisplayName("Иерархия и полный набор интерфейсов — основа подбора бина по типу")
    void walksTypeGraph() {
        assertThat(ClassApi.superChain(ArrayList.class))
                .containsExactly("ArrayList", "AbstractList", "AbstractCollection", "Object");
        assertThat(ClassApi.allInterfaces(ArrayList.class))
                .contains("List", "Collection", "Iterable", "RandomAccess", "Cloneable");
    }

    @Test
    @DisplayName("isAssignableFrom читается «слева можно хранить справа»")
    void assignabilityDirection() {
        assertThat(ClassApi.canHold(Number.class, Integer.class)).isTrue();
        assertThat(ClassApi.canHold(Integer.class, Number.class)).isFalse();
        assertThat(ClassApi.canHold(Collection.class, ArrayList.class)).isTrue();
    }

    @Test
    @DisplayName("У record есть отдельное API компонентов, у enum — констант")
    void readsRecordAndEnumMetadata() {
        assertThat(ClassApi.recordComponents(Point.class)).containsExactly("x", "y");
        assertThat(ClassApi.recordComponents(Order.class)).isEmpty();
        assertThat(ClassApi.enumConstants(Colour.class)).containsExactly("RED", "GREEN");
        assertThat(ClassApi.enumConstants(Order.class)).isEmpty();
    }

    @Test
    @DisplayName("Массив создаётся фабрикой Array.newInstance — new здесь неприменим")
    void createsArrayReflectively() {
        Object array = ClassApi.newArray(String.class, 3);

        assertThat(array).isInstanceOf(String[].class);
        assertThat(((String[]) array)).hasSize(3);
        assertThat(array.getClass().getComponentType()).isEqualTo(String.class);
    }
}
