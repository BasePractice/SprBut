/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.declarations.Audited;
import ru.sprbut.m05.declarations.Marker;
import ru.sprbut.m05.samples.Child;
import ru.sprbut.m05.samples.ContractImpl;
import ru.sprbut.m05.samples.MarkedChild;
import ru.sprbut.m05.samples.Parent;

/**
 * Слайд 41: @Inherited и три его границы.
 * @since 1.0
 */
@DisplayName("Слайд 41: @Inherited и три его границы")
final class InheritedAnnotationTest {

    @Test
    @DisplayName("аннотация класса наследуется, если помечена @Inherited")
    void inheritsClassAnnotation() {
        MatcherAssert.assertThat(
            "class annotation cannot be inherited",
            new InheritedAnnotation<>(Child.class, Audited.class)
                .found().orElseThrow().actor(),
            Matchers.equalTo("родитель")
        );
    }

    @Test
    @DisplayName("getDeclaredAnnotation наследование игнорирует")
    void dontInheritForDeclaredLookup() {
        MatcherAssert.assertThat(
            "declared lookup cannot ignore inheritance",
            new InheritedAnnotation<>(Child.class, Audited.class).declared().isEmpty(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("граница первая: без @Inherited аннотация на подкласс не переходит")
    void dontInheritPlainAnnotation() {
        MatcherAssert.assertThat(
            "annotation without @Inherited cannot stop at the parent",
            new InheritedAnnotation<>(MarkedChild.class, Marker.class).found().isEmpty(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("граница вторая: переопределённый метод аннотацию не наследует")
    void dontInheritMethodAnnotation() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "overridden method cannot lose the annotation",
            Child.class.getMethod("action").isAnnotationPresent(Audited.class),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("граница третья: аннотация интерфейса на реализацию не переходит никогда")
    void dontInheritFromInterface() {
        MatcherAssert.assertThat(
            "interface annotation cannot stay on the interface",
            new InheritedAnnotation<>(ContractImpl.class, Audited.class).found().isEmpty(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("ручной подъём по иерархии находит то, что @Inherited не нашло")
    void findsByManualClimb() {
        MatcherAssert.assertThat(
            "manual climb cannot find the parent annotation",
            new HierarchySearch<>(MarkedChild.class, Marker.class).onClass().isPresent(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("ручной поиск метода поднимается к родителю")
    void findsMethodAnnotationUpTheHierarchy() {
        MatcherAssert.assertThat(
            "manual method search cannot reach the parent method",
            new HierarchySearch<>(Child.class, Audited.class)
                .onMethod("action").orElseThrow().actor(),
            Matchers.equalTo("метод-родителя")
        );
    }

    @Test
    @DisplayName("ручной поиск доходит и до интерфейса")
    void findsMethodAnnotationOnInterface() {
        MatcherAssert.assertThat(
            "manual search cannot reach the interface annotation",
            new HierarchySearch<>(ContractImpl.class, Audited.class)
                .onClass().isEmpty(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("на самом родителе аннотация метода объявлена честно")
    void keepsAnnotationOnParentMethod() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "parent method cannot keep its own annotation",
            Parent.class.getMethod("action").getAnnotation(Audited.class).actor(),
            Matchers.equalTo("метод-родителя")
        );
    }
}
