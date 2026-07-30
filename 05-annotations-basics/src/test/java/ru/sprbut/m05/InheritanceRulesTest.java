package ru.sprbut.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.declarations.Audited;
import ru.sprbut.m05.declarations.Marker;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 41: @Inherited и три границы его применимости")
class InheritanceRulesTest {

    @Test
    @DisplayName("@Inherited: аннотация родительского класса видна на подклассе")
    void inheritedWorksForClasses() {
        assertThat(InheritanceRules.onClass(InheritanceRules.Child.class, Audited.class))
                .get()
                .extracting(Audited::actor)
                .isEqualTo("родитель");
    }

    @Test
    @DisplayName("getDeclaredAnnotation игнорирует наследование — только объявленное здесь")
    void declaredIgnoresInheritance() {
        assertThat(InheritanceRules.declaredOnClass(InheritanceRules.Child.class, Audited.class))
                .isEmpty();
        assertThat(InheritanceRules.declaredOnClass(InheritanceRules.Parent.class, Audited.class))
                .isPresent();
    }

    @Test
    @DisplayName("Граница 1: без @Inherited аннотация на подкласс не переходит")
    void withoutInheritedNothingIsInherited() {
        assertThat(InheritanceRules.MarkedParent.class.isAnnotationPresent(Marker.class)).isTrue();
        assertThat(InheritanceRules.MarkedChild.class.isAnnotationPresent(Marker.class)).isFalse();
    }

    @Test
    @DisplayName("Граница 2: с интерфейса на реализацию аннотация не переходит никогда")
    void interfacesNeverPropagate() {
        assertThat(InheritanceRules.AuditedContract.class.isAnnotationPresent(Audited.class)).isTrue();
        assertThat(InheritanceRules.ContractImpl.class.isAnnotationPresent(Audited.class)).isFalse();
    }

    @Test
    @DisplayName("Граница 3: переопределённый метод аннотацию родителя не наследует")
    void methodsDoNotInherit() throws NoSuchMethodException {
        assertThat(InheritanceRules.onMethod(
                InheritanceRules.Parent.class.getMethod("action"), Audited.class)).isPresent();
        assertThat(InheritanceRules.onMethod(
                InheritanceRules.Child.class.getMethod("action"), Audited.class)).isEmpty();
    }

    @Test
    @DisplayName("Обход иерархии руками находит то, что @Inherited не даёт")
    void manualSearchFillsTheGaps() {
        assertThat(InheritanceRules.searchUpHierarchy(InheritanceRules.MarkedChild.class, Marker.class))
                .isPresent();

        assertThat(InheritanceRules.searchMethodUpHierarchy(
                InheritanceRules.Child.class, "action", Audited.class))
                .get()
                .extracting(Audited::actor)
                .isEqualTo("метод-родителя");
    }

    @Test
    @DisplayName("Ручной обход достаёт аннотацию и с интерфейса")
    void manualSearchReachesInterfaces() {
        assertThat(InheritanceRules.searchMethodUpHierarchy(
                InheritanceRules.ContractImpl.class, "action", Audited.class))
                .isEmpty();

        // ...но аннотация лежит на самом интерфейсе, а не на его методе
        assertThat(InheritanceRules.searchUpHierarchy(
                InheritanceRules.AuditedContract.class, Audited.class))
                .get()
                .extracting(Audited::actor)
                .isEqualTo("интерфейс");
    }

    @Test
    @DisplayName("Значение по умолчанию подставляется, если элемент не задан")
    void defaultValueIsUsed() {
        @Audited
        class Silent {
        }

        assertThat(Silent.class.getAnnotation(Audited.class).actor()).isEqualTo("system");
    }
}
