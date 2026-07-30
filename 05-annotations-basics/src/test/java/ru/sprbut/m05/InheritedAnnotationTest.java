package ru.sprbut.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.declarations.Audited;
import ru.sprbut.m05.declarations.Marker;
import ru.sprbut.m05.samples.Child;
import ru.sprbut.m05.samples.ContractImpl;
import ru.sprbut.m05.samples.MarkedChild;
import ru.sprbut.m05.samples.Parent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд 41: @Inherited и три его границы")
final class InheritedAnnotationTest {

    @Test
    @DisplayName("аннотация класса наследуется, если помечена @Inherited")
    void inheritsClassAnnotation() {
        assertThat(
            "class annotation cannot be inherited",
            new InheritedAnnotation<>(Child.class, Audited.class)
                .found().orElseThrow().actor(),
            equalTo("родитель")
        );
    }

    @Test
    @DisplayName("getDeclaredAnnotation наследование игнорирует")
    void dontInheritForDeclaredLookup() {
        assertThat(
            "declared lookup cannot ignore inheritance",
            new InheritedAnnotation<>(Child.class, Audited.class).declared().isEmpty(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("граница первая: без @Inherited аннотация на подкласс не переходит")
    void dontInheritPlainAnnotation() {
        assertThat(
            "annotation without @Inherited cannot stop at the parent",
            new InheritedAnnotation<>(MarkedChild.class, Marker.class).found().isEmpty(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("граница вторая: переопределённый метод аннотацию не наследует")
    void dontInheritMethodAnnotation() throws NoSuchMethodException {
        assertThat(
            "overridden method cannot lose the annotation",
            Child.class.getMethod("action").isAnnotationPresent(Audited.class),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("граница третья: аннотация интерфейса на реализацию не переходит никогда")
    void dontInheritFromInterface() {
        assertThat(
            "interface annotation cannot stay on the interface",
            new InheritedAnnotation<>(ContractImpl.class, Audited.class).found().isEmpty(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("ручной подъём по иерархии находит то, что @Inherited не нашло")
    void findsByManualClimb() {
        assertThat(
            "manual climb cannot find the parent annotation",
            new HierarchySearch<>(MarkedChild.class, Marker.class).onClass().isPresent(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("ручной поиск метода поднимается к родителю")
    void findsMethodAnnotationUpTheHierarchy() {
        assertThat(
            "manual method search cannot reach the parent method",
            new HierarchySearch<>(Child.class, Audited.class)
                .onMethod("action").orElseThrow().actor(),
            equalTo("метод-родителя")
        );
    }

    @Test
    @DisplayName("ручной поиск доходит и до интерфейса")
    void findsMethodAnnotationOnInterface() {
        assertThat(
            "manual search cannot reach the interface annotation",
            new HierarchySearch<>(ContractImpl.class, Audited.class)
                .onClass().isEmpty(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("на самом родителе аннотация метода объявлена честно")
    void keepsAnnotationOnParentMethod() throws NoSuchMethodException {
        assertThat(
            "parent method cannot keep its own annotation",
            Parent.class.getMethod("action").getAnnotation(Audited.class).actor(),
            equalTo("метод-родителя")
        );
    }
}
